package saros.core.context;

import saros.context.AbstractContextFactory;
import saros.context.IContextKeyBindings;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.BindKey;
import saros.repackaged.picocontainer.MutablePicoContainer;

public abstract class AbstractCoreContextFactory extends AbstractContextFactory {

  protected abstract Class<? extends IPreferenceStore> getPreferenceStoreClass();

  protected abstract Class<? extends Preferences> getPreferencesClass();

  protected abstract Class<? extends IRemoteProgressIndicatorFactory>
      getRemoteProgressIndicatorFactoryClass();

  protected abstract String getVersion();

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(IPreferenceStore.class, this.getPreferenceStoreClass());
    container.addComponent(Preferences.class, this.getPreferencesClass());
    container.addComponent(
        IRemoteProgressIndicatorFactory.class, this.getRemoteProgressIndicatorFactoryClass());

    container.addComponent(
        BindKey.bindKey(String.class, IContextKeyBindings.SarosVersion.class), this.getVersion());
  }
}
