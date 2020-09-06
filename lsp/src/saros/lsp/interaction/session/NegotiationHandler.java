package saros.lsp.interaction.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import saros.filesystem.IWorkspace;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.filesystem.IWorkspacePath;
import saros.lsp.filesystem.LspReferencePoint;
import saros.lsp.filesystem.LspWorkspace;
import saros.lsp.ui.UIInteractionManager;
import saros.filesystem.IReferencePoint;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.ResourceNegotiationData;
import saros.negotiation.SessionNegotiation;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;

// TODO: why not general logic and different for all?
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger LOG = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IProgressMonitor progressMonitor;

  private final ISarosLanguageClient client;

  private final IWorkspacePath workspace;

  private final UIInteractionManager interactionManager;

  private final XMPPContactsService contactService;

  private ExecutorService executor = Executors.newCachedThreadPool();

  public NegotiationHandler(
      final ISarosSessionManager sessionManager,
      final XMPPConnectionService connectionService,
      final IProgressMonitor progressMonitor,
      ISarosLanguageClient client,
      IWorkspacePath workspace,
      final UIInteractionManager interactionManager,
      final XMPPContactsService contactService) {

    sessionManager.setNegotiationHandler(this);

    this.sessionManager = sessionManager;
    this.progressMonitor = new NullProgressMonitor();
    this.client = client;
    this.workspace = workspace;
    this.interactionManager = interactionManager;
    this.contactService = contactService;

    LOG.info(String.format("Negotiation handler registered (%d)", sessionManager.hashCode()));
  }

  @Override
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {
    // TODO Auto-generated method stub
    LOG.info("handleOutgoingSessionNegotiation");

    // TODO: how to enfoce threading on super definition?!
    Executors.newCachedThreadPool()
        .submit(
            () -> {

              // TODO: why here start and somewhere else run?
              SessionNegotiation.Status status =
                  negotiation.start(new NullProgressMonitor()); // TODO: use mine
              LOG.info(status);
              // TODO: LOG to client
              switch (status) {
                case OK:
                  sessionManager.startSharingReferencePoints(negotiation.getPeer());
                  break;
                case ERROR:
                  LOG.error("ERROR running session negotiation: " + negotiation.getErrorMessage());
                  break;
                case REMOTE_ERROR:
                  LOG.error(
                      "REMOTE_ERROR running session negotiation: "
                          + negotiation.getErrorMessage()
                          + " at remote: "
                          + negotiation.getPeer().toString());
                  break;
                case CANCEL:
                  LOG.info("Session negotiation was cancelled locally");
                  break;
                case REMOTE_CANCEL:
                  LOG.info(
                      "Session negotiation was cancelled by remote: "
                          + negotiation.getPeer().toString());
                  break;
              }
            });
  }

  private boolean AskAcceptInvite(JID inviteeJid) {
    Optional<XMPPContact> invitingContact = this.contactService.getContact(inviteeJid.getBase());
    String inviteeName =
        invitingContact.isPresent()
            ? invitingContact.get().getDisplayableName()
            : inviteeJid.getName();

    return this.interactionManager.getUserInputYesNo(
        String.format("'%s' invited you to a session", inviteeName), "Accept?");
  }

  @Override
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {
    // TODO Auto-generated method stub
    LOG.info("handleIncomingSessionNegotiation");
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              if (!this.AskAcceptInvite(negotiation.getPeer())) {
                negotiation.localCancel("Declined", CancelOption.NOTIFY_PEER);
              } else {
                SessionNegotiation.Status status = negotiation.accept(this.progressMonitor);
                switch (status) {
                  case OK:
                    break;
                  case CANCEL:
                  case ERROR:
                    LOG.error(
                        "Remote " + negotiation.getPeer() + ": " + negotiation.getErrorMessage());
                    break;
                  case REMOTE_CANCEL:
                  case REMOTE_ERROR:
                    LOG.error(
                        "Session negotiation was cancelled by remote: "
                            + negotiation.getPeer().toString());
                    break;
                }
              }
            });
  }

  private static String getNickname(JID jid) {
    return XMPPUtils.getNickname(null, jid, jid.getBase());
  }

  private void sendNotification(String message, MessageType type) {

    MessageParams messageParams = new MessageParams();
    messageParams.setType(type);
    messageParams.setMessage(message);

    this.client.showMessage(messageParams);
  }

  @Override
  public void handleOutgoingResourceNegotiation(AbstractOutgoingResourceNegotiation negotiation) {
    // TODO: negotiation.addCancelListener(listener);
    LOG.info("handleOutgoingResourceNegotiation");

    Executors.newCachedThreadPool()
        .submit(
            () -> {
              ResourceNegotiation.Status status = negotiation.run(this.progressMonitor);

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
            });
  }

  @Override
  public void handleIncomingResourceNegotiation(
    AbstractIncomingResourceNegotiation negotiation) { // TODO: logic in own class
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              LOG.info("handleIncomingProjectNegotiation");
              // TODO: negotiation.addCancelListener(listener);

              Map<String, IReferencePoint> projectMapping = new HashMap<>();
              // TODO: use abstraction?
              for (ResourceNegotiationData data : negotiation.getResourceNegotiationData()) {

                for (String file : data.getFileList().getPaths()) {
                  LOG.debug(String.format("File: %s", file));
                }

                String projectName = data.getReferencePointName();
                LOG.info(String.format("Retrieved project '%s'", projectName));
                IReferencePoint project = new LspReferencePoint(this.workspace, projectName);

                // TODO: The file path is currently dictated by the name, potentially resulting in
                // CONFLICTS
                if (!project.exists()) {
                  try {
                    ((LspReferencePoint) project).create();
                  } catch (IOException e) {
                    negotiation.localCancel(
                        "Error creating project folder", CancelOption.NOTIFY_PEER);
                    LOG.error(e);
                    return;
                  }
                }

                LOG.info(String.format("PUT '%s'", data.getReferencePointID()));
                projectMapping.put(data.getReferencePointID(), project);
              }

              LOG.info("RUN");
              //TODO: hier war runnable MIGRATION
              ResourceNegotiation.Status status =
                  negotiation.run(projectMapping, this.progressMonitor); // TODO: cancel

                  LOG.info(String.format("STATUS '%s'", status));
              // TODO: process state
              if (status != ResourceNegotiation.Status.OK) {
                //this.sendNotification("", status); //TODO: !!!!
                // TODO: this.client.openProject(new
                // SarosResultResponse<String>("C:\\Temp\\saros-workspace-test\\"));
              }
            });
  }
}