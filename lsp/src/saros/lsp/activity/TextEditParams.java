package saros.lsp.activity;

import java.nio.file.Paths;
import java.util.Collections;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import saros.filesystem.IFile;
import saros.activities.TextEditActivity;
import saros.filesystem.IWorkspace;
import saros.lsp.editor.EditorManager;
import saros.lsp.filesystem.IWorkspacePath;

public class TextEditParams extends ApplyWorkspaceEditParams {

  public TextEditParams(
      IWorkspacePath workspace, EditorManager editorManager, TextEditActivity activity) {
    // TODO: replace workspace by project get full path!

    String content = editorManager.getContent(activity.getResource());
    TextEdit edit = createEdit(content, activity);

    TextDocumentEdit documentEdit =
        new TextDocumentEdit(
            this.createIdentifier(workspace, editorManager, activity),
            Collections.singletonList(edit));
    WorkspaceEdit e = new WorkspaceEdit(Collections.singletonList(Either.forLeft(documentEdit)));

    this.setEdit(e);
    this.setLabel(activity.getSource().toString());
  }

  private VersionedTextDocumentIdentifier createIdentifier(
    IWorkspacePath workspace, EditorManager editorManager, TextEditActivity activity) {
    String uri = createFileUri(workspace, activity.getResource());
    return new VersionedTextDocumentIdentifier(uri, editorManager.getVersion(activity.getResource()));
  }

  private static String createFileUri(IWorkspacePath workspace, IFile path) {
    return Paths.get(path.getReferencePointRelativePath().toString()) //TODO: MIGRATION
        .toUri()
        .toString(); // TODO: do better!
  }

  private static TextEdit createEdit(String content, TextEditActivity activity) {
    TextEdit edit = new TextEdit();
    edit.setNewText(activity.getNewText());
    edit.setRange(
        new Range(
            new Position(activity.getStartPosition().getLineNumber(), activity.getStartPosition().getInLineOffset()), //TODO: use conversion
            new Position(activity.getNewEndPosition().getLineNumber(), activity.getNewEndPosition().getInLineOffset()))); //TODO: use conversion

    return edit;
  }
}
