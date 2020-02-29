package saros.lsp.activity;

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
import saros.session.User;

public class TextEditParams extends ApplyWorkspaceEditParams {

    // public TextEditParams(TextEditActivity activity) {
        

    //     TextEdit edit = createEdit(activity);

    //     TextDocumentEdit documentEdit = new TextDocumentEdit(
    //         new VersionedTextDocumentIdentifier(uri, editorManager.getVersion(activity.getPath())+1),
    //         Collections.singletonList(edit));
    //     WorkspaceEdit e = new WorkspaceEdit(Collections.singletonList(Either.forLeft(documentEdit)));

    //     this.setEdit(e);
    //     this.setLabel(activity.getSource().toString());
    // }

    // private static TextEdit createEdit(TextEditActivity activity) {
    //     int offset = activity.getOffset();
        
    //     TextEdit edit = new TextEdit();
    //     edit.setNewText(activity.getText());
    //     edit.setRange(
    //         new Range(content.getPosition(offset), content.getPosition(offset + activity.getReplacedText().length())));

    //     return edit;
    // }
}