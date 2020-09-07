package saros.lsp.extensions.client;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import saros.lsp.extensions.client.dto.AnnotationParams;
import saros.lsp.extensions.client.dto.ProgressParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressCreateParams;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.session.dto.SessionUserDto;

/**
 * Interface of the Saros language client.
 *
 * <p>The language client is being used to interact with the connected client.
 *
 * <p>All client features that aren't covered by the lsp protocol have to be specified here.
 */
public interface ISarosLanguageClient extends LanguageClient {

  @JsonNotification("saros/session/state")
  void sendStateSession(SarosResultResponse<Boolean> isActive);

  @JsonNotification("saros/connected")
  void sendStateConnected(SarosResultResponse<Boolean> isConnected);

  @JsonNotification("saros/contact/state")
  void sendStateContact(ContactDto r);

  @JsonNotification("saros/editor/open")
  void openEditor(SarosResultResponse<String> path);

  @JsonNotification("saros/editor/annotate")
  void sendAnnotation(SarosResultResponse<AnnotationParams[]> annotations);

  @JsonNotification("saros/session/user-joined")
  void notifyUserJoinedSession(SessionUserDto user);

  @JsonNotification("saros/session/user-changed")
  void notifyUserChangedSession(SessionUserDto user);

  @JsonNotification("saros/session/user-left")
  void notifyUserLeftSession(SessionUserDto user);

  @JsonRequest("window/workDoneProgress/create")
  CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params);

  @JsonNotification("$/progress")
  <T> void progress(ProgressParams<T> params);
}
