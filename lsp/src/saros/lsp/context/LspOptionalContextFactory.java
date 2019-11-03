package saros.lsp.context;

import saros.communication.connection.IProxyResolver;
import saros.communication.connection.NullProxyResolver;
import saros.core.context.AbstractOptionalContextFactory;
import saros.filesystem.IChecksumCache;
import saros.filesystem.NullChecksumCache;

public class LspOptionalContextFactory extends AbstractOptionalContextFactory {

    @Override
    protected Class<? extends IProxyResolver> getProxyResolverClass() {
        return NullProxyResolver.class;
    }

    @Override
    protected Class<? extends IChecksumCache> getChecksumCacheClass() {
        return NullChecksumCache.class;
    }

}