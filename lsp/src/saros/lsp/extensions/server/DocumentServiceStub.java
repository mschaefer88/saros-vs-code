package saros.lsp.extensions.server;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Color;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IWorkspace;
import saros.lsp.activity.TextEditParams;
import saros.lsp.editor.Editor;
import saros.lsp.editor.EditorManager;
import saros.lsp.editor.adapter.EditorString;
import saros.lsp.editor.annotation.Annotation;
import saros.lsp.editor.annotation.AnnotationManager;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.dto.AnnotationParams;
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

  private final Map<SPath, TextEditActivity> ignore = new HashMap<>();

  private Object lock = new Object();

  private final IActivityConsumer consumer = new AbstractActivityConsumer() {
    @Override
    public void receive(TextEditActivity activity) {
      // super.receive(activity); TODO: used?

      LOG.info(String.format("Received activity: %s", activity));

      synchronized (lock) {
        LOG.info(String.format("Executing activity: %s", activity));
        TextEditParams editParams = new TextEditParams(workspace, editorManager, activity);

        editParams.getEdit().getDocumentChanges().forEach(e -> {
          String uri = e.getLeft().getTextDocument().getUri();

          LOG.info(String.format("Add '%s' to ignore", uri));
          ignore.put(getSPath(uri), activity);
        });

        ApplyWorkspaceEditResponse r;
        try {
          r = client.applyEdit(editParams).get();
          if(!r.isApplied()) {
            LOG.info(String.format("Edit Result: %b", r.isApplied()));
            editParams.getEdit().getDocumentChanges().forEach(e -> {
              String uri = e.getLeft().getTextDocument().getUri();
              
              LOG.info(String.format("Remove '%s' from ignore", uri));
              ignore.remove(getSPath(uri));
            });
          }
        } catch (InterruptedException | ExecutionException e1) {
          LOG.error(e1);
        }        
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

  private final AnnotationManager annotationManager;

  public DocumentServiceStub(EditorManager editorManager, ISarosSessionManager sessionManager,
      ISarosLanguageClient client, IWorkspace workspace, AnnotationManager annotationManager) {
    this.editorManager = editorManager;
    this.client = client;
    this.workspace = workspace;
    this.annotationManager = annotationManager;

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

    TextEditActivity ig = null;
    SPath docId = this.getSPath(i.getUri());
    if(ignore.containsKey(docId)) {
      ig = ignore.get(docId);
      ignore.remove(docId);
    }

    
    System.out.println(String.format("Changed '%s' (version %d)", i.getUri(), i.getVersion()));

    User source = this.session != null ? this.session.getLocalUser() : this.getAnonymousUser();

    if(ig != null) {
      source = ig.getSource();
    }
    
    this.editorManager.setVersion(this.getSPath(i.getUri()), i.getVersion());
    for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
      SPath path = this.getSPath(i.getUri());
      EditorString content = new EditorString(this.editorManager.getContent(path));
      TextEditActivity activity = new TextEditActivity(source, content.getOffset(changeEvent.getRange().getStart()), changeEvent.getText(), content.substring(changeEvent.getRange().getStart(), changeEvent.getRangeLength()), path);

      this.editorManager.applyTextEdit(activity);      

      if(this.session != null && ig == null) {
        LOG.info(String.format("Sending activity: %s", activity));
        this.fireActivity(activity); //TODO: do here or in editormanager?!
      }
    }


    if(this.session != null) { //TODO: do in consume in top?
      
      SPath p = this.getSPath(i.getUri());
      Editor editor = this.editorManager.getEditor(p);
      Annotation[] annotations = editor.getAnnotations();
      AnnotationParams[] aps = Arrays.stream(annotations).map(a -> new AnnotationParams(a, this.workspace, p)).toArray(size -> new AnnotationParams[size]);
      client.sendAnnotation(new SarosResultResponse<AnnotationParams[]>(aps));//TODO: wording apply? 
    }
  
  //LOG.info(String.format("Content after change: \n\n'%s'\n\n", this.editorManager.getContent(this.getSPath(i.getUri()))));
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

  @Override
  public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {//TODO: check URI!
    
    Position r = position.getPosition();

    SPath p = this.getSPath(position.getTextDocument().getUri());
      Editor editor = this.editorManager.getEditor(p);
      Annotation[] annotations = editor.getAnnotations();


    for (Annotation annotation : annotations) {

      Position start = annotation.getRange().getStart();
      Position end = annotation.getRange().getEnd();

      if((r.getLine() == start.getLine() && r.getCharacter() >= start.getCharacter())
        || (r.getLine() == end.getLine() && r.getCharacter() <= end.getCharacter())
        || (r.getLine() > start.getLine() && r.getLine() < end.getLine())) {

          Hover h = new Hover();
          MarkupContent c = new MarkupContent();
          
          c.setKind(MarkupKind.MARKDOWN);
          c.setValue(String.format("Edited by `%s`", "Michael Schäfer"));

          h.setRange(annotation.getRange());
          h.setContents(c);          

          return CompletableFuture.completedFuture(h);
      }      
    }    

    return CompletableFuture.completedFuture(null);
	}

  @Override
  public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
    
    List<CodeLens> lenses = new ArrayList<>();

    SPath p = this.getSPath(params.getTextDocument().getUri());
      Editor editor = this.editorManager.getEditor(p);
      Annotation[] annotations = editor.getAnnotations();
      LOG.info(String.format("codeLens... (%d)", annotations.length));

    for (Annotation annotation : annotations) {
      CodeLens cl = new CodeLens();
      cl.setRange(annotation.getRange());
      cl.setData("TEST");

      Command c = new Command();
      c.setTitle("Michael Schäfer");
      cl.setCommand(c); 
      
      lenses.add(cl);
    }
    

    return CompletableFuture.completedFuture(lenses);
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
    LOG.info("RESOLVE");
		CodeLens cl = new CodeLens();
    cl.setRange(new Range(new Position(4, 4), new Position(4, 10)));
    cl.setData("TEST");

    Command c = new Command();
    c.setTitle("LOCAL USER");
    cl.setCommand(c);
    
    return CompletableFuture.completedFuture(cl);
	}
}
