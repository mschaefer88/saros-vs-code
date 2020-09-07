package saros.lsp.extensions.server.session;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.filesystem.IReferencePoint;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.session.dto.InviteDto;
import saros.lsp.filesystem.IWorkspacePath;
import saros.net.ConnectionState;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

public class SessionService
    implements ISessionService, IConnectionStateListener, ISessionLifecycleListener {

  private final ConnectionHandler connectionHandler;
  private final XMPPAccountStore accountStore;
  private final ISarosSessionManager sessionManager;
  private final ISarosLanguageClient client;
  private final XMPPContactsService contactService;
  private final IWorkspacePath workspace;

  public SessionService(
      ConnectionHandler connectionHandler,
      XMPPAccountStore accountStore,
      ISarosSessionManager sessionManager,
      ISarosLanguageClient client,
      XMPPContactsService contactService,
      IWorkspacePath workspace) {
    this.connectionHandler = connectionHandler;
    this.accountStore = accountStore;
    this.sessionManager = sessionManager;
    this.client = client;
    this.contactService = contactService;
    this.workspace = workspace;

    this.connectionHandler.addConnectionStateListener(this);
    this.sessionManager.addSessionLifecycleListener(this);
  }

  @Override
  public CompletableFuture<SarosResponse> connect() {

    try {
      XMPPAccount account = this.accountStore.getDefaultAccount();

      this.connectionHandler.connect(account, false);

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> disconnect() {

    try {
      this.connectionHandler.disconnect();

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> start() {

    try {
      Set<IReferencePoint> map =
          Collections.singleton(this.workspace.getReferencePoint(""));
      this.sessionManager.startSession(map);

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> stop() {

    try {

      this.sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);

    } catch (Exception e) {
      return CompletableFuture.completedFuture(new SarosResponse(e));
    }

    return CompletableFuture.completedFuture(new SarosResponse());
  }

  @Override
  public CompletableFuture<SarosResponse> invite(
      InviteDto invite) {
    CompletableFuture<SarosResponse> c = new CompletableFuture<SarosResponse>();

    Executors.newCachedThreadPool()
        .submit(
            () -> {
              try {
                if (this.sessionManager.getSession() == null) {
                  this.start().get();
                }

                JID jid = new JID(invite.id);

                this.sessionManager.invite(jid, "VS Code Invitation");

                c.complete(new SarosResponse());
              } catch (Exception e) {
                c.complete(new SarosResponse(e));
              }

              return null;
            });

    return c;
  }

  @Override
  public CompletableFuture<SarosResultResponse<Boolean>> status() {
    return CompletableFuture.completedFuture(
        new SarosResultResponse<Boolean>(this.connectionHandler.isConnected()));
  }

  @Override
  public void connectionStateChanged(ConnectionState state, ErrorType errorType) {

    this.client.sendStateConnected(
          new SarosResultResponse<Boolean>(
              state == ConnectionState.CONNECTED));
  }

  @Override
  public void sessionStarting(ISarosSession session) {
    this.client.sendStateSession(new SarosResultResponse<Boolean>(true));
  }

  @Override
  public void sessionEnding(ISarosSession session) {
    this.client.sendStateSession(new SarosResultResponse<Boolean>(false));
  }
}
