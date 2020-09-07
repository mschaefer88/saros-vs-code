package saros.lsp.context;

import saros.lsp.activity.FileActivityHandler;
import saros.lsp.activity.InconsistencyHandler;
import saros.lsp.activity.SessionStatusHandler;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** LSP implementation of the {@link ISarosSessionContextFactory} interface. */
public class SessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    container.addComponent(InconsistencyHandler.class);
    container.addComponent(FileActivityHandler.class);
    container.addComponent(SessionStatusHandler.class);
  }
}
