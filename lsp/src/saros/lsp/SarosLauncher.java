package saros.lsp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.filesystem.IWorkspacePath;
import saros.lsp.filesystem.WorkspacePath;
import saros.lsp.log.LanguageClientAppender;

/** Entry point for the Saros language server. */
public class SarosLauncher implements Callable<Integer> {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
  private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

  @Option(
      names = {"-p", "--port"},
      description = "The port to listen on")
  int port;

  @Option(
      names = {"-l", "--log"},
      description = "The log level")
  String logLevel;

  @Override
  public Integer call() throws Exception {
    final URL log4jProperties = SarosLauncher.class.getResource(LOGGING_CONFIG_FILE);
    PropertyConfigurator.configure(log4jProperties);
    LogManager.getRootLogger().setLevel(Level.toLevel(logLevel));

    LOG.info("listening on port " + port);

    final AsynchronousServerSocketChannel serverSocket =
        AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost", port));

    final SarosLifecycle lifecycle = new SarosLifecycle();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override()
              public void run() {
                lifecycle.stop();
              }
            });

    lifecycle.start();

    startLanguageServer(lifecycle, serverSocket);

    return 0;
  }

  /**
   * Starts the Saros language server.
   *
   * @param args command-line arguments
   * @throws Exception on critical failures
   */
  public static void main(final String... args) {
    new CommandLine(new SarosLauncher()).execute(args);
  }

  /**
   * Starts the Saros language server.
   *
   * @param lifecycle The lifecycle of Saros
   * @param serverSocket The used socket for a client connection
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  private void startLanguageServer(
      final SarosLifecycle lifecycle, final AsynchronousServerSocketChannel serverSocket)
      throws IOException, InterruptedException, ExecutionException {

    final ISarosLanguageServer langSvr = lifecycle.getLanguageServer();
    final AsynchronousSocketChannel socket = createSocket(serverSocket);

    LOG.info("starting saros language server...");

    final Launcher<ISarosLanguageClient> l =
        createClientLauncher(langSvr, ISarosLanguageClient.class, socket);
    final ISarosLanguageClient langClt = lifecycle.registerLanguageClient(l.getRemoteProxy());

    registerClientLogger(langClt);

    langSvr.onInitialize(
        params -> {
          try {
            IWorkspacePath root = new WorkspacePath(new URI(params.getRootUri()));
            lifecycle.registerWorkspace(root);
          } catch (URISyntaxException e) {
            LOG.error(e);
          }
        });

    l.startListening();
  }

  /**
   * Registers a log appender that logs to the Saros language client.
   *
   * @param client The Saros language client
   */
  private void registerClientLogger(final ISarosLanguageClient client) {
    final LanguageClientAppender a = new LanguageClientAppender(client);
    a.setThreshold(Level.toLevel(this.logLevel));
    LOG.addAppender(a);
  }

  /**
   * Creates a server socket for receiving Saros language client connections and waits for the
   * client to connect.
   *
   * @param serverSocket The used server socket
   * @return The socket channel with a client connection
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  static AsynchronousSocketChannel createSocket(final AsynchronousServerSocketChannel serverSocket)
      throws IOException, InterruptedException, ExecutionException {

    AsynchronousSocketChannel socketChannel;

    socketChannel = serverSocket.accept().get();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override()
              public void run() {
                try {
                  LOG.info("shutdown from runtime detected");
                  socketChannel.close();
                } catch (final IOException e) {
                  // NOP
                }
              }
            });

    return socketChannel;
  }

  /**
   * Creates the launcher of the language client that will allow communications to the client.
   *
   * @param <T> Type of the client
   * @param languageServer The local service endpoint
   * @param remoteInterface The class of the client
   * @param socketChannel The used socket channel for communication
   * @return The launcher for the specified language client
   * @throws IOException
   */
  static <T> Launcher<T> createClientLauncher(
      final Object languageServer,
      final Class<T> remoteInterface,
      final AsynchronousSocketChannel socketChannel)
      throws IOException {
    final Function<MessageConsumer, MessageConsumer> wrapper =
        consumer -> {
          final MessageConsumer result = consumer;
          return result;
        };

    return Launcher.createIoLauncher(
        languageServer,
        remoteInterface,
        Channels.newInputStream(socketChannel),
        Channels.newOutputStream(socketChannel),
        Executors.newCachedThreadPool(),
        wrapper);
  }
}
