package saros.lsp.extensions.server.eventhandler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

import saros.filesystem.IFile;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IWorkspace;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.SarosResultResponse;
import saros.lsp.filesystem.LspProject;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingProjectNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.ProjectNegotiationData;
import saros.negotiation.SessionNegotiation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.server.filesystem.ServerProjectImpl;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;

//TODO: why not general logic and different for all?
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger LOG = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IProgressMonitor progressMonitor;

  private final ISarosLanguageClient client;

  private ExecutorService executor = Executors.newCachedThreadPool();

  public NegotiationHandler(
            final ISarosSessionManager sessionManager, final XMPPConnectionService connectionService, 
            final IProgressMonitor progressMonitor, ISarosLanguageClient client, IWorkspace workspace) {

    sessionManager.setNegotiationHandler(this);

    this.sessionManager = sessionManager;
    this.progressMonitor = new NullProgressMonitor();
    this.client = client;
    
    LOG.info(String.format("Negotiation handler registered (%d)", sessionManager.hashCode()));
  }

    @Override
    public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {
        // TODO Auto-generated method stub
        LOG.info("handleOutgoingSessionNegotiation");

        //TODO: how to enfoce threading on super definition?!
        Executors.newCachedThreadPool().submit(() -> {

            //TODO: why here start and somewhere else run?
            SessionNegotiation.Status status =
            negotiation.start(new NullProgressMonitor()); //TODO: use mine
            LOG.info(status);
            //TODO: LOG to client
            switch (status) {
              case OK:
                sessionManager.startSharingProjects(negotiation.getPeer());
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

    @Override
    public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {
        // TODO Auto-generated method stub
        LOG.info("handleIncomingSessionNegotiation");
        Executors.newCachedThreadPool().submit(() -> {
        SessionNegotiation.Status status = negotiation.accept(this.progressMonitor);
        switch (status) {
          case OK:
            break;
          case CANCEL:
          case ERROR:
          LOG.error("Remote " + 
                negotiation.getPeer() + ": " + negotiation.getErrorMessage());
            break;
          case REMOTE_CANCEL:
          case REMOTE_ERROR:
          LOG.error(
            "Session negotiation was cancelled by remote: "
                + negotiation.getPeer().toString());
            break;
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
    public void handleOutgoingProjectNegotiation(AbstractOutgoingProjectNegotiation negotiation) {
        //TODO: negotiation.addCancelListener(listener);
        LOG.info("handleOutgoingProjectNegotiation");
      
        Executors.newCachedThreadPool().submit(() -> {
          ProjectNegotiation.Status status =
            negotiation.run(this.progressMonitor);

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
              this.sendNotification(String.format("%s declined", peerName), MessageType.Warning);
              break;
            case REMOTE_ERROR:
              this.sendNotification(String.format("%s had error '$s'", peerName, negotiation.getErrorMessage()), MessageType.Error);
              break;
          }
        });
    }

    @Override
    public void handleIncomingProjectNegotiation(AbstractIncomingProjectNegotiation negotiation) {//TODO: logic in own class
      Executors.newCachedThreadPool().submit(() -> {
        LOG.info("handleIncomingProjectNegotiation");
        //TODO: negotiation.addCancelListener(listener);

          Map<String, IProject> projectMapping = new HashMap<>();
          //TODO: use abstraction?
          for (ProjectNegotiationData data : negotiation.getProjectNegotiationData()) {

            LOG.debug(String.format("ID: %s, Name: %s", data.getProjectID(), data.getProjectName()));
            for(String file : data.getFileList().getPaths()) {
              LOG.debug(String.format("File: %s", file));
            }
            Map<String,String> add = data.getAdditionalProjectData();
            add.forEach((k,v) -> {
              LOG.debug(String.format("Add: %s = %s", k, v));
            });

            String projectName = data.getProjectName();
            IProject project = new LspProject("C:\\Temp\\saros-workspace-test", projectName);
            
      
            // TODO: The file path is currently dictated by the name, potentially resulting in CONFLICTS
            if (!project.exists()) {
              try {
                project.adaptTo(ServerProjectImpl.class).create();
              } catch (IOException e) {
                negotiation.localCancel(
                    "Error creating project folder", CancelOption.NOTIFY_PEER);
                    LOG.error(e);
                return;
              }
            }
      
            projectMapping.put(data.getProjectID(), project);
          }

          ProjectNegotiation.Status status = negotiation.run(projectMapping, this.progressMonitor); //TODO: cancel
          
          //TODO: process state
          if(status == ProjectNegotiation.Status.OK) {
            //TODO: this.client.openProject(new SarosResultResponse<String>("C:\\Temp\\saros-workspace-test\\"));
          }
        });
    }
}
