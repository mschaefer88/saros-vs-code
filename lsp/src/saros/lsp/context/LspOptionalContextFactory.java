package saros.lsp.context;

import saros.core.context.AbstractOptionalContextFactory;
import saros.filesystem.checksum.IChecksumCache;
import saros.filesystem.checksum.NullChecksumCache;

public class LspOptionalContextFactory extends AbstractOptionalContextFactory {

  @Override
  protected Class<? extends IChecksumCache> getChecksumCacheClass() {
    return NullChecksumCache.class;
  }
}
