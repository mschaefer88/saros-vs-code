package saros.lsp.context;

import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.context.AbstractContextFactory;
import saros.editor.IEditorManager;
import saros.filesystem.IPathFactory;
import saros.filesystem.IWorkspace;
import saros.lsp.SarosLanguageServer;
import saros.lsp.editor.EditorManager;
import saros.lsp.editor.annotation.AnnotationManager;
import saros.lsp.extensions.server.DocumentServiceStub;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.WorkspaceServiceStub;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.IDocumentService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.extensions.server.session.SessionService;
import saros.lsp.filesystem.LspWorkspace;
import saros.lsp.filesystem.PathFactory;
import saros.lsp.interaction.SubscriptionAuthorizer;
import saros.lsp.interaction.session.NegotiationHandler;
import saros.lsp.monitoring.ProgressMonitor;
import saros.lsp.ui.UIInteractionManager;
import saros.lsp.ui.UISynchronizerImpl;
import saros.monitoring.IProgressMonitor;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionContextFactory;
import saros.synchronize.UISynchronizer;

public class LspContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(
      MutablePicoContainer container) { // TODO: in sinneinheiten aufteilen und kommentieren
    container.addComponent(ISarosLanguageServer.class, SarosLanguageServer.class);
    container.addComponent(IAccountService.class, AccountService.class);
    container.addComponent(IContactService.class, ContactService.class);
    container.addComponent(ISessionService.class, SessionService.class);
    container.addComponent(
        INegotiationHandler.class, NegotiationHandler.class); // TODO: needed for session
    container.addComponent(
        UISynchronizer.class, UISynchronizerImpl.class); // TODO: needed for session
    container.addComponent(
        ISarosSessionContextFactory.class, LspSessionContextFactory.class); // TODO: needed for
    // session start -
    // otherwise
    // nullexception!
    container.addComponent(
        IEditorManager.class,
        EditorManager
            .class); // .as(Characteristics.LOCK, Characteristics.CACHE); // TODO: needed for start
                     // session
    container.addComponent(IPathFactory.class, PathFactory.class); // TODO: needed for start session

    // TODO: needed to get rid of subscription pending
    container.addComponent(SubscriptionAuthorizer.class);

    container.addComponent(IProgressMonitor.class, ProgressMonitor.class);

    container.addComponent(IDocumentService.class, DocumentServiceStub.class);
    container.addComponent(WorkspaceService.class, WorkspaceServiceStub.class);
    container.addComponent(AnnotationManager.class);
    container.addComponent(UIInteractionManager.class);
    container.addComponent(IWorkspace.class, LspWorkspace.class);
  }
}
