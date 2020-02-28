package saros.lsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.filesystem.IWorkspace;
import saros.lsp.context.LspContextFactory;
import saros.lsp.context.LspCoreContextFactory;
import saros.lsp.context.LspOptionalContextFactory;
import saros.lsp.context.LspProxyContextFactory;
import saros.lsp.context.LspSessionContextFactory;
import saros.lsp.extensions.ISarosLanguageServer;
import saros.lsp.extensions.client.ISarosLanguageClient;

public class SarosLifecycle extends AbstractContextLifecycle {

  private ISarosLanguageClient client;
  private IWorkspace workspace;

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    Collection<IContextFactory> factories = new ArrayList<IContextFactory>();

    factories.add(new LspOptionalContextFactory());
    factories.add(new LspCoreContextFactory());
    factories.add(new LspContextFactory());
    factories.add(new LspProxyContextFactory<ISarosLanguageClient>(ISarosLanguageClient.class, () -> this.client));
    factories.add(new LspProxyContextFactory<IWorkspace>(IWorkspace.class, () -> this.workspace));

    return factories;
  }

  public ISarosLanguageServer createLanguageServer() {
    return this.getSarosContext().getComponent(ISarosLanguageServer.class);
  }

  public ISarosLanguageClient registerLanguageClient(ISarosLanguageClient client) {

    this.client = client;

    return client;
  }

  public IWorkspace registerWorkspace(IWorkspace workspace) {

    this.workspace = workspace;

    return workspace;
  }
}
