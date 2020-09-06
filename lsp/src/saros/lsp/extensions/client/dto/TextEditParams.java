package saros.lsp.extensions.client.dto;

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
import saros.lsp.filesystem.LspFile;

public class TextEditParams extends ApplyWorkspaceEditParams {

  public TextEditParams(
      IWorkspacePath workspace, EditorManager editorManager, TextEditActivity activity) {
    // TODO: replace workspace by project get full path!

    IFile workspaceFile = new LspFile(workspace, activity.getResource().getReferencePointRelativePath());//TODO: better Way? How to elimenate the need of a ws?)
    String content = editorManager.getContent(workspaceFile);
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
    return Paths.get(workspace.toString(), path.getReferencePointRelativePath().toString()) //TODO: MIGRATION
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
