package saros.lsp.activity;

import org.apache.log4j.Logger;

import saros.activities.IActivity;
import saros.session.AbstractActivityConsumer;
import saros.session.User;

public class ActivityConsumer extends AbstractActivityConsumer {
    private static final Logger LOG = Logger.getLogger(ActivityConsumer.class);

    @Override
    public void exec(IActivity activity) {
        LOG.debug(String.format("exec(%s)", activity.toString()));
        
        User sender = activity.getSource();
          if (!sender.isInSession()) {
            LOG.warn(
                "skipping execution of activity "
                    + activity
                    + " for user "
                    + sender
                    + " who is not in the current session");
            return;
          }

          // First let the remote manager update itself based on the
          // Activity
          //TODO: remoteWriteAccessManager.exec(activity);

          super.exec(activity);
    }

}