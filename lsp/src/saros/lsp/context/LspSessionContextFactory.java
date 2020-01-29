package saros.lsp.context; //TODO: move to other package! (is no real factory like the other)

import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.server.session.JoinSessionRequestHandler;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** LSP implementation of the {@link ISarosSessionContextFactory} interface. */
public class LspSessionContextFactory extends SarosCoreSessionContextFactory {//TODO: interface sinnvoll?

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {

    container.addComponent(JoinSessionRequestHandler.class);
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
