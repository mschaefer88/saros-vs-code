package saros.lsp.extensions.server.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.ContactService;
import saros.lsp.extensions.server.session.dto.InviteDto;
import saros.lsp.filesystem.LspProject;
import saros.lsp.filesystem.LspWorkspace;
import saros.net.ConnectionState;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.server.filesystem.ServerPathImpl;
import saros.server.filesystem.ServerProjectImpl;
import saros.server.filesystem.ServerWorkspaceImpl;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

public class SessionService implements ISessionService, IConnectionStateListener, ISessionLifecycleListener {// TODO:
                                                                                                             // interfaces
                                                                                                             // according
                                                                                                             // to
                                                                                                             // guidelines
    private final ConnectionHandler connectionHandler;
    private final XMPPAccountStore accountStore;
    private final ISarosSessionManager sessionManager; // TODO: move to own service
    private final ISarosLanguageClient client;
    private final XMPPContactsService contactService;
    private final IWorkspace workspace;

    private static final Logger LOG = Logger.getLogger(SessionService.class);

    public SessionService(ConnectionHandler connectionHandler, XMPPAccountStore accountStore,
            ISarosSessionManager sessionManager, ISarosLanguageClient client, XMPPContactsService contactService,
            IWorkspace workspace) {
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

            LOG.debug(String.format("Connecting with %s", account.toString()));
            this.connectionHandler.connect(account, false);// TODO: on fail currently true gets back

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
            Map<IProject, List<IResource>> map = Collections.singletonMap(this.workspace.getProject(""), null);

            this.sessionManager.startSession(map);

        } catch (Exception e) {
            return CompletableFuture.completedFuture(new SarosResponse(e));
        }

        return CompletableFuture.completedFuture(new SarosResponse());
    }

    @Override
    public CompletableFuture<SarosResponse> stop() {

        try {

            this.sessionManager.stopSession(SessionEndReason.valueOf("Session Closed")); // TODO: correct?

        } catch (Exception e) {
            return CompletableFuture.completedFuture(new SarosResponse(e));
        }

        return CompletableFuture.completedFuture(new SarosResponse());
    }

    //TODO: join session
    @Override
    public CompletableFuture<SarosResponse> invite(InviteDto invite) { // TODO: hier und ähnliche nichts zurückgeben und
                                                                       // über listener Nachrichten schicken!
        CompletableFuture<SarosResponse> c = new CompletableFuture<SarosResponse>();

        Executors.newCachedThreadPool().submit(() -> {
                                                               
            try {
                if(this.sessionManager.getSession() == null) {
                    this.start().get(); //TODO: meh, do in client?
                }
    
                XMPPContact contact = this.contactService.getContact(invite.id).get(); //TODO: check if there
                JID jid = new JID(invite.id);

                LOG.info("using JID '" + jid.toString() + "'");
                this.sessionManager.invite(jid, "VS Code Invitation");
                //this.sessionManager.startSharingProjects(jid);
                                                                                       
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
        return CompletableFuture
                .completedFuture(new SarosResultResponse<Boolean>(this.connectionHandler.isConnected()));
    }

    //TODO: do along guidelines!
    @Override
    public void connectionStateChanged(ConnectionState state, Exception error) {
        
        if(error == null) {
            this.client.sendStateConnected(new SarosResultResponse<Boolean>(state == ConnectionState.CONNECTED)); //TODO: send State?
        }
    }

    @Override
    public void sessionStarting(ISarosSession session) {
        
        LOG.info("sessionStarting");
        this.client.sendStateSession(new SarosResultResponse<Boolean>(true));
    }

    @Override public void sessionEnding(ISarosSession session) {
        
        LOG.info("sessionEnding");
        this.client.sendStateSession(new SarosResultResponse<Boolean>(false));
    }
}