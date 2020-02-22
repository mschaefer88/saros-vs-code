package saros.lsp;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.filesystem.IProject;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.CancelManager;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.filesystem.LspProject;
import saros.lsp.filesystem.LspWorkspace;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.service.DocumentServiceStub;
import saros.lsp.service.WorkspaceServiceStub;

/** Implmenentation of the Saros language server. */
public class SarosLanguageServer implements ISarosLanguageServer {

  private static final Logger LOG = Logger.getLogger(SarosLanguageServer.class);

  private IAccountService accountService;

  private IContactService contactService;

  private ISessionService sessionService;

  private CancelManager cancelManager;

  private ISarosLanguageClient client;

  public SarosLanguageServer(IAccountService accountService, IContactService contactService, 
  ISessionService sessionService, CancelManager cancelManager, ISarosLanguageClient client) {
    this.accountService = accountService;
    this.contactService = contactService;
    this.sessionService = sessionService;
    this.cancelManager = cancelManager;
    this.client = client;
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {

    LOG.info(String.format("Client: %s", params.getClientName())); //TODO:ClientInfo impl.
    
    LspWorkspace.projects.add(new LspProject(params.getRootPath()));

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
    for (Runnable runnable : listeners) {
      runnable.run();
    }
    LOG.info("exit");    
  }


  private List<Runnable> listeners = new LinkedList<Runnable>();

  @Override
  public void addExitHook(Runnable r) {
    this.listeners.add(r);
  }

  // @JsonNotification("window/workDoneProgress/cancel")
  // public void progressCancel(WorkDoneProgressCreateParams p) {
    
  //   this.cancelManager.cancel(p.token);
  // }

  @Override
  public TextDocumentService getTextDocumentService() {
    return new DocumentServiceStub();
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new WorkspaceServiceStub();
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
