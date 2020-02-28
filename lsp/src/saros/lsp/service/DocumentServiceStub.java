package saros.lsp.service;

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
import saros.filesystem.IProject;
import saros.lsp.adapter.EditorString;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.editor.EditorManager;
import saros.lsp.filesystem.LspWorkspace;
import saros.net.xmpp.JID;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
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

  private static final Logger LOG = Logger.getLogger(DocumentServiceStub.class);

  private final AbstractActivityConsumer consumer = new AbstractActivityConsumer() {
    @Override
    public void receive(TextEditActivity activity) {
      super.receive(activity);

      LOG.info(activity);

      String uri = "file:///" + activity.getPath().getFullPath().toString();

      EditorString content = new EditorString(editorManager.getContent(activity.getPath()));      

      LOG.info(String.format("saros::URI: '%s'", uri));

      ApplyWorkspaceEditParams workspaceEditParams = new ApplyWorkspaceEditParams();
      
      int offset = activity.getOffset();

      TextEdit edit = new TextEdit();
      edit.setNewText(activity.getText());
      edit.setRange(new Range(content.getPosition(offset), content.getPosition(offset + activity.getReplacedText().length())));

      TextDocumentEdit documentEdit = new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, editorManager.getVersion(activity.getPath())),
          Collections.singletonList(edit));
      WorkspaceEdit e = new WorkspaceEdit(Collections.singletonList(Either.forLeft(documentEdit)));
      
      workspaceEditParams.setEdit(e);
      workspaceEditParams.setLabel(activity.getSource().toString());

      try {
        ApplyWorkspaceEditResponse r = client.applyEdit(workspaceEditParams).get(); //TODO: use facade?
        LOG.info(String.format("Edit Result: %b", r.isApplied()));
      } catch (InterruptedException | ExecutionException e1) {
        LOG.error(e1);
      }
    }
  };

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


  public DocumentServiceStub(EditorManager editorManager, ISarosSessionManager sessionManager, ISarosLanguageClient client) {
    this.editorManager = editorManager;
    this.client = client;

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }

  protected void uninitialize(ISarosSession session) {
    session.removeActivityProducer(this);
    session.removeActivityConsumer(this.consumer);

    this.session = session;
  }

  protected void initialize(ISarosSession session) {
    session.addActivityProducer(this);
    session.addActivityConsumer(this.consumer, Priority.ACTIVE);

    this.session = null;
  }

  // TODO: Own class like ServerPathImpl.fromString(root)
  private String fromUriToPathString(String uri) {// TODO: not null
    // file:///c%3A/Temp/saros-workspace-test/workspace-alice-stf/textX/src/textX/Saros.java
    return uri.replaceAll("[a-z]+:/{3}.*?saros-workspace-test/", "");
  }

  private SPath getSPath(String uri) {
    String path = this.fromUriToPathString(uri);
    IProject p = LspWorkspace.projects.get(0);
    IFile f = p.getFile(path);

    //TODO: f.getProjectRelativePath() has error (doubled folder)
    return new SPath(p, f.getFullPath());
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
