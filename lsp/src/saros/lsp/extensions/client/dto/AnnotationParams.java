package saros.lsp.extensions.client.dto;

import java.nio.file.Paths;

import org.eclipse.lsp4j.Range;

import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.filesystem.IWorkspace;
import saros.lsp.adapter.EditorString;

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
}