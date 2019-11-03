package saros.core.context;

import saros.communication.connection.IProxyResolver;
import saros.context.AbstractContextFactory;
import saros.filesystem.IChecksumCache;
import saros.repackaged.picocontainer.MutablePicoContainer;

public abstract class AbstractOptionalContextFactory extends AbstractContextFactory {

  protected abstract Class<? extends IProxyResolver> getProxyResolverClass();

  protected abstract Class<? extends IChecksumCache> getChecksumCacheClass();

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(IProxyResolver.class, this.getProxyResolverClass());
    container.addComponent(IChecksumCache.class, this.getChecksumCacheClass());
  }
}
