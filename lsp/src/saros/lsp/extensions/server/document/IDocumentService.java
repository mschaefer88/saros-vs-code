package saros.lsp.extensions.server.document;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.TextDocumentService;
import saros.lsp.extensions.client.dto.AnnotationParams;
import saros.lsp.extensions.server.SarosResultResponse;

@JsonSegment("textDocument")
public interface IDocumentService extends TextDocumentService {
  @JsonRequest
  CompletableFuture<SarosResultResponse<AnnotationParams[]>> getAnnotations(
      TextDocumentIdentifier identifier);
}
