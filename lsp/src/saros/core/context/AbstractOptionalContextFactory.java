package saros.core.context;

import saros.context.AbstractContextFactory;
import saros.filesystem.checksum.IChecksumCache;
import saros.repackaged.picocontainer.MutablePicoContainer;

public abstract class AbstractOptionalContextFactory extends AbstractContextFactory {

  protected abstract Class<? extends IChecksumCache> getChecksumCacheClass();

  @Override
  public void createComponents(MutablePicoContainer container) {
    container.addComponent(IChecksumCache.class, this.getChecksumCacheClass());
  }
}
