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

  /**
   * Sends a notification that informs the client about a state change of the session, ie. if it's
   * active or not.
   *
   * @param isActive <i>true</i> if session is active, <i>false</i> otherwise
   */
  @JsonNotification("saros/session/state")
  void sendStateSession(SarosResultResponse<Boolean> isActive);

  /**
   * Sends a notification that informs the client about a state change of the XMPP connection, ie.
   * if it's active or not.
   *
   * @param isConnected <i>true</i> if XMPP connection is active, <i>false</i> otherwise
   */
  @JsonNotification("saros/connection/state")
  void sendStateConnected(SarosResultResponse<Boolean> isConnected);

  /**
   * Sends a notification that informs the client about a state change of a contact, eg. online
   * status or saros support.
   *
   * @param contact The contact whose state has changed
   */
  @JsonNotification("saros/contact/state")
  void sendStateContact(ContactDto contact);

  /**
   * Sends a notification that issues the client to open an editor with the file contents.
   *
   * @param path The path as URI (Uniform Resource Identifier) to open
   */
  @JsonNotification("saros/editor/open")
  void openEditor(SarosResultResponse<String> path);

  /**
   * Sends a notification to the client that annotations have changed.
   *
   * @param annotations Annotations of the currently active editor
   */
  @JsonNotification("saros/editor/annotate")
  void sendAnnotation(SarosResultResponse<AnnotationParams[]> annotations);

  /**
   * Sends a notification to the client that a user has joined the currently active session.
   *
   * @param user The user that joined the session
   */
  @JsonNotification("saros/session/user-joined")
  void notifyUserJoinedSession(SessionUserDto user);

  /**
   * Sends a notification to the client that the state of a user that is part of the session has
   * changed.
   *
   * @param user The user whose state has changed
   */
  @JsonNotification("saros/session/user-changed")
  void notifyUserChangedSession(SessionUserDto user);

  /**
   * Sends a notification to the client that a user that is part of the session has left.
   *
   * @param user The user that left the session
   */
  @JsonNotification("saros/session/user-left")
  void notifyUserLeftSession(SessionUserDto user);

  /**
   * Sends a request to the client to inform it about a new progress operation.
   *
   * @param params Details about the progress
   * @return A future without a value
   */
  @JsonRequest("window/workDoneProgress/create")
  CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params);

  /**
   * Sends a notification to the client to inform it about progress changes.
   *
   * @param <T>
   * @param params
   */
  @JsonNotification("$/progress")
  <T> void progress(ProgressParams<T> params);
}
