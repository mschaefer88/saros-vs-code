package saros.lsp.service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.lsp.SarosLauncher;
import saros.lsp.extensions.server.editor.EditorManager;
import saros.lsp.filesystem.LspPath;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

//TODO: Deprecated since not used -> to null in server?
/** Empty implementation of the workspace service. */
public class WorkspaceServiceStub extends AbstractActivityProducer implements WorkspaceService {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);

  private ISarosSession session;

  private IWorkspace workspace;

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
  public void didChangeConfiguration(DidChangeConfigurationParams params) {// TODO dat Location Änderbar? -> bessere
                                                                           // Vorführung
    LOG.info("didChangeConfiguration");
  }

  protected void uninitialize(ISarosSession session) {
    this.session.removeActivityProducer(this);
    this.session = null;
  }

  protected void initialize(ISarosSession session) {
    this.session = session;
    this.session.addActivityProducer(this);
  }

  public WorkspaceServiceStub(ISarosSessionManager sessionManager, IWorkspace workspace, EditorManager editorManager) {
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
      } catch (URISyntaxException e) {
        LOG.error(e);
      }
    }
  }

  private void handleFileEvent(FileEvent fileEvent) throws URISyntaxException {
    if (this.session == null) {
      return;
    }    

    SPath target = this.tryCreateSPath(fileEvent);
    if(target == null) {
      return;
    }

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

    if(activityFromEvent != null) {
      this.fireActivity(activityFromEvent);
    }
  }

  private IActivity getDeleteActivity(SPath target) {
    if(target.getResource().getType() == IResource.PROJECT) {
      return new FolderDeletedActivity(this.session.getLocalUser(), target);
    } else if(target.getResource().getType() == IResource.FILE) {
      return new FileActivity(this.session.getLocalUser(), Type.REMOVED, Purpose.ACTIVITY, target, null, null, null);
    }
    
    return null;    
  }

  private IActivity getCreateActivity(SPath target) {
    LOG.info(target);
    LOG.info(target.getResource());
    LOG.info(target.getResource().getType());
    if(target.getResource().getType() == IResource.PROJECT) {
      return new FolderCreatedActivity(this.session.getLocalUser(), target);
    } else if(target.getResource().getType() == IResource.FILE) {
      return new FileActivity(this.session.getLocalUser(), Type.CREATED, Purpose.ACTIVITY, target, null, this.editorManager.getContent(target).getBytes(), null);
    }
    
    return null;
  }

  private SPath tryCreateSPath(FileEvent fileEvent) throws URISyntaxException {
    URI uri = new URI(fileEvent.getUri());
    IPath path = LspPath.fromUri(uri);
    IProject project = this.workspace.getProject("");    

    File file = path.toFile();
    IResource resource = null;
    if(file.isFile()) {
      resource = project.getFile(path);
    } else if(file.isDirectory()) {
      resource = project.getFolder(path);
    } else {
      LOG.warn(String.format("'%s' doesn't seem to be a file nor a directory!", uri.getPath()));
      return null;
    }
    
    return new SPath(resource);
  }
}
