package saros.lsp.extensions.server.account;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.account.dto.AccountDto;
import saros.lsp.extensions.server.account.dto.AccountIdDto;

/** Interface of the account service. */
@JsonSegment("saros/account")
public interface IAccountService {

  /**
   * Adds a new account.
   *
   * @param request arguments of the request
   * @return response for the request
   */
  @JsonRequest
  CompletableFuture<SarosResponse> add(AccountDto request);

  @JsonRequest
  CompletableFuture<SarosResponse> update(AccountDto request);

  @JsonRequest
  CompletableFuture<SarosResponse> remove(AccountIdDto request);

  @JsonRequest
  CompletableFuture<SarosResponse> setDefault(AccountIdDto request);

  @JsonRequest
  CompletableFuture<SarosResultResponse<AccountDto[]>> getAll();
}
