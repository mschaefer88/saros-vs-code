package saros.lsp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;

import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.log.LanguageClientAppender;
import saros.lsp.log.LogOutputStream;

/** Entry point for the Saros LSP server. */
public class SarosLauncher {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
  private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

  /**
   * Starts the server.
   *
   * @param args command-line arguments
   * @throws Exception on critical failures
   */
  public static void main(String[] args) throws Exception {

    // if (args.length > 1) {
    //   throw new IllegalArgumentException("wrong number of arguments");
    // } else if (args.length != 1) {
    //   throw new IllegalArgumentException("port parameter not supplied");
    // }

    URL log4jProperties = SarosLauncher.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);

    LogOutputStream los = new LogOutputStream(LOG, Level.DEBUG); //aka DEBUG
    PrintStream ps = new PrintStream(los);
    System.setOut(ps);

    int port = Integer.parseInt(args[0]);
    LOG.info("listening on port " + port);
    // Socket socket = new Socket("localhost", port);
    //Socket socket = new Socket("localhost", port);


    SarosLifecycle lifecycle = new SarosLifecycle();

    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        // try {
          //socket.close();
          lifecycle.stop();
        // } catch (IOException e) {
        //   // NOP
        // }
      }
    });

    lifecycle.start();

    ISarosLanguageServer langSvr = lifecycle.createLanguageServer();

LOG.info(String.format("LangSvr is null? %b", langSvr == null));

    Launcher<ISarosLanguageClient> l =

        // Launcher.createLauncher(
        // langSvr, ISarosLanguageClient.class, socket.getInputStream(),
        // socket.getOutputStream(), Executors.newFixedThreadPool(20), mc -> mc);

        createSocketLauncher(langSvr, ISarosLanguageClient.class, new InetSocketAddress("localhost", port));
    ISarosLanguageClient langClt = lifecycle.registerLanguageClient(l.getRemoteProxy());
    LOG.addAppender(new LanguageClientAppender(langClt)); //aka TRACE
     
    LOG.info("LISTENING");
    l.startListening();
    LOG.info("CONNECTED");
  }


  static <T> Launcher<T> createSocketLauncher(Object localService, Class<T> remoteInterface, SocketAddress socketAddress) throws IOException {
    AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(socketAddress);
    AsynchronousSocketChannel socketChannel;
    try {
      Function<MessageConsumer, MessageConsumer> wrapper = consumer -> {
        MessageConsumer result = consumer;
        return result;
      };
        socketChannel = serverSocket.accept().get();

        Runtime.getRuntime().addShutdownHook(new Thread() {
          public void run() {
            try {
              socketChannel.close();
            } catch (IOException e) {
              // NOP
            }
          }
        });
        
        return Launcher.createIoLauncher(localService, remoteInterface, Channels.newInputStream(socketChannel), Channels.newOutputStream(socketChannel), Executors.newCachedThreadPool(), wrapper);
    } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
    }
    return null;
}

  static <T> Launcher<T> createSocketLaunchertest(Object localService, Class<T> remoteInterface,
       SocketAddress socketAddress) throws IOException, InterruptedException, ExecutionException {


		// // AsynchronousServerSocketChannel  serverSocket = AsynchronousServerSocketChannel.open();
		
		// // serverSocket.bind(socketAddress);
    // // AsynchronousSocketChannel socketChannel = serverSocket.accept().get();
    
    //AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open().bind(socketAddress);
    AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open().bind(socketAddress);
    

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          socketChannel.close();
        } catch (IOException e) {
          // NOP
        }
      }
    });

		return Launcher.createIoLauncher(localService, remoteInterface, Channels.newInputStream(socketChannel), Channels.newOutputStream(socketChannel), Executors.newCachedThreadPool(), mc -> mc);
	}
}
