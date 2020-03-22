package saros.lsp.context;

import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.communication.connection.IProxyResolver;
import saros.context.AbstractContextFactory;
import saros.editor.IEditorManager;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IPathFactory;
import saros.lsp.SarosLanguageServer;
import saros.lsp.activity.ActivityConsumer;
import saros.lsp.annotation.AnnotationManager;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.path.PathFactory;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.extensions.server.session.SessionService;
import saros.lsp.extensions.server.ui.UISynchronizerImpl;
import saros.lsp.monitoring.ProgressMonitor;
import saros.lsp.service.DocumentServiceStub;
import saros.lsp.service.WorkspaceServiceStub;
import saros.monitoring.IProgressMonitor;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.editor.EditorManager;
import saros.lsp.extensions.server.eventhandler.NegotiationHandler;
import saros.lsp.extensions.server.net.SubscriptionAuthorizer;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.repackaged.picocontainer.Characteristics;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;

public class LspContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(MutablePicoContainer container) { // TODO: in sinneinheiten aufteilen und kommentieren
    container.addComponent(ISarosLanguageServer.class, SarosLanguageServer.class);
    container.addComponent(IAccountService.class, AccountService.class);
    container.addComponent(IContactService.class, ContactService.class);
    container.addComponent(ISessionService.class, SessionService.class);
    container.addComponent(INegotiationHandler.class, NegotiationHandler.class); // TODO: needed for session
    container.addComponent(UISynchronizer.class, UISynchronizerImpl.class); // TODO: needed for session
    container.addComponent(ISarosSessionContextFactory.class, LspSessionContextFactory.class); // TODO: needed for
                                                                                               // session start -
                                                                                               // otherwise
                                                                                               // nullexception!
    container.addComponent(IEditorManager.class, EditorManager.class);//.as(Characteristics.LOCK, Characteristics.CACHE); // TODO: needed for start session
    container.addComponent(IPathFactory.class, PathFactory.class); // TODO: needed for start session

    // TODO: needed to get rid of subscription pending
    container.addComponent(SubscriptionAuthorizer.class);

    container.addComponent(IProgressMonitor.class, ProgressMonitor.class);
    
    container.addComponent(TextDocumentService.class, DocumentServiceStub.class);
    container.addComponent(WorkspaceService.class, WorkspaceServiceStub.class);
    container.addComponent(AnnotationManager.class);
  }
}
