package saros.communication.contact;

import java.util.Optional;

/**
 * Represents the current status of a contact. A status is mainly defined by a {@link
 * ContactStatus.TYPE} provided via {@link #getType()} and can have a message {@link #getMessage()}.
 *
 * <p>To create a status use create methods e. g. {@link #createAvailable()} which cache objects for
 * cases without a message.
 */
public class ContactStatus {

  public enum TYPE {
    AVAILABLE,
    AWAY,
    SUBSCRIPTION_CANCELED,
    SUBSCRIPTION_PENDING,
    OFFLINE
  }

  private static ContactStatus TYPE_AVAILABLE = new ContactStatus(TYPE.AVAILABLE);
  private static ContactStatus TYPE_SUBSCRIPTION_CANCELED =
      new ContactStatus(TYPE.SUBSCRIPTION_CANCELED);
  private static ContactStatus TYPE_SUBSCRIPTION_PENDING =
      new ContactStatus(TYPE.SUBSCRIPTION_PENDING);
  private static ContactStatus TYPE_OFFLINE = new ContactStatus(TYPE.OFFLINE);

  public static ContactStatus createAvailable() {
    return TYPE_AVAILABLE;
  }

  public static ContactStatus createAway(String message) {
    return new ContactStatus(TYPE.AWAY, message);
  }

  public static ContactStatus createOffline() {
    return TYPE_OFFLINE;
  }

  public static ContactStatus createSubscriptionCanceled() {
    return TYPE_SUBSCRIPTION_CANCELED;
  }

  public static ContactStatus createSubscriptionPending() {
    return TYPE_SUBSCRIPTION_PENDING;
  }

  private final TYPE status;
  private final String message;

  private ContactStatus(TYPE status) {
    this(status, null);
  }

  private ContactStatus(TYPE status, String message) {
    this.status = status;
    this.message = message;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof ContactStatus)) return false;
    ContactStatus other = (ContactStatus) obj;
    if (message == null) {
      if (other.message != null) return false;
    } else if (!message.equals(other.message)) return false;
    if (status != other.status) return false;
    return true;
  }

  public Optional<String> getMessage() {
    return Optional.ofNullable(message);
  }

  public TYPE getType() {
    return status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((message == null) ? 0 : message.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  /**
   * Returns if contact is online.
   *
   * @return true if status is {@link TYPE#AVAILABLE} or {@link TYPE#AWAY}
   */
  public boolean isOnline() {
    if (status == TYPE.AVAILABLE || status == TYPE.AWAY) return true;
    return false;
  }

  @Override
  public String toString() {
    return "Presence [mode=" + status + ", message=" + message + "]";
  }
}
