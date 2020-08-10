package saros.lsp.extensions.client.dto;

import java.nio.file.Paths;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import saros.filesystem.IFile;
import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.editor.text.TextPosition;
import saros.filesystem.IWorkspace;
import saros.lsp.editor.annotation.Annotation;
import saros.lsp.filesystem.IWorkspacePath;

public class AnnotationParams {
  public String uri;

  public String user;

  public Range range;

  public int annotationColorId;

  public AnnotationParams(TextEditActivity activity, IWorkspacePath workspace, IEditorManager manager) {
    this.uri =
        Paths.get(workspace.append(activity.getResource().getReferencePointRelativePath()).toString()) //TODO: get from Editor/TextDocument!
            .toUri()
            .toString(); // TODO: do bettter! (centralized + correct get fullpath)
    this.user = activity.getSource().getJID().getName();

    TextPosition start = activity.getStartPosition();
    TextPosition end = activity.getNewEndPosition();
    this.range =
        new Range(
            new Position(start.getLineNumber(), start.getInLineOffset()), //TODO: converter
            new Position(end.getLineNumber(), end.getInLineOffset())); //TODO: converter
  }

  public AnnotationParams(Annotation annotation, IWorkspacePath workspace, IFile path) {
    this.uri =
    Paths.get(workspace.append(path.getReferencePointRelativePath()).toString()) //TODO: get from Editor/TextDocument!
            .toUri()
            .toString(); // TODO: do bettter! (centralized + correct get fullpath)
    this.user = annotation.getSource().getJID().getName();
    this.range = annotation.getRange();
    this.annotationColorId = annotation.getSource().getColorID();
  }
}
