package saros.lsp.context;

import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.filesystem.checksum.IChecksumCache;
import saros.filesystem.checksum.NullChecksumCache;
import saros.lsp.interaction.SubscriptionAuthorizer;
import saros.lsp.interaction.session.NegotiationHandler;
import saros.lsp.interaction.session.negotiation.IncomingResourceNegotiationHandler;
import saros.lsp.interaction.session.negotiation.IncomingSessionNegotiationHandler;
import saros.lsp.interaction.session.negotiation.OutgoingResourceNegotiationHandler;
import saros.lsp.interaction.session.negotiation.OutgoingSessionNegotiationHandler;
import saros.lsp.monitoring.remote.LspRemoteProgressIndicatorFactory;
import saros.lsp.preferences.LspPreferenceStore;
import saros.lsp.preferences.LspPreferences;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.INegotiationHandler;
import saros.session.ISarosSessionContextFactory;

/** ContextFactory for Saros core components. */
public class CoreContextFactory extends AbstractContextFactory {

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(IPreferenceStore.class, LspPreferenceStore.class);
    container.addComponent(Preferences.class, LspPreferences.class);
    container.addComponent(
        IRemoteProgressIndicatorFactory.class, LspRemoteProgressIndicatorFactory.class);
    container.addComponent(IChecksumCache.class, NullChecksumCache.class);
    container.addComponent(ISarosSessionContextFactory.class, SessionContextFactory.class);
    container.addComponent(SubscriptionAuthorizer.class);
    container.addComponent(INegotiationHandler.class, NegotiationHandler.class);
    container.addComponent(IncomingResourceNegotiationHandler.class);
    container.addComponent(OutgoingResourceNegotiationHandler.class);
    container.addComponent(IncomingSessionNegotiationHandler.class);
    container.addComponent(OutgoingSessionNegotiationHandler.class);
    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class), "0.0.1");
  }
}
