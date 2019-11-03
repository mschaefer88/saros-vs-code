package saros.lsp.extensions.server.editor; //TODO: best package location?

import java.util.Collections;
import java.util.Set;

import org.apache.log4j.Logger;

import saros.activities.SPath;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IProject;
import saros.session.User;

public class EditorManager implements IEditorManager {

    private static final Logger LOG = Logger.getLogger(EditorManager.class);

    @Override
    public void openEditor(SPath path, boolean activate) {
        // TODO Auto-generated method stub

        LOG.info(String.format("openEditor(%s, %b)", path.toString(), activate));
    }

    @Override
    public Set<SPath> getOpenEditors() {
        // TODO Auto-generated method stub

        LOG.info("getOpenEditors()");
        return Collections.emptySet();
    }

    @Override
    public String getContent(SPath path) {
        // TODO Auto-generated method stub

        LOG.info(String.format("getContent(%s)", path.toString()));
        return null;
    }

    @Override
    public void saveEditors(IProject project) {
        // TODO Auto-generated method stub

        LOG.info(String.format("saveEditors(%s)", project.toString()));

    }

    @Override
    public void closeEditor(SPath path) {
        // TODO Auto-generated method stub

        LOG.info(String.format("closeEditor(%s)", path.toString()));
    }

    @Override
    public void adjustViewport(SPath path, LineRange range, TextSelection selection) {
        // TODO Auto-generated method stub

        LOG.info(String.format("adjustViewport(%s, %s, %s)", path.toString(), range.toString(), selection.toString()));

    }

    @Override
    public void jumpToUser(User target) {
        // TODO Auto-generated method stub

        LOG.info(String.format("jumpToUser(%s)", target.toString()));

    }

    @Override
    public void addSharedEditorListener(ISharedEditorListener listener) {
        // TODO Auto-generated method stub

        LOG.info(String.format("addSharedEditorListener(%s)", listener.toString()));

    }

    @Override
    public void removeSharedEditorListener(ISharedEditorListener listener) {
        // TODO Auto-generated method stub

        LOG.info(String.format("removeSharedEditorListener(%s)", listener.toString()));

    }

}