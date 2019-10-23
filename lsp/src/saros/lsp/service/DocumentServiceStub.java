package saros.lsp.service;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.services.TextDocumentService;

/** Empty implementation of the text document service. */
public class DocumentServiceStub implements TextDocumentService {

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    System.out.println("didOpen");
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    System.out.println("didChange");
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    System.out.println("didClose");
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    System.out.println("didSave");
  }
}
