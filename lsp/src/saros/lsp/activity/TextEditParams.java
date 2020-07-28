package saros.lsp.activity;

import java.nio.file.Paths;
import java.util.Collections;

import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.filesystem.IWorkspace;
import saros.lsp.editor.EditorManager;
import saros.lsp.editor.adapter.EditorString;
import saros.session.User;

public class TextEditParams extends ApplyWorkspaceEditParams {

    public TextEditParams(IWorkspace workspace, EditorManager editorManager, TextEditActivity activity) {
        //TODO: replace workspace by project get full path!
        
        String content = editorManager.getContent(activity.getPath());        
        TextEdit edit = createEdit(content, activity);

        TextDocumentEdit documentEdit = new TextDocumentEdit(
            this.createIdentifier(workspace, editorManager, activity),
            Collections.singletonList(edit));
        WorkspaceEdit e = new WorkspaceEdit(Collections.singletonList(Either.forLeft(documentEdit)));

        this.setEdit(e);
        this.setLabel(activity.getSource().toString());
    }

    private VersionedTextDocumentIdentifier createIdentifier(IWorkspace workspace, EditorManager editorManager, TextEditActivity activity) {
        String uri = createFileUri(workspace, activity.getPath());
        return new VersionedTextDocumentIdentifier(uri, editorManager.getVersion(activity.getPath()));
    }

    private static String createFileUri(IWorkspace workspace, SPath path) {
        return Paths.get(workspace.getLocation().append(path.getFullPath()).toString()).toUri().toString();//TODO: do bettter!
    }

    private static TextEdit createEdit(String content, TextEditActivity activity) {
        int offset = activity.getOffset();
        EditorString editorString = new EditorString(content);
        
        TextEdit edit = new TextEdit();
        edit.setNewText(activity.getText());
        edit.setRange(
            new Range(editorString.getPosition(offset), editorString.getPosition(offset + activity.getReplacedText().length())));

        return edit;
    }
}