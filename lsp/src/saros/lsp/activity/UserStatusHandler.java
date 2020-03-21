package saros.lsp.activity;

import java.util.Optional;

import org.apache.log4j.Logger;

import saros.core.manager.xmpp.XmppContactManager;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.session.dto.SessionUserDto;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;

//TODO: move to session ns? or saros.ui.eventhandler
public class UserStatusHandler {

  private static Logger LOG = Logger.getLogger(UserStatusHandler.class);

  private XMPPContactsService contactsService;

    private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
          LOG.info("UserStatusHandler.sessionStarting");
          session.addListener(sessionListener);
          
          session.getRemoteUsers().forEach(user -> {
            client.notifyUserJoinedSession(createParticipantDto(user));//TODO: is host
          });
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          LOG.info("UserStatusHandler.sessionEnded");
          session.removeListener(sessionListener);
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
          client.notifyUserLeftSession(new SarosResultResponse<String>(user.getJID().toString()));//TODO: is host / getDisplayablename from XMPPContact
        //   SarosView.showNotification(
        //       Messages.UserStatusChangeHandler_user_left,
        //       CoreUtils.format(Messages.UserStatusChangeHandler_user_left_text, user));
        }
      };      

  public UserStatusHandler(ISarosSessionManager sessionManager, ISarosLanguageClient client, XMPPContactsService contactsService) { //TODO: is interface injected everywhere? (rather than the class)
    LOG.info("UserStatusHandler.CTOR");
    this.client = client;
    this.contactsService = contactsService;

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  private SessionUserDto createParticipantDto(User user) {
    final XMPPContact userAsContact = this.contactsService.getContact(user.getJID().toString()).get();
    return new SessionUserDto(userAsContact.getBareJid().toString(), userAsContact.getDisplayableName());
  }
}