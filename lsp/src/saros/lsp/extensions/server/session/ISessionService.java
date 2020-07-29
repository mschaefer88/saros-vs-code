package saros.lsp.extensions.server.session;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.session.dto.InviteDto;

/** Interface of the account service. */
@JsonSegment("saros/session")
public interface ISessionService {

  @JsonRequest
  CompletableFuture<SarosResponse> connect();

  @JsonRequest
  CompletableFuture<SarosResponse> disconnect();

  @JsonRequest
  CompletableFuture<SarosResponse> invite(InviteDto invite);

  @JsonRequest
  CompletableFuture<SarosResultResponse<Boolean>> status();

  @JsonRequest
  CompletableFuture<SarosResponse> start();

  @JsonRequest
  CompletableFuture<SarosResponse> stop();
}
