package saros.lsp.interaction;

import java.util.Optional;
import org.apache.log4j.Logger;
import saros.lsp.ui.UIInteractionManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.net.xmpp.subscription.SubscriptionHandler;
import saros.net.xmpp.subscription.SubscriptionListener;

/**
 * A component which automatically authorizes all incoming presence subscription requests from
 * contacts.
 */
public class SubscriptionAuthorizer implements SubscriptionListener {

  private static final Logger LOG = Logger.getLogger(SubscriptionAuthorizer.class);

  private SubscriptionHandler subscriptionHandler;
  private XMPPContactsService contactsService;
  private UIInteractionManager interactionManager;

  /**
   * Initializes the SubscriptionAuthorizer.
   *
   * @param subscriptionHandler the subscription handler to use
   */
  public SubscriptionAuthorizer(
      SubscriptionHandler subscriptionHandler,
      XMPPContactsService contactsService,
      UIInteractionManager interactionManager) {
    this.subscriptionHandler = subscriptionHandler;
    this.contactsService = contactsService;
    this.interactionManager = interactionManager;
    subscriptionHandler.addSubscriptionListener(this);
  }

  @Override
  public void subscriptionRequestReceived(JID jid) {
    LOG.info(String.format("JID: %s", jid));
    String title = String.format("User '%s' requested subscription", jid.getName());
    String message = String.format("Allow subscription from '%s'?", jid.getBase());
    LOG.info(String.format("title: %s", title));
    LOG.info(String.format("message: %s", message));

    if (this.interactionManager.getUserInputYesNo(title, message)) {
      subscriptionHandler.addSubscription(jid, true);
    }
  }

  @Override
  public void subscriptionCanceled(JID jid) {
    subscriptionHandler.removeSubscription(jid);
    Optional<XMPPContact> contactResult = this.contactsService.getContact(jid.getBase());
    if (contactResult.isPresent()) {
      XMPPContact contact = contactResult.get();
      String title =
          String.format("User '%s' cancelled subscription", contact.getDisplayableName());
      String message =
          String.format("Remove user '%s' from contact list?", contact.getDisplayableName());

      if (this.interactionManager.getUserInputYesNo(title, message)) {
        this.contactsService.removeContact(contact);
      }
    }
  }
}