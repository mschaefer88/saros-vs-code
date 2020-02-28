package saros.lsp.extensions.server.editor; //TODO: best package location?

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IProject;
import saros.lsp.activity.ActivityConsumer;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.IActivityConsumer.Priority;

public class EditorManager implements IEditorManager {

    private static final Logger LOG = Logger.getLogger(EditorManager.class);
    private Map<SPath, Editor> openEditors = Collections.synchronizedMap(new HashMap<SPath, Editor>());

    private final ISarosLanguageClient languageClient;
    
    
    private List<ISharedEditorListener> listeners = new CopyOnWriteArrayList<>();

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

    public EditorManager(ISarosSessionManager sessionManager, ISarosLanguageClient languageClient) {
        sessionManager.addSessionLifecycleListener(this.sessionLifecycleListener);

        this.languageClient = languageClient;
    }

    protected void uninitialize() {
        this.openEditors.clear();
    }

    protected void initialize(ISarosSession session) {
        assert this.openEditors.size() == 0 : "Open editors were not correctly reset!";
    }

    @Override
    public void openEditor(SPath path, boolean activate) {

        LOG.debug(String.format("openEditor(%s, %b)", path.toString(), activate));

        try {
            Editor editor = new Editor(path.getFile());
            this.openEditors.put(path, editor);
        
            if(activate) {
                this.languageClient.openEditor(new SarosResultResponse<String>(editor.getUri()));
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Override
    public Set<SPath> getOpenEditors() {

        LOG.debug("getOpenEditors()");

        return this.openEditors.keySet();
    }

    @Override
    public String getContent(SPath path) {

        LOG.debug(String.format("getContent(%s)", path.toString()));
        
        return this.openEditors.get(path).getText();
    }

    @Override
    public void saveEditors(IProject project) {

        LOG.debug(String.format("saveEditors(%s)", project.toString()));

        this.openEditors.keySet().forEach(path -> this.saveEditor(path));
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
        this.listeners.add(listener);
    }

    @Override
    public void removeSharedEditorListener(ISharedEditorListener listener) {
        // TODO Auto-generated method stub

        LOG.info(String.format("removeSharedEditorListener(%s)", listener.toString()));
        this.listeners.remove(listener);

    }

    public void applyTextEdit(TextEditActivity activity) {
        Editor editor = this.openEditors.get(activity.getPath());

        editor.apply(activity);

        this.listeners.forEach(listener -> listener.textEdited(activity));
    }
    
    public int getVersion(SPath path) {
        return this.openEditors.get(path).getVersion();
    }

    public void saveEditor(SPath path) {
        try {
            path.getFile().setContents(IOUtils.toInputStream(this.openEditors.get(path).getText()), true, false);
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}