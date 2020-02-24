package saros.lsp.service;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.TextDocumentService;

import saros.activities.SPath;
import saros.editor.IEditorManager;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.lsp.filesystem.LspWorkspace;
import saros.server.filesystem.ServerPathImpl;

/** Empty implementation of the text document service. */
public class DocumentServiceStub implements TextDocumentService {

  private IEditorManager editorManager;

  public DocumentServiceStub(IEditorManager editorManager) {
    this.editorManager = editorManager;
  }

  // TODO: Own class like ServerPathImpl.fromString(root)
  private String fromUriToPathString(String uri) {// TODO: not null
    // file:///c%3A/Temp/saros-workspace-test/workspace-alice-stf/textX/src/textX/Saros.java
    return uri.replaceAll("[a-z]+:/{3}", "");
  }

  private SPath getSPath(String uri) {
    String path = this.fromUriToPathString(uri);
    IProject p = LspWorkspace.projects.get(0);
    System.out.println("A");
    IFile f = p.getFile(path);

    return new SPath(p, f.getProjectRelativePath());
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem i = params.getTextDocument();
    
    System.out.println(String.format("Opened '%s' (%s, version %d)", path, i.getLanguageId(), i.getVersion()));
    this.editorManager.openEditor(this.getSPath(i.getUri()), true);//TODO: what bool value
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    VersionedTextDocumentIdentifier i = params.getTextDocument();
    System.out.println(String.format("Changed '%s' (version %d)", i.getUri(), i.getVersion()));

    //TODO: how? this.editorManager.
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

    this.editorManager.saveEditors(LspWorkspace.projects.get(0)); //TODO: selective saving?
  }
}
