package saros.lsp.monitoring.remote;

import org.apache.log4j.Logger;
import saros.activities.ProgressActivity;
import saros.monitoring.remote.IRemoteProgressIndicator;
import saros.session.User;

// TODO: correct location?!
public class LspRemoteProgressIndicator implements IRemoteProgressIndicator {

  private static final Logger LOG = Logger.getLogger(LspRemoteProgressIndicator.class);

  @Override
  public String getRemoteProgressID() {
    LOG.debug("getRemoteProgressID");
    return null;
  }

  @Override
  public User getRemoteUser() {
    LOG.debug("getRemoteUser");
    return null;
  }

  @Override
  public void start() {
    LOG.debug("start");
  }

  @Override
  public void stop() {
    LOG.debug("stop");
  }

  @Override
  public void handleProgress(ProgressActivity activity) {
    LOG.debug(String.format("handleProgress([%s])", activity.getTaskName()));
  }
}
