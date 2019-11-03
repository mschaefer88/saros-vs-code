package saros.lsp.extensions.server.eventhandler;

import java.text.MessageFormat;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import saros.monitoring.NullProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.AbstractOutgoingProjectNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.SessionNegotiation;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;

//TODO: check logic with eclipse!
//TODO: why not general logic and different for all?
public class NegotiationHandler implements INegotiationHandler {

  private static final Logger LOG = Logger.getLogger(NegotiationHandler.class);

  private final ISarosSessionManager sessionManager;

  public NegotiationHandler(
            final ISarosSessionManager sessionManager, final XMPPConnectionService connectionService) {

    sessionManager.setNegotiationHandler(this);

    this.sessionManager = sessionManager;
    
    LOG.info(String.format("Negotiation handler registered (%d)", sessionManager.hashCode()));
  }

    @Override
    public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {
        // TODO Auto-generated method stub
        LOG.info("handleOutgoingSessionNegotiation");

        Executors.newCachedThreadPool().submit(() -> {

            //TODO: why here start and somewhere else run?
            SessionNegotiation.Status status =
            negotiation.start(new NullProgressMonitor()); //TODO: use mine

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
    }

    @Override
    public void handleOutgoingProjectNegotiation(AbstractOutgoingProjectNegotiation negotiation) {
        // TODO Auto-generated method stub
        LOG.info("handleOutgoingProjectNegotiation");

        //TODO: why here async and not in calling class?
        Executors.newCachedThreadPool().submit(() -> {
            LOG.info("enter");
            ProjectNegotiation.Status status = 
            negotiation.run(new NullProgressMonitor()); //TODO: use mine

            LOG.info("run");
            if (status != ProjectNegotiation.Status.OK) //TODO: inject client?
                LOG.error(String.format("NEGOTIATION ERROR: status = '%s' error = '%s'", status, negotiation.getErrorMessage()));
                              //handleErrorStatus(status, negotiation.getErrorMessage(), negotiation.getPeer());

                              LOG.info("leave");
              return null;
        });
    }

    @Override
    public void handleIncomingProjectNegotiation(AbstractIncomingProjectNegotiation negotiation) {
        // TODO Auto-generated method stub
        LOG.info("handleIncomingProjectNegotiation");
    }
}
