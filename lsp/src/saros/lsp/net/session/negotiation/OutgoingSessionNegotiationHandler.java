package saros.lsp.interaction.session.negotiation;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.session.ISarosSessionManager;

/** Handler for outgoing session negotiations. */
public class OutgoingSessionNegotiationHandler {
  private ISarosLanguageClient client;
  private ISarosSessionManager sessionManager;

  public OutgoingSessionNegotiationHandler(
      ISarosLanguageClient client, ISarosSessionManager sessionManager) {
    this.client = client;
    this.sessionManager = sessionManager;
  }

  /**
   * Handles the outgoing negotiation.
   * 
   * @param negotiation The outgoing session negotiation
   */
  public void handle(OutgoingSessionNegotiation negotiation) {
    SessionNegotiation.Status status = negotiation.start(new ProgressMonitor(this.client));
    switch (status) {
      case OK:
        sessionManager.startSharingReferencePoints(negotiation.getPeer());
        break;
      case ERROR:
        this.showMessage(negotiation.getErrorMessage(), MessageType.Error);
        break;
      case REMOTE_ERROR:
        this.showMessage(
            negotiation.getErrorMessage() + " at remote: " + negotiation.getPeer(),
            MessageType.Error);
        break;
      case CANCEL:
        this.showMessage("Session negotiation was cancelled locally", MessageType.Info);
        break;
      case REMOTE_CANCEL:
        this.showMessage(
            "Session negotiation was cancelled by remote: " + negotiation.getPeer(),
            MessageType.Warning);
        break;
    }
  }

  /**
   * Sends a message to the client to show it
   * to the user.
   * 
   * @param message The message to show
   * @param type The type of message
   */
  private void showMessage(String message, MessageType type) {

    MessageParams messageParams = new MessageParams();
    messageParams.setType(type);
    messageParams.setMessage(message);

    this.client.showMessage(messageParams);
  }
}
