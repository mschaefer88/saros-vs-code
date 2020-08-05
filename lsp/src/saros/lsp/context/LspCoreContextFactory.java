package saros.lsp.context;

import saros.core.context.AbstractCoreContextFactory;
import saros.lsp.monitoring.remote.LspRemoteProgressIndicatorFactory;
import saros.lsp.preferences.LspPreferenceStore;
import saros.lsp.preferences.LspPreferences;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

public class LspCoreContextFactory extends AbstractCoreContextFactory {

  @Override
  protected Class<? extends IPreferenceStore> getPreferenceStoreClass() {
    return LspPreferenceStore.class;
  }

  @Override
  protected Class<? extends Preferences> getPreferencesClass() {
    return LspPreferences.class;
  }

  @Override
  protected String getVersion() {
    // TODO get from proper source
    return "15.0.0"; // TODO: faked for eclipse support
  }

  @Override
  protected Class<? extends IRemoteProgressIndicatorFactory>
      getRemoteProgressIndicatorFactoryClass() {
    return LspRemoteProgressIndicatorFactory.class;
  }
}
