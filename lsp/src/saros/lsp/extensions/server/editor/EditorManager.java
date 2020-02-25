package saros.lsp.extensions.server.editor; //TODO: best package location?

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import saros.activities.SPath;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IProject;
import saros.lsp.activity.ActivityConsumer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.IActivityConsumer.Priority;

public class EditorManager implements IEditorManager {

    private static final Logger LOG = Logger.getLogger(EditorManager.class);
    private Set<SPath> openEditors = Collections.synchronizedSet(new HashSet<SPath>());

    private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(final ISarosSession session) {
            initialize(session);
        }

        @Override
        public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
            uninitialize();
        }
      };

    public EditorManager(ISarosSessionManager sessionManager) {
        sessionManager.addSessionLifecycleListener(this.sessionLifecycleListener);
    }

    protected void uninitialize() {
        this.openEditors.clear();
    }

    protected void initialize(ISarosSession session) {
        assert this.openEditors.size() == 0 : "Open editors were not correctly reset!";

        session.addActivityConsumer(new ActivityConsumer(), Priority.ACTIVE);
    }

    @Override
    public void openEditor(SPath path, boolean activate) {//TODO: activate?

        LOG.info(String.format("openEditor(%s, %b)", path.toString(), activate));
        this.openEditors.add(path);
        //TODO: open in vscode
    }

    @Override
    public Set<SPath> getOpenEditors() {

        LOG.info("getOpenEditors()");
        return this.openEditors; //TODO: ask vscode?
    }

    @Override
    public String getContent(SPath path) {//TODO: get from vscode? or from file?

        LOG.info(String.format("getContent(%s)", path.toString()));
        try {
            return IOUtils.toString(path.getFile().getContents(), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            LOG.error(e);
            return "";
        }
    }

    @Override
    public void saveEditors(IProject project) {
        // TODO Auto-generated method stub

        LOG.info(String.format("saveEditors(%s)", project.toString()));

    }

    @Override
    public void closeEditor(SPath path) {

        LOG.info(String.format("closeEditor(%s)", path.toString()));
        this.openEditors.remove(path);
        //TODO: close in vscode
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