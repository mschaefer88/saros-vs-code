package saros.lsp.context; //TODO: move to other package! (is no real factory like the other)

import org.apache.log4j.Logger;

import saros.context.IContextFactory;
import saros.lsp.session.NegotiationHook;
import saros.lsp.session.SessionStatusRequestHandler;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.server.session.JoinSessionRequestHandler;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** LSP implementation of the {@link ISarosSessionContextFactory} interface. */
public class LspSessionContextFactory extends SarosCoreSessionContextFactory implements IContextFactory {//TODO: interface sinnvoll?


  private static final Logger LOG = Logger.getLogger(LspSessionContextFactory.class);
  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    LOG.info("createNonCoreComponents");
    container.addComponent(JoinSessionRequestHandler.class);
    container.addComponent(SessionStatusRequestHandler.class);

    container.addComponent(NegotiationHook.class);
    // // IDE context wrapper
    // container.addComponent(SharedIDEContext.class);
    // container.addComponent(ApplicationEventHandlersFactory.class);
    // container.addComponent(ProjectEventHandlersFactory.class);

    // // Project interaction
    // container.addComponent(ProjectClosedHandler.class);

    // // Editor interaction
    // container.addComponent(LocalEditorHandler.class);
    // container.addComponent(LocalEditorManipulator.class);
    // container.addComponent(SelectedEditorStateSnapshotFactory.class);

    // // Annotation utility to create, remove, and manage annotations
    // container.addComponent(AnnotationManager.class);

    // // Other
    // if (!session.isHost()) {
    //   container.addComponent(ModuleConfigurationInitializer.class);
    // }
    // container.addComponent(SharedResourcesManager.class);

    // // User notifications
    // container.addComponent(FollowModeNotificationDispatcher.class);
  }
}
