package saros.lsp.interaction.session.negotiation;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.ResourceNegotiation;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;

public class OutgoingResourceNegotiationHandler {

    private ISarosLanguageClient client;

    public OutgoingResourceNegotiationHandler(ISarosLanguageClient client) {
        this.client = client;
  
    }

    private static String getNickname(JID jid) {
      return XMPPUtils.getNickname(null, jid, jid.getBase());
    }

    public void handle(AbstractOutgoingResourceNegotiation negotiation) {
        ResourceNegotiation.Status status = negotiation.run(new ProgressMonitor(this.client));

              String peerName = getNickname(new JID(negotiation.getPeer().getBase()));

              switch (status) {
                case CANCEL:
                  this.sendNotification("Cancelled", MessageType.Warning);
                  break;
                case ERROR:
                  this.sendNotification(negotiation.getErrorMessage(), MessageType.Error);
                  break;
                case OK:
                  break;
                case REMOTE_CANCEL:
                  this.sendNotification(
                      String.format("%s declined", peerName), MessageType.Warning);
                  break;
                case REMOTE_ERROR:
                  this.sendNotification(
                      String.format("%s had error '$s'", peerName, negotiation.getErrorMessage()),
                      MessageType.Error);
                  break;
              }
    }

    private void sendNotification(String message, MessageType type) {

        MessageParams messageParams = new MessageParams();
        messageParams.setType(type);
        messageParams.setMessage(message);
    
        this.client.showMessage(messageParams);
      } 
}
