package saros.lsp.extensions.server.contact;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import saros.lsp.extensions.server.SarosResponse;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;

/** Interface of the account service. */
@JsonSegment("saros/contact")
public interface IContactService {

  @JsonRequest
  CompletableFuture<SarosResponse> add(ContactDto request);

  @JsonRequest
  CompletableFuture<SarosResponse> remove(ContactDto request);

  @JsonRequest
  CompletableFuture<SarosResponse> rename(ContactDto request);

  @JsonRequest
  CompletableFuture<SarosResultResponse<ContactDto[]>> getAll(ContactDto request);
}
