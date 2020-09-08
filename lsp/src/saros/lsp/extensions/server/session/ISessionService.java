package saros.lsp.extensions.server.session;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.session.dto.InviteInput;

/** 
 * Interface of the session service that is responsible for 
 * everything session related.
 */
@JsonSegment("saros/session")
public interface ISessionService {

  /**
   * Invites a contact to the currently active session and
   * starts one if currently none is active.
   * 
   * @param input The invitation target 
   * @return A future with a result indicating if the request
   * has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> invite(InviteInput input);

  /**
   * Starts a new Saros session.
   * 
   * @return A future with a result indicating if the request
   * has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> start();

  /**
   * Stops the currently active Saros session.
   * 
   * @return A future with a result indicating if the request
   * has been succesfull or not
   */
  @JsonRequest
  CompletableFuture<SarosResponse> stop();
}
