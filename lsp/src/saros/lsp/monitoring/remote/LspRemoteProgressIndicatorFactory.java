package saros.lsp.monitoring.remote;

import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;
import saros.monitoring.remote.RemoteProgressManager;
import saros.session.User;

public class LspRemoteProgressIndicatorFactory implements IRemoteProgressIndicatorFactory {

  @Override
  public IRemoteProgressIndicator create(
      RemoteProgressManager remoteProgressManager, String remoteProgressID, User remoteUser) {

    return new LspRemoteProgressIndicator(); // TODO: manager muss hier drauf arbeiten
  }
}
