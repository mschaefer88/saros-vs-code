package saros.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.IDocumentService;
import saros.lsp.extensions.server.session.ISessionService;

/** Implmenentation of the Saros language server. */
public class SarosLanguageServer implements ISarosLanguageServer {

  private static final Logger LOG = Logger.getLogger(SarosLanguageServer.class);

  private IAccountService accountService;

  private IContactService contactService;

  private ISessionService sessionService;

  private IDocumentService documentService;

  private WorkspaceService workspaceService;

  public SarosLanguageServer(
      IAccountService accountService,
      IContactService contactService,
      ISessionService sessionService,
      IDocumentService documentService,
      WorkspaceService workspaceService) {
    this.accountService = accountService;
    this.contactService = contactService;
    this.sessionService = sessionService;
    this.documentService = documentService;
    this.workspaceService = workspaceService;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

    this.initializeListeners.forEach(listener -> listener.accept(params));

    return CompletableFuture.completedFuture(new InitializeResult(this.createCapabilities()));
  }

  /**
   * Creates the language capabilities of the server.
   *
   * <p>Capabilities are language related features like: * syntax highlighting * code lens * hover *
   * code completition
   *
   * <p>The capabilities are being evaluated by the IDE that uses the server in order to know which
   * features can be used.
   *
   * <p>Since this server isn't processing any programming language in the original sense all
   * features will default to false.
   *
   * @return ServerCapabilities capabilities of the server
   */
  private ServerCapabilities createCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();

    capabilities.setExperimental(true);
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);

    return capabilities;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    this.exitListeners.forEach(listener -> listener.run());
  }

  private List<Runnable> exitListeners = new ArrayList<>();
  private List<Consumer<InitializeParams>> initializeListeners = new ArrayList<>();

  @Override
  public void onInitialize(Consumer<InitializeParams> consumer) {
    this.initializeListeners.add(consumer);
  }

  @Override
  public void onExit(Runnable runnable) {
    this.exitListeners.add(runnable);
  }

  @Override
  public IDocumentService getTextDocumentService() {
    return this.documentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return this.workspaceService;
  }

  @Override
  public IAccountService getSarosAccountService() {
    return this.accountService;
  }

  @Override
  public IContactService getSarosContactService() {
    return this.contactService;
  }

  @Override
  public ISessionService getSarosSessionService() {
    return this.sessionService;
  }
}
