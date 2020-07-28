package saros.lsp.activity;

import java.util.Optional;

import org.apache.log4j.Logger;

import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.session.dto.SessionUserDto;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;

//TODO: move to session ns? or saros.ui.eventhandler
public class SessionStatusHandler {

  private static Logger LOG = Logger.getLogger(SessionStatusHandler.class);

  private XMPPContactsService contactsService;
  private ISarosSessionManager sessionManager;
  private UIInteractionManager interactionManager;

    private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
          LOG.info("UserStatusHandler.sessionStarting");
          session.addListener(sessionListener);
          client.sendStateSession(new SarosResultResponse<Boolean>(true));
          
          session.getRemoteUsers().forEach(user -> {
            client.notifyUserJoinedSession(createParticipantDto(user));//TODO: is host
          });
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          LOG.info("UserStatusHandler.sessionEnded");
          session.removeListener(sessionListener);
          client.sendStateSession(new SarosResultResponse<Boolean>(false));
        }
      };

      private final ISarosLanguageClient client;

  private ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userJoined(User user) {
          LOG.info("UserStatusHandler.userJoined");
            client.notifyUserJoinedSession(createParticipantDto(user));//TODO: is host
        //   SarosView.showNotification(
        //       Messages.UserStatusChangeHandler_user_joined,
        //       CoreUtils.format(Messages.UserStatusChangeHandler_user_joined_text, user));
        }

        @Override
        public void userLeft(User user) {
          LOG.info("UserStatusHandler.userLeft");
          client.notifyUserLeftSession(createParticipantDto(user));//TODO: is host
        
          if(sessionManager.getSession().getRemoteUsers().size() == 0) {
            if(interactionManager.getUserInputYesNo("Session is empty", "Close session?")) {
              sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
            }
          }
        }
      };      

  public SessionStatusHandler(ISarosSessionManager sessionManager, ISarosLanguageClient client, XMPPContactsService contactsService, UIInteractionManager interactionManager) {
    LOG.info("UserStatusHandler.CTOR");
    this.client = client;
    this.contactsService = contactsService;
    this.sessionManager = sessionManager;
    this.interactionManager = interactionManager;

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  private SessionUserDto createParticipantDto(User user) {
    final XMPPContact userAsContact = this.contactsService.getContact(user.getJID().toString()).get();
    return new SessionUserDto(userAsContact.getBareJid().getBase().toString(), userAsContact.getDisplayableName());
  }
}