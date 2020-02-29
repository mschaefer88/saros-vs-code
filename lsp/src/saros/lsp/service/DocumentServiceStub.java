package saros.lsp.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IWorkspace;
import saros.lsp.activity.TextEditParams;
import saros.lsp.adapter.EditorString;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.editor.EditorManager;
import saros.lsp.filesystem.LspPath;
import saros.lsp.filesystem.LspWorkspace;
import saros.net.xmpp.JID;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.IActivityConsumer.Priority;

/** Empty implementation of the text document service. */
public class DocumentServiceStub extends AbstractActivityProducer implements TextDocumentService {

  private EditorManager editorManager;
  private ISarosSession session;

  private final ISarosLanguageClient client;
  private final IWorkspace workspace;

  private static final Logger LOG = Logger.getLogger(DocumentServiceStub.class);

  private final IActivityConsumer consumer = new AbstractActivityConsumer() {
    @Override
    public void receive(TextEditActivity activity) {
      //super.receive(activity); TODO: used?

      LOG.info(String.format("Received activity: %s", activity));

      TextEditParams editParams = new TextEditParams(workspace, editorManager, activity);

      try {
        ApplyWorkspaceEditResponse r = client.applyEdit(editParams).get(); // TODO: use facade?
        LOG.info(String.format("Edit Result: %b", r.isApplied()));
      } catch (InterruptedException | ExecutionException e1) {
        LOG.error(e1);
      }
    }
  };

  private final ISessionLifecycleListener sessionLifecycleListener = new ISessionLifecycleListener() {

    @Override
    public void sessionStarted(final ISarosSession session) {
      LOG.info("Session started!");
      initialize(session);
    }

    @Override
    public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
      LOG.info("Session ended!");
      uninitialize(session);
    }
  };

  public DocumentServiceStub(EditorManager editorManager, ISarosSessionManager sessionManager,
      ISarosLanguageClient client, IWorkspace workspace) {
    this.editorManager = editorManager;
    this.client = client;
    this.workspace = workspace;

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  protected void uninitialize(ISarosSession session) {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(this.consumer);

    this.session = null;
  }

  protected void initialize(ISarosSession session) {
    session.addActivityProducer(this);
    session.addActivityConsumer(this.consumer, Priority.ACTIVE);

    this.session = session;
  }

  private SPath getSPath(String uri) {
    IPath path;
    try {
      path = LspPath.fromUri(new URI(uri));
    } catch (URISyntaxException e) {
      LOG.error(e);
      return null;
    }
    IProject p = this.workspace.getProject("");
    
    return new SPath(p.getFile(path));
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem i = params.getTextDocument();

    System.out.println(String.format("Opened '%s' (%s, version %d)", i.getUri(), i.getLanguageId(), i.getVersion()));
    this.editorManager.openEditor(this.getSPath(i.getUri()), false);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    VersionedTextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Changed '%s' (version %d)", i.getUri(), i.getVersion()));

    User source = this.session != null ? this.session.getLocalUser() : this.getAnonymousUser();
    
    for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
      SPath path = this.getSPath(i.getUri());
      EditorString content = new EditorString(this.editorManager.getContent(path));
      TextEditActivity activity = new TextEditActivity(source, content.getOffset(changeEvent.getRange().getStart()), changeEvent.getText(), content.substring(changeEvent.getRange().getStart(), changeEvent.getRangeLength()), path);

      this.editorManager.applyTextEdit(activity);
      if(this.session != null) {
        LOG.info(String.format("Sending activity: %s", activity));
        this.fireActivity(activity); //TODO: do here or in editormanager?!
      }
    }
  }

  private User getAnonymousUser() {
    return new User(new JID("anonymous@local.user"), true, true, null);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    TextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Closed '%s'", i.getUri()));

    this.editorManager.closeEditor(this.getSPath(i.getUri()));
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    TextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Saved '%s'", i.getUri()));

    this.editorManager.saveEditor(this.getSPath(i.getUri())); //TODO: selective saving?
  }
}
