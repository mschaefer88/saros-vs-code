package saros.lsp.extensions.client.dto;

import java.nio.file.Paths;

import org.eclipse.lsp4j.Range;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.filesystem.IWorkspace;
import saros.lsp.editor.adapter.EditorString;
import saros.lsp.editor.annotation.Annotation;

public class AnnotationParams {
    public String uri;

    public String user;

    public Range range;

    public AnnotationParams(TextEditActivity activity, IWorkspace workspace, IEditorManager manager) {
        this.uri = Paths.get(workspace.getLocation().append(activity.getPath().getFullPath()).toString()).toUri().toString();//TODO: do bettter! (centralized + correct get fullpath)
        this.user = activity.getSource().getJID().getName();

        EditorString es = new EditorString(manager.getContent(activity.getPath()));
        this.range = new Range(es.getPosition(activity.getOffset()), es.getPosition(activity.getOffset()+activity.getText().length()));
    }

    public AnnotationParams(Annotation annotation, IWorkspace workspace, SPath path) {
        this.uri = Paths.get(workspace.getLocation().append(path.getFullPath()).toString()).toUri().toString();//TODO: do bettter! (centralized + correct get fullpath)
        this.user = annotation.getSource().getJID().getName();
        this.range = annotation.getRange();
    }
}