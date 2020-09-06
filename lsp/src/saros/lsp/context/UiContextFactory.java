package saros.lsp.context;

import saros.context.AbstractContextFactory;
import saros.editor.IEditorManager;
import saros.lsp.editor.EditorManager;
import saros.lsp.editor.annotation.AnnotationManager;
import saros.lsp.monitoring.ProgressMonitor;
import saros.lsp.ui.UIInteractionManager;
import saros.lsp.ui.UISynchronizerImpl;
import saros.monitoring.IProgressMonitor;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.synchronize.UISynchronizer;

public class UiContextFactory extends AbstractContextFactory {

    @Override
    public void createComponents(MutablePicoContainer container) {
        container.addComponent(UISynchronizer.class, UISynchronizerImpl.class);
        container.addComponent(IEditorManager.class, EditorManager.class);
        container.addComponent(IProgressMonitor.class, ProgressMonitor.class);
        container.addComponent(AnnotationManager.class);
        container.addComponent(UIInteractionManager.class);
    }
}
