package saros.lsp.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Position;
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
import saros.editor.IEditorManager;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.lsp.commons.TextDocument;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.filesystem.LspWorkspace;
import saros.server.filesystem.ServerPathImpl;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;
import saros.session.IActivityConsumer.Priority;

/** Empty implementation of the text document service. */
public class DocumentServiceStub extends AbstractActivityProducer implements TextDocumentService {

  private IEditorManager editorManager;
  private ISarosSession session;

  private final Map<String, TextDocument> documents = new HashMap<String, TextDocument>();
  private final ISarosLanguageClient client;

  private static final Logger LOG = Logger.getLogger(DocumentServiceStub.class);

  private final AbstractActivityConsumer consumer = new AbstractActivityConsumer() {
    @Override
    public void receive(TextEditActivity textEditActivity) {
      super.receive(textEditActivity);

      String uri = "file:///c%3A/Temp/saros-workspace-test/workspace-alice-stf/textX/src/textX/Saros.java";
      TextDocument target = documents.get(uri);//TODO: better SPath as key

      ApplyWorkspaceEditParams p = new ApplyWorkspaceEditParams();
      LOG.info(textEditActivity);
      int offset = textEditActivity.getOffset();
      TextEdit te = new TextEdit();
      te.setNewText(textEditActivity.getText());
      te.setRange(new Range(target.positionAt(offset),
          target.positionAt(offset + textEditActivity.getReplacedText().length())));
      TextDocumentEdit tde = new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, 1),//TODO: get from document
          Collections.singletonList(te));
      WorkspaceEdit e = new WorkspaceEdit(Collections.singletonList(Either.forLeft(tde)));
      
      p.setEdit(e);

      try {
        ApplyWorkspaceEditResponse r = client.applyEdit(p).get();
        LOG.info(String.format("Edit Result: %b", r.isApplied()));
      } catch (InterruptedException | ExecutionException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
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

  public DocumentServiceStub(IEditorManager editorManager, ISarosSessionManager sessionManager, ISarosLanguageClient client) {
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

    return new SPath(p, f.getProjectRelativePath());
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem i = params.getTextDocument();

    this.documents.put(i.getUri(), new TextDocument(i.getText(), i.getUri()));

    System.out.println(String.format("Opened '%s' (%s, version %d)", i.getUri(), i.getLanguageId(), i.getVersion()));
    this.editorManager.openEditor(this.getSPath(i.getUri()), true);// TODO: what bool value
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    VersionedTextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Changed '%s' (version %d)", i.getUri(), i.getVersion()));

    List<TextEditActivity> activities = 
      this.documents.get(i.getUri()).apply(params.getContentChanges(), 
        this.session == null ? null : this.session.getLocalUser(),//TODO: do better!
        this.getSPath(i.getUri()));    

    if(this.session != null) {
      activities.forEach(activity -> this.fireActivity(activity));
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    TextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Closed '%s'", i.getUri()));

    this.documents.remove(i.getUri());

    this.editorManager.closeEditor(this.getSPath(i.getUri()));
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    TextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Saved '%s'", i.getUri()));

    this.editorManager.saveEditors(LspWorkspace.projects.get(0)); //TODO: selective saving?
  }
}
