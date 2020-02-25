package saros.lsp.activity;

import org.apache.log4j.Logger;

import saros.activities.IActivity;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.User;
import saros.repackaged.picocontainer.Startable;

public class ActivityProducer extends AbstractActivityProducer implements Startable {
    private static final Logger LOG = Logger.getLogger(ActivityConsumer.class);

    private final ISarosSession session;

    public ActivityProducer(ISarosSession session) {
        this.session = session;
    }

    @Override
    public void start() {
        this.session.addActivityProducer(this);
    }

    @Override
    public void stop() {
        this.session.removeActivityProducer(this);
    }
}