package saros.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
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

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;

import saros.filesystem.IPath;
import saros.lsp.connector.AsyncSocketConnector;
import saros.lsp.connector.Connector;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.filesystem.LspPath;
import saros.lsp.filesystem.LspWorkspace;
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

    if (args.length > 1) {
      throw new IllegalArgumentException("wrong number of arguments");
    } else if (args.length != 1) {
      throw new IllegalArgumentException("port parameter not supplied");
    }

    URL log4jProperties = SarosLauncher.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);

    LogOutputStream los = new LogOutputStream(LOG, Level.DEBUG); // aka DEBUG
    PrintStream ps = new PrintStream(los);
    System.setOut(ps);

    int port = Integer.parseInt(args[0]);
    LOG.info("listening on port " + port);

    SarosLifecycle lifecycle = new SarosLifecycle();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        // try {
        // socket.close();

        LOG.info("shutdown from runtime detected");
        lifecycle.stop();
        // } catch (IOException e) {
        // // NOP
        // }
      }
    });

    lifecycle.start();

    AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open()
        .bind(new InetSocketAddress("localhost", port));
    startLanguageServer(lifecycle, serverSocket);
  }

  private static void startLanguageServer(SarosLifecycle lifecycle, AsynchronousServerSocketChannel serverSocket)
      throws IOException, InterruptedException, ExecutionException {

    ISarosLanguageServer langSvr = lifecycle.createLanguageServer();
    AsynchronousSocketChannel socket = createSocket(serverSocket);

    LOG.info("starting...");

    Launcher<ISarosLanguageClient> l = createSocketLauncher(langSvr, ISarosLanguageClient.class, socket);
    ISarosLanguageClient langClt = lifecycle.registerLanguageClient(l.getRemoteProxy());

    Appender a = new LanguageClientAppender(langClt);
    LOG.addAppender(a);

    langSvr.onInitialize(params -> {

      try {
        IPath root = LspPath.fromUri(new URI(params.getRootUri()));
        lifecycle.registerWorkspace(new LspWorkspace(root));
      } catch (URISyntaxException e) {
        LOG.error(e);
      }
    });
    
    langSvr.onExit(() ->{
    
      try {
          
        LOG.info("removing appender");
        LOG.removeAppender(a);
        LOG.info("done");
        LOG.info("closing old socket");
        socket.close();
        LOG.info("closed");
        
        startLanguageServer(lifecycle, serverSocket);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ExecutionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    
    l.startListening();
    LOG.info("CONNECTED");

  }

  static AsynchronousSocketChannel createSocket(AsynchronousServerSocketChannel serverSocket) throws IOException, InterruptedException, ExecutionException {
    
    AsynchronousSocketChannel socketChannel;
      
        socketChannel = serverSocket.accept().get();

        Runtime.getRuntime().addShutdownHook(new Thread() {
          public void run() {
            try {
              LOG.info("shutdown from runtime detected");
              socketChannel.close();
            } catch (IOException e) {
              // NOP
            }
          }
        });

    return socketChannel;
  }

  static <T> Launcher<T> createSocketLauncher(Object localService, Class<T> remoteInterface, AsynchronousSocketChannel socketChannel) throws IOException {
    Function<MessageConsumer, MessageConsumer> wrapper = consumer -> {
      MessageConsumer result = consumer;
      return result;
    };    

        return Launcher.createIoLauncher(localService, remoteInterface, Channels.newInputStream(socketChannel), Channels.newOutputStream(socketChannel), Executors.newCachedThreadPool(), wrapper);
  }
}
