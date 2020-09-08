package saros.lsp.activity;

import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.session.dto.SessionUserDto;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.preferences.IPreferenceStore;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;

/**
 * Handler responsible for listening to session status changes
 * and reporting them to the user.
 */
public class SessionStatusHandler {

  private XMPPContactsService contactsService;
  private ISarosSessionManager sessionManager;
  private UIInteractionManager interactionManager;

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
          session.addListener(sessionListener);
          client.sendStateSession(new SarosResultResponse<Boolean>(true));

          session
              .getRemoteUsers()
              .forEach(
                  user -> {
                    client.notifyUserJoinedSession(createParticipantDto(user));
                  });
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
          client.sendStateSession(new SarosResultResponse<Boolean>(false));
        }
      };

  private final ISarosLanguageClient client;

  private ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void userColorChanged(User user) {
          if (user.isHost()) {
            return;
          }
          client.notifyUserChangedSession(createParticipantDto(user));
        }

        @Override
        public void userJoined(User user) {
          if (user.isHost()) {
            return;
          }
          client.notifyUserJoinedSession(createParticipantDto(user));
        }

        @Override
        public void userLeft(User user) {
          if (user.isHost()) {
            return;
          }
          client.notifyUserLeftSession(createParticipantDto(user));

          if (sessionManager.getSession().getRemoteUsers().size() == 0) {
            if (interactionManager.getUserInputYesNo("Session is empty", "Close session?")) {
              sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
            }
          }
        }
      };

  public SessionStatusHandler(
      ISarosSessionManager sessionManager,
      ISarosLanguageClient client,
      XMPPContactsService contactsService,
      UIInteractionManager interactionManager,
      IPreferenceStore preferenceStore) {
    this.client = client;
    this.contactsService = contactsService;
    this.sessionManager = sessionManager;
    this.interactionManager = interactionManager;

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  /**
   * Creates the dto send to the client representing a user.
   * 
   * @param user The user of the session whose state has changed
   * @return The dto representing the user
   */
  private SessionUserDto createParticipantDto(User user) {
    final XMPPContact userAsContact =
        this.contactsService.getContact(user.getJID().toString()).get();

    int colorId = user.getColorID();

    return new SessionUserDto(
        userAsContact.getBareJid().getBase().toString(),
        userAsContact.getDisplayableName(),
        colorId);
  }
}
