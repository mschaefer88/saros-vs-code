package saros.lsp.interaction.session.negotiation;

import java.util.Optional;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;
import saros.lsp.ui.UIInteractionManager;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.SessionNegotiation;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

public class IncomingSessionNegotiationHandler {
  private XMPPContactsService contactService;
  private ISarosLanguageClient client;
  private UIInteractionManager interactionManager;

  public IncomingSessionNegotiationHandler(
      final XMPPContactsService contactService,
      final ISarosLanguageClient client,
      final UIInteractionManager interactionManager) {
    this.contactService = contactService;
    this.client = client;
    this.interactionManager = interactionManager;
  }

  public void handle(IncomingSessionNegotiation negotiation) {
    if (!this.AskAcceptInvite(negotiation)) {
      negotiation.localCancel("Declined", CancelOption.NOTIFY_PEER);
    } else {
      SessionNegotiation.Status status = negotiation.accept(new ProgressMonitor(this.client));
      switch (status) {
        case OK:
          break;
        case CANCEL:
        case ERROR:
          this.sendNotification(
              negotiation.getErrorMessage() + " at remote: " + negotiation.getPeer(),
              MessageType.Error);
          break;
        case REMOTE_CANCEL:
        case REMOTE_ERROR:
          this.sendNotification(
              "Session negotiation was cancelled by remote: " + negotiation.getPeer().toString(),
              MessageType.Warning);
          break;
      }
    }
  }

  private void sendNotification(String message, MessageType type) {

    MessageParams messageParams = new MessageParams();
    messageParams.setType(type);
    messageParams.setMessage(message);

    this.client.showMessage(messageParams);
  }

  private boolean AskAcceptInvite(IncomingSessionNegotiation negotiation) {
    JID peer = negotiation.getPeer();
    Optional<XMPPContact> invitingContact = this.contactService.getContact(peer.getBase());
    String inviteeName =
        invitingContact.isPresent()
            ? invitingContact.get().getDisplayableName()
            : peer.getName();
    String sessionDescription = negotiation.getDescription();

    return this.interactionManager.getUserInputYesNo(
        String.format("'%s' invited you to the session '%s'", inviteeName, sessionDescription), "Accept?");
  }
}
