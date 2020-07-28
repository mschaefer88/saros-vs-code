package saros.lsp.interaction.session;

import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import saros.communication.extensions.SessionStatusRequestExtension;
import saros.communication.extensions.SessionStatusResponseExtension;
import saros.filesystem.IProject;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

public class SessionStatusRequestHandler {
    private static final Logger LOG = Logger.getLogger(SessionStatusRequestHandler.class);

  private final ISarosSessionManager sessionManager;

  private final IReceiver receiver;

  private final ITransmitter transmitter;

  private final IPreferenceStore preferenceStore;

  private final PacketListener statusRequestListener =
      new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
          Executors.newCachedThreadPool().submit(() -> {
            handleStatusRequest(new JID(packet.getFrom()));
          });
        }
      };

  public SessionStatusRequestHandler(
      ISarosSessionManager sessionManager,
      ITransmitter transmitter,
      IReceiver receiver,
      IPreferenceStore preferenceStore) {
    this.sessionManager = sessionManager;
    this.transmitter = transmitter;
    this.receiver = receiver;
    this.preferenceStore = preferenceStore;

    if (Boolean.getBoolean("saros.server.SUPPORTED")) {
      this.receiver.addPacketListener(
          statusRequestListener, SessionStatusRequestExtension.PROVIDER.getPacketFilter());
    }
  }

  private void handleStatusRequest(JID from) {

    ISarosSession session = sessionManager.getSession();
    SessionStatusResponseExtension response;

    if (session == null) {
      response = new SessionStatusResponseExtension();
    } else {
      // Don't count the server
      int participants = session.getUsers().size() - 1;

      response = new SessionStatusResponseExtension(participants, getSessionDescription(session));
    }

    transmitter.sendPacketExtension(from, SessionStatusResponseExtension.PROVIDER.create(response));
  }

  private String getSessionDescription(ISarosSession session) {
    String description = "Projects: ";

    Set<IProject> projects = session.getProjects();
    int i = 0;
    int numOfProjects = projects.size();

    for (IProject project : projects) {
      description += project.getName();

      if (!session.isCompletelyShared(project)) description += " (partial)";

      if (i < numOfProjects - 1) description += ", ";

      i++;
    }

    if (numOfProjects == 0) {
      description += "none";
    }

    return description;
  }
}