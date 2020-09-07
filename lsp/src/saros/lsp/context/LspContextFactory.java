package saros.lsp.context;

import org.eclipse.lsp4j.services.WorkspaceService;
import saros.context.AbstractContextFactory;
import saros.lsp.SarosLanguageServer;
import saros.lsp.extensions.server.DocumentServiceImpl;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.extensions.server.WorkspaceServiceImpl;
import saros.lsp.extensions.server.account.AccountService;
import saros.lsp.extensions.server.account.IAccountService;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.contact.IContactService;
import saros.lsp.extensions.server.document.IDocumentService;
import saros.lsp.extensions.server.session.ISessionService;
import saros.lsp.extensions.server.session.SessionService;
import saros.repackaged.picocontainer.MutablePicoContainer;

public class LspContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(ISarosLanguageServer.class, SarosLanguageServer.class);
    container.addComponent(IAccountService.class, AccountService.class);
    container.addComponent(IContactService.class, ContactService.class);
    container.addComponent(ISessionService.class, SessionService.class);
    container.addComponent(IDocumentService.class, DocumentServiceImpl.class);
    container.addComponent(WorkspaceService.class, WorkspaceServiceImpl.class);
  }
}
