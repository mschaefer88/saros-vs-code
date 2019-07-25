package saros.ui.model.roster;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import saros.communication.contact.ContactStatus;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPContact;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.model.ITreeElement;
import saros.ui.model.TreeElement;

/** Wrapper for {@link RosterEntryElement RosterEntryElements} in use with {@link Viewer Viewers} */
public class RosterEntryElement extends TreeElement {

  private final XMPPContact contact;
  private final Roster roster;
  private final JID jid;
  private final boolean hasSarosSupport;

  public RosterEntryElement(Roster roster, XMPPContact contact, boolean hasSarosSupport) {

    this.roster = roster;
    this.contact = contact;
    this.jid = contact.getJid();
    this.hasSarosSupport = hasSarosSupport;
  }

  protected RosterEntry getRosterEntry() {
    if (roster == null) return null;

    return roster.getEntry(jid.getBase());
  }

  @Override
  public StyledString getStyledText() {
    StyledString styledString = new StyledString(contact.getDisplayableName());

    ContactStatus status = contact.getStatus();
    switch (status.getType()) {
      case SUBSCRIPTION_CANCELED:
        styledString
            .append(" ")
            .append(Messages.RosterEntryElement_subscription_canceled, StyledString.COUNTER_STYLER);
        break;
      case SUBSCRIPTION_PENDING:
        styledString
            .append(" ")
            .append(Messages.RosterEntryElement_subscription_pending, StyledString.COUNTER_STYLER);
        break;
      case AWAY:
        status
            .getMessage()
            .ifPresent(
                message -> styledString.append(" (" + message + ")", StyledString.COUNTER_STYLER));
        break;
      default:
        break;
    }

    return styledString;
  }

  @Override
  public Image getImage() {
    boolean sarosSupported = isSarosSupported();

    ContactStatus status = contact.getStatus();
    switch (status.getType()) {
      case AVAILABLE:
        return sarosSupported ? ImageManager.ICON_CONTACT_SAROS_SUPPORT : ImageManager.ICON_CONTACT;
      case AWAY:
        return sarosSupported ? ImageManager.ICON_USER_SAROS_AWAY : ImageManager.ICON_CONTACT_AWAY;
      default:
        return ImageManager.ICON_CONTACT_OFFLINE;
    }
  }

  public boolean isOnline() {
    return contact.getStatus().isOnline();
  }

  public JID getJID() {
    return jid;
  }

  public XMPPContact getContact() {
    return contact;
  }

  public boolean isSarosSupported() {
    return hasSarosSupport;
  }

  @Override
  public ITreeElement getParent() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof RosterEntryElement)) {
      return false;
    }

    RosterEntryElement rosterEntryElement = (RosterEntryElement) obj;
    return (jid == null ? rosterEntryElement.jid == null : jid.equals(rosterEntryElement.jid));
  }

  @Override
  public int hashCode() {
    return (jid != null) ? jid.hashCode() : 0;
  }
}
