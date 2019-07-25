package saros.net.xmpp;

import java.util.Objects;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import saros.communication.contact.ContactStatus;
import saros.net.util.XMPPUtils;

/**
 * Currently each XMPPContact represents a {@code JID} provided by Smack. This will change to
 * represent a whole Contact on the {@code org.jivesoftware.smack.Roster} including all its
 * Presence, Feature Supports, etc. Information.
 *
 * <p>This class is likely to implement a generic Contact Interface in the future.
 */
public class XMPPContact {

  /**
   * Access to XMPP service, available before XMPP Connection establishment, set by {@link
   * #setConnectionService(XMPPConnectionService)}
   *
   * <p>Should be removed after finishing a Contact Handler.
   */
  private static volatile XMPPConnectionService connectionService;

  /**
   * Called only by {@code XMPPConnectionService}.
   *
   * @param connectionService The {@code XMPPConnectionService} Singleton
   */
  protected static void setConnectionService(XMPPConnectionService connectionService) {
    XMPPContact.connectionService = connectionService;
  }

  private final JID jid;

  public XMPPContact(JID jid) {
    this.jid = Objects.requireNonNull(jid);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof XMPPContact)) return false;
    XMPPContact other = (XMPPContact) obj;
    if (jid == null) {
      if (other.jid != null) return false;
    } else if (!jid.equals(other.jid)) return false;
    return true;
  }

  /**
   * If available returns the Roster provided Nickname, alternatively the base JID of this Contact.
   *
   * @return String containing Nickname or JID of this Contact
   */
  public String getDisplayableName() {
    return XMPPUtils.getNickname(connectionService, jid, jid.getBase());
  }

  /**
   * Get the combination of Nickname (if available) and base JID of this Contact.
   *
   * @return String if nickname available in format "Nickname (base JID)" otherwise just base JID
   */
  public String getDisplayableNameLong() {
    String nickname = XMPPUtils.getNickname(connectionService, jid, null);
    if (nickname == null || nickname.isEmpty()) return jid.getBase().toString();
    return String.format("%s (%s)", nickname, jid.getBase().toString());
  }

  /**
   * Get the latest available status information.
   *
   * @return current {@link ContactStatus}
   */
  public ContactStatus getStatus() {
    Roster roster = connectionService.getRoster();
    if (roster == null) return ContactStatus.createOffline();

    Presence presence = roster.getPresence(jid.getBase());
    RosterEntry rosterEntry = roster.getEntry(jid.getBase());

    if (rosterEntry.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING) {
      return ContactStatus.createSubscriptionPending();
    } else if (rosterEntry.getType() == ItemType.none || rosterEntry.getType() == ItemType.from) {
      /* see http://xmpp.org/rfcs/rfc3921.html chapter 8.2.1, 8.3.1 and 8.6 */
      return ContactStatus.createSubscriptionCanceled();
    } else if (!presence.isAvailable()) {
      return ContactStatus.createOffline();
    } else if (presence.isAway()) {
      return ContactStatus.createAway(presence.getMode().toString());
    }

    return ContactStatus.createAvailable();
  }

  /**
   * This method should be avoided in future use outside of {@link saros.net.xmpp}
   *
   * @return JID of this Contact
   */
  public JID getJid() {
    return jid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((jid == null) ? 0 : jid.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "XMPPContact [jid=" + jid + "]";
  }
}
