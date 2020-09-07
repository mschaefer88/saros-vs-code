package saros.lsp.extensions.server;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.lsp.SarosLauncher;
import saros.lsp.configuration.Configuration;
import saros.lsp.editor.EditorManager;
import saros.lsp.filesystem.IWorkspacePath;
import saros.lsp.filesystem.LspFile;
import saros.lsp.filesystem.LspFolder;
import saros.lsp.filesystem.LspPath;
import saros.lsp.filesystem.LspWorkspace;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

// TODO: Deprecated since not used -> to null in server?
/** Empty implementation of the workspace service. */
public class WorkspaceServiceImpl extends AbstractActivityProducer implements WorkspaceService {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);

  private ISarosSession session;

  private IWorkspacePath workspace;

  private EditorManager editorManager;

  private final ISessionLifecycleListener sessionLifecycleListener = new ISessionLifecycleListener() {

    @Override
    public void sessionStarted(final ISarosSession session) {
      initialize(session);
    }

    @Override
    public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
      uninitialize(session);
    }
  };

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) { // TODO dat Location Änderbar? -> bessere
    // Vorführung
    LOG.info("didChangeConfiguration");
    String settingsJson = params.getSettings().toString();
    Configuration configuration = new Gson().fromJson(settingsJson, Configuration.class);
  }

  protected void uninitialize(ISarosSession session) {
    this.session.removeActivityProducer(this);
    this.session = null;
  }

  protected void initialize(ISarosSession session) {
    this.session = session;
    this.session.addActivityProducer(this);
  }

  public WorkspaceServiceImpl(ISarosSessionManager sessionManager, IWorkspacePath workspace,
      EditorManager editorManager) {
    this.workspace = workspace;
    this.editorManager = editorManager;

    sessionManager.addSessionLifecycleListener(this.sessionLifecycleListener);
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    for (FileEvent fileEvent : params.getChanges()) {
      LOG.info(String.format("Changed: '%s' (%s)", fileEvent.getUri(), fileEvent.getType()));
      try {
        this.handleFileEvent(fileEvent);
      } catch (URISyntaxException | IOException e) {
        LOG.error(e);
      }
    }
  }

  private void handleFileEvent(FileEvent fileEvent) throws URISyntaxException, IOException {
    if (this.session == null) {
      return;
    }

    IResource target = this.getFile(fileEvent);

    IActivity activityFromEvent = null;
    switch (fileEvent.getType()) {
      case Changed:
        // NOP
        break;
      case Created:
        activityFromEvent = this.getCreateActivity(target);
        break;
      case Deleted:
        activityFromEvent = this.getDeleteActivity(target);
        break;
    }

    if (activityFromEvent != null) {
      this.fireActivity(activityFromEvent);
    }
  }

  private IActivity getDeleteActivity(IResource target) {
    if (target.getType() == IResource.Type.FOLDER) {
      LOG.info("DELETE FOLDER");
      return new FolderDeletedActivity(this.session.getLocalUser(), (IFolder) target);
    } else if (target.getType() == IResource.Type.FILE) {
      LOG.info("DELETE FILE");
      return new FileActivity(this.session.getLocalUser(), Type.REMOVED, Purpose.ACTIVITY, (IFile) target, null, null,
          null);
    }

    return null;
  }

  private IActivity getCreateActivity(IResource target) throws IOException {
    LOG.info(target);
    LOG.info(target.getType());
    if (target.getType() == IResource.Type.FOLDER) {
      return new FolderCreatedActivity(this.session.getLocalUser(), (IFolder) target);
    } else if (target.getType() == IResource.Type.FILE) {
      return new FileActivity(
          this.session.getLocalUser(),
          Type.CREATED,
          Purpose.ACTIVITY,
          (IFile) target,
          null,
          this.editorManager.getContent((IFile) target).getBytes(),
          ((IFile)target).getCharset());
    }

    return null;
  }

  private IResource getFile(FileEvent fileEvent) throws URISyntaxException {
    URI uri = new URI(fileEvent.getUri());
    IPath path = LspPath.fromUri(uri);

    File file = path.toFile();
    if (file.isDirectory()) {
      return new LspFolder(workspace, path);
    } else {
      return new LspFile(workspace, path);
    }
  }
}
