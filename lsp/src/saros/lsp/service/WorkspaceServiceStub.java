package saros.lsp.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;

import saros.activities.FileActivity;
import saros.activities.SPath;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IWorkspace;
import saros.lsp.SarosLauncher;
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

  public WorkspaceServiceStub(ISarosSessionManager sessionManager, IWorkspace workspace) {
    this.workspace = workspace;
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

    SPath target = this.createSPath(fileEvent);

    FileActivity activityFromEvent = null;
    switch (fileEvent.getType()) {
      case Changed:
        // NOP
        break;
      case Created:
        activityFromEvent = new FileActivity(this.session.getLocalUser(), Type.CREATED, Purpose.ACTIVITY, target, null, null, null);
        break;
      case Deleted:
        activityFromEvent = new FileActivity(this.session.getLocalUser(), Type.REMOVED, Purpose.ACTIVITY, target, null, null, null);
        break;
    }

    if(activityFromEvent != null) {
      this.fireActivity(activityFromEvent);
    }
  }

  private SPath createSPath(FileEvent fileEvent) throws URISyntaxException {
    IPath path = LspPath.fromUri(new URI(fileEvent.getUri()));
    IProject project = this.workspace.getProject("");    
    SPath sPath = new SPath(project.getFile(path));
    
    return sPath;
  }
}
