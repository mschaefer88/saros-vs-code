package saros.lsp.activity;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.lsp4j.MessageType;

import saros.activities.ChecksumErrorActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.monitoring.IProgressMonitor;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityListener;
import saros.repackaged.picocontainer.Startable;

public class ChecksumErrorHandler extends AbstractActivityConsumer implements Startable {

    private final ConsistencyWatchdogClient watchdogClient;
    private final ISarosLanguageClient languageClient;
    private final IProgressMonitor progressMonitor;

    public ChecksumErrorHandler(ConsistencyWatchdogClient watchdogClient, ISarosLanguageClient languageClient, IProgressMonitor progressMonitor) {
        this.watchdogClient = watchdogClient;
        this.languageClient = languageClient;
        this.progressMonitor = progressMonitor;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void receive(ChecksumErrorActivity checksumErrorActivity) {
        
        String message = "These files have become unsynchronized with the host:\n {0} \n\nPress the inconsistency recovery button to synchronize your project. \nYou may wish to backup those file(s) in case important changes are overwritten.";
        String files = String.join(", ", (CharSequence[]) checksumErrorActivity.getPaths().stream()
                .map(path -> path.getFile().getName()).toArray());
        this.languageClient.showMessageRequest(new ShowMessageParams(MessageType.Warning, "Inconsistency Detected", MessageFormat.format(message, files), "Yes", "No"))
        .thenAccept(action -> {
            if(action.getTitle().equals("Yes")) {
                this.watchdogClient.runRecovery(this.progressMonitor);
            }
        });
    }
}