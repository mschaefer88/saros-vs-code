package saros.lsp.net.session;

import java.util.concurrent.Executors;
import saros.lsp.net.session.negotiation.IncomingResourceNegotiationHandler;
import saros.lsp.net.session.negotiation.IncomingSessionNegotiationHandler;
import saros.lsp.net.session.negotiation.OutgoingResourceNegotiationHandler;
import saros.lsp.net.session.negotiation.OutgoingSessionNegotiationHandler;
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.AbstractOutgoingResourceNegotiation;
import saros.negotiation.IncomingSessionNegotiation;
import saros.negotiation.OutgoingSessionNegotiation;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionManager;

/** Implementation of {@link INegotiationHandler}. */
public class NegotiationHandler implements INegotiationHandler {

  private final OutgoingSessionNegotiationHandler outgoingSessionNegotiationHandler;

  private IncomingSessionNegotiationHandler incomingSessionNegotiationHandler;

  private OutgoingResourceNegotiationHandler outgoingResourceNegotiationHandler;

  private IncomingResourceNegotiationHandler incomingResourceNegotiationHandler;

  public NegotiationHandler(
      final ISarosSessionManager sessionManager,
      final OutgoingSessionNegotiationHandler outgoingSessionNegotiationHandler,
      final IncomingSessionNegotiationHandler incomingSessionNegotiationHandler,
      final OutgoingResourceNegotiationHandler outgoingResourceNegotiationHandler,
      final IncomingResourceNegotiationHandler incomingResourceNegotiationHandler) {

    sessionManager.setNegotiationHandler(this);

    this.outgoingSessionNegotiationHandler = outgoingSessionNegotiationHandler;
    this.incomingSessionNegotiationHandler = incomingSessionNegotiationHandler;
    this.outgoingResourceNegotiationHandler = outgoingResourceNegotiationHandler;
    this.incomingResourceNegotiationHandler = incomingResourceNegotiationHandler;
  }

  @Override
  public void handleOutgoingSessionNegotiation(OutgoingSessionNegotiation negotiation) {
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              this.outgoingSessionNegotiationHandler.handle(negotiation);
            });
  }

  @Override
  public void handleIncomingSessionNegotiation(IncomingSessionNegotiation negotiation) {
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              this.incomingSessionNegotiationHandler.handle(negotiation);
            });
  }

  @Override
  public void handleOutgoingResourceNegotiation(AbstractOutgoingResourceNegotiation negotiation) {
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              this.outgoingResourceNegotiationHandler.handle(negotiation);
            });
  }

  @Override
  public void handleIncomingResourceNegotiation(AbstractIncomingResourceNegotiation negotiation) {
    Executors.newCachedThreadPool()
        .submit(
            () -> {
              this.incomingResourceNegotiationHandler.handle(negotiation);
            });
  }
}
