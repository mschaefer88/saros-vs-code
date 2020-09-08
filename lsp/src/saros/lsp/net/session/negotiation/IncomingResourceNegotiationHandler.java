package saros.lsp.interaction.session.negotiation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import saros.filesystem.IReferencePoint;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.filesystem.IWorkspacePath;
import saros.lsp.filesystem.LspReferencePoint;
import saros.lsp.monitoring.ProgressMonitor;
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.ResourceNegotiationData;

/** Handler for incoming resource negotiations. */
public class IncomingResourceNegotiationHandler {

  private static final Logger LOG = Logger.getLogger(IncomingResourceNegotiationHandler.class);

  private final IWorkspacePath workspacePath;
  private final ISarosLanguageClient client;

  public IncomingResourceNegotiationHandler(
      final IWorkspacePath workspacePath, final ISarosLanguageClient client) {
    this.workspacePath = workspacePath;
    this.client = client;
  }

  /**
   * Handles the incoming negotiation.
   * 
   * @param negotiation The incoming resource negotiation
   */
  public void handle(final AbstractIncomingResourceNegotiation negotiation) {

    final Map<String, IReferencePoint> projectMapping = new HashMap<>();
    for (final ResourceNegotiationData data : negotiation.getResourceNegotiationData()) {

      final String projectName = data.getReferencePointName();
      final IReferencePoint project = new LspReferencePoint(this.workspacePath, projectName);

      if (!project.exists()) {
        try {
          ((LspReferencePoint) project).create();
        } catch (final IOException e) {
          negotiation.localCancel("Error creating project folder", CancelOption.NOTIFY_PEER);
          LOG.error(e);
          return;
        }
      }

      projectMapping.put(data.getReferencePointID(), project);
    }

    final ResourceNegotiation.Status status =
        negotiation.run(projectMapping, new ProgressMonitor(this.client));
    if (status != ResourceNegotiation.Status.OK) {
      this.showMessage(negotiation.getErrorMessage(), MessageType.Error);
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
