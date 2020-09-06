package saros.lsp;

import java.util.ArrayList;
import java.util.Collection;
import saros.context.AbstractContextLifecycle;
import saros.context.IContextFactory;
import saros.lsp.context.LspContextFactory;
import saros.lsp.context.CoreContextFactory;
import saros.lsp.context.FileSystemContextFactory;
import saros.lsp.context.ProxyContextFactory;
import saros.lsp.context.UiContextFactory;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.server.ISarosLanguageServer;
import saros.lsp.filesystem.IWorkspacePath;

public class SarosLifecycle extends AbstractContextLifecycle {

  private ISarosLanguageClient client;
  private IWorkspacePath workspace;

  @Override
  protected Collection<IContextFactory> additionalContextFactories() {
    Collection<IContextFactory> factories = new ArrayList<IContextFactory>();

    factories.add(new CoreContextFactory());
    factories.add(new LspContextFactory());
    factories.add(new UiContextFactory());
    factories.add(new FileSystemContextFactory());
    factories.add(
        new ProxyContextFactory<ISarosLanguageClient>(
            ISarosLanguageClient.class, () -> this.client));
    factories.add(new ProxyContextFactory<IWorkspacePath>(IWorkspacePath.class, () -> this.workspace));

    return factories;
  }

  public ISarosLanguageServer createLanguageServer() {
    return this.getSarosContext().getComponent(ISarosLanguageServer.class);
  }

  public ISarosLanguageClient registerLanguageClient(ISarosLanguageClient client) {

    this.client = client;

    return client;
  }

  public IWorkspacePath registerWorkspace(IWorkspacePath workspace) {

    this.workspace = workspace;

    return workspace;
  }
}
