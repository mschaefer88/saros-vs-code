package saros.lsp.activity;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.MessageType;

import saros.activities.ChecksumErrorActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.filesystem.IResource;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.monitoring.IProgressMonitor;
import saros.observables.ValueChangeListener;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityListener;
import saros.session.ISarosSession;
import saros.session.ISessionLifecycleListener;
import saros.repackaged.picocontainer.Startable;

public class InconsistencyHandler extends AbstractActivityConsumer implements Startable {

    private final ISarosSession session;
    private final ISarosLanguageClient languageClient;
    private final IProgressMonitor progressMonitor;
    private final IsInconsistentObservable isInconsistentObservable;

    private final Logger LOG = Logger.getLogger(InconsistencyHandler.class);

    private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;

    // private final ISessionLifecycleListener sessionLifecycleListener = new ISessionLifecycleListener() {

    //     @Override
    //     public void sessionStarted(final ISarosSession session) {
    //       LOG.info("Session started!");
    //       initialize(session);
    //     }
    
    //     @Override
    //     public void sessionEnded(final ISarosSession session, SessionEndReason reason) {
    //       LOG.info("Session ended!");
    //       uninitialize(session);
    //     }
    //   };

    public InconsistencyHandler(ISarosSession session, IsInconsistentObservable isInconsistentObservable, ISarosLanguageClient languageClient, IProgressMonitor progressMonitor) {
        this.session = session;
        this.languageClient = languageClient;
        this.progressMonitor = progressMonitor;      
        this.isInconsistentObservable = isInconsistentObservable;
    }

    private void handleConsistencyChange(Boolean isInconsistent) {
        LOG.info(String.format("Consistency changed: isInconsistent = %b", isInconsistent));

        if(isInconsistent) {
            if(!this.session.isHost()) {
                ConsistencyWatchdogClient client = this.session.getComponent(ConsistencyWatchdogClient.class);
                Set<SPath> paths = client.getPathsWithWrongChecksums();

                if(!paths.isEmpty()) {
                    this.handleInconsistency(paths, client);
                }
            }
        }
    }

    @Override
    public void start() {//TODO: do with session listener?
        LOG.info("start");

        this.isInconsistentObservable.add(this.isConsistencyListener);
    }

    @Override
    public void stop() {
        LOG.info("stop");

        this.isInconsistentObservable.remove(this.isConsistencyListener);
    }

    private void handleInconsistency(Set<SPath> files, ConsistencyWatchdogClient watchdogClient) {

        if(files.isEmpty()) {
            return;
        }

        String message = "These files have become unsynchronized with the host:{1} {0} {1}{1}Do you want to synchronize your project? \nYou may wish to backup those file(s) in case important changes are overwritten.";
        String fileList = this.createInconsistentPathsMessage(files);
        this.languageClient.showMessageRequest(new ShowMessageParams(MessageType.Warning, "Inconsistency Detected", MessageFormat.format(message, files, System.lineSeparator()), "Yes", "No"))
        .thenAccept(action -> {
            if(action.getTitle().equals("Yes")) {
                watchdogClient.runRecovery(this.progressMonitor);
            }
            this.isConsistencyListener.setValue(false);
        });
    }

    private String createInconsistentPathsMessage(Set<SPath> paths) {
        StringBuilder sb = new StringBuilder();
    
        for (SPath path : paths) {
          IResource resource = path.getResource();
    
          if (resource == null) {
            LOG.warn("Inconsistent resource " + path + " could not be " + "found.");
    
            continue;
          }
    
          if (sb.length() > 0) {
            sb.append(", ");
          }
    
          sb.append(resource.getProject().getName())
              .append(" - ")
              .append(resource.getProjectRelativePath().lastSegment());
        }
    
        return sb.toString();
      }

    // @Override
    // public void receive(ChecksumErrorActivity checksumErrorActivity) {
        
        // String message = "These files have become unsynchronized with the host:\n {0} \n\nPress the inconsistency recovery button to synchronize your project. \nYou may wish to backup those file(s) in case important changes are overwritten.";
        // String files = String.join(", ", (CharSequence[]) checksumErrorActivity.getPaths().stream()
        //         .map(path -> path.getFile().getName()).toArray());
        // this.languageClient.showMessageRequest(new ShowMessageParams(MessageType.Warning, "Inconsistency Detected", MessageFormat.format(message, files), "Yes", "No"))
        // .thenAccept(action -> {
        //     if(action.getTitle().equals("Yes")) {
        //         this.watchdogClient.runRecovery(this.progressMonitor);
        //     }
        // });
    // }
}