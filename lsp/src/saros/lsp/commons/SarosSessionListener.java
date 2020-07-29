package saros.lsp.commons;

import java.util.Optional;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

public class SarosSessionListener {

  private ISarosSession session;

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
          initialize(session);
        }

        @Override
        public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
          uninitialize(session);
        }
      };

  protected SarosSessionListener(ISarosSessionManager sessionManager) {
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  protected void initialize(ISarosSession session) {
    this.session = session;
  }

  protected void uninitialize(ISarosSession session) {
    this.session = null;
  }

  public Optional<ISarosSession> getSession() {
    return Optional.ofNullable(this.session);
  }
}
