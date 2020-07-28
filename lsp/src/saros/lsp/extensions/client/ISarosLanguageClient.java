package saros.lsp.extensions.client;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

import saros.lsp.extensions.client.dto.AnnotationParams;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.extensions.server.contact.dto.ContactDto;
import saros.lsp.extensions.server.session.dto.SessionUserDto;

/** 
 * Interface of the Saros language client.
 * 
 * The language client is being used to
 * interact with the connected client.
 * 
 * All client features that aren't covered by
 * the lsp protocol have to be specified here.
 */
public interface ISarosLanguageClient extends LanguageClient {     //TODO: adapter/bridge for better function access aka openProject(string) etc.

    @JsonNotification("saros/session/state") //TODO: naming!
    void sendStateSession(SarosResultResponse<Boolean> r); //TODO: use own notification/type!

    @JsonNotification("saros/session/connected") //TODO: naming!
    void sendStateConnected(SarosResultResponse<Boolean> r); //TODO: use own notification/type!

    @JsonNotification("saros/contact/state") //TODO: naming!
    void sendStateContact(ContactDto r); //TODO: is Contact here really necessary? Void?

    @JsonNotification("saros/editor/open") //TODO: request? what if couldn't open?
    void openEditor(SarosResultResponse<String> path); //TODO: own dto

    @JsonNotification("saros/editor/annotate") //TODO: or request?
    void sendAnnotation(SarosResultResponse<AnnotationParams[]> annotations);

    @JsonNotification("saros/session/user-joined")
    void notifyUserJoinedSession(SessionUserDto user);

    @JsonNotification("saros/session/user-left")
    void notifyUserLeftSession(SessionUserDto user); //TODO: use own notification/type!

    /**
     * Do seperation like: ?
     * @JsonDelegate
  IAccountService getSarosAccountService();
     */
}
