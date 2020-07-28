package saros.lsp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.ColorProviderOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.DocumentServiceStub;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.WorkspaceServiceStub;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.filesystem.LspPath;
import saros.lsp.filesystem.LspProject;
import saros.lsp.filesystem.LspWorkspace;
import saros.lsp.extensions.server.contact.IContactService;

/** Implmenentation of the Saros language server. */
public class SarosLanguageServer implements ISarosLanguageServer {

  private static final Logger LOG = Logger.getLogger(SarosLanguageServer.class);

  private IAccountService accountService;

  private IContactService contactService;

  private ISessionService sessionService;

  private TextDocumentService documentService;

  private WorkspaceService workspaceService;

  public SarosLanguageServer(IAccountService accountService, IContactService contactService,
      ISessionService sessionService, TextDocumentService documentService, WorkspaceService workspaceService) {
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
   * Capabilities are language related features like: * syntax highlighting * code
   * lens * hover * code completition
   * 
   * The capabilities are being evaluated by the IDE that uses the server in order
   * to know which features can be used.
   * 
   * Since this server isn't processing any programming language in the original
   * sense all features will default to false.
   * 
   * @return ServerCapabilities capabilities of the server
   */
  private ServerCapabilities createCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();

    capabilities.setExperimental(true);
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Incremental);
    // CodeLensOptions clo = new CodeLensOptions();
    // clo.setResolveProvider(true);
    // capabilities.setCodeLensProvider(clo);
    // capabilities.setHoverProvider(true);

    // StaticProgressOptions opts = new StaticProgressOptions();
    // opts.setWorkDoneProgress(true);
    // capabilities.setWindow(opts);

    return capabilities;
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    LOG.info("shutdown");
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
    this.exitListeners.forEach(listener -> listener.run());
    
    LOG.info("exit");    
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

  // @JsonNotification("window/workDoneProgress/cancel")
  // public void progressCancel(WorkDoneProgressCreateParams p) {
    
  //   this.cancelManager.cancel(p.token);
  // }

  @Override
  public TextDocumentService getTextDocumentService() {
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
  public ISessionService getSarosConnectionService() {
    return this.sessionService;
  }
}
