package saros.lsp.context; // TODO: move to other package! (is no real factory like the other)

import org.apache.log4j.Logger;
import saros.lsp.activity.FileActivityHandler;
import saros.lsp.activity.InconsistencyHandler;
import saros.lsp.activity.SessionStatusHandler;
import saros.lsp.interaction.session.NegotiationHook;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** LSP implementation of the {@link ISarosSessionContextFactory} interface. */
public class SessionContextFactory
    extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    container.addComponent(NegotiationHook.class);
    container.addComponent(InconsistencyHandler.class);
    container.addComponent(FileActivityHandler.class);
    container.addComponent(SessionStatusHandler.class);
  }
}
