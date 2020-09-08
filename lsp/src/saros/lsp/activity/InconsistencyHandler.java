package saros.lsp.activity;

import java.text.MessageFormat;
import java.util.Set;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.filesystem.IFile;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.monitoring.ProgressMonitor;
import saros.lsp.ui.UIInteractionManager;
import saros.observables.ValueChangeListener;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/**
 * The InconsistencyHandler is responsible for asking the user if inconsistencies should be resolved
 * once they have been reported by the watchdog client.
 *
 * <p>If the user responds with a positive response the inconsistencies will be resolved by the
 * watchdog client.
 */
public class InconsistencyHandler extends AbstractActivityConsumer implements Startable {

  private final ISarosSession session;
  private final IsInconsistentObservable isInconsistentObservable;

  private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;
  private UIInteractionManager interactionManager;
  private ISarosLanguageClient client;

  public InconsistencyHandler(
      ISarosSession session,
      IsInconsistentObservable isInconsistentObservable,
      ISarosLanguageClient client,
      UIInteractionManager interactionManager) {
    this.session = session;
    this.isInconsistentObservable = isInconsistentObservable;
    this.client = client;
    this.interactionManager = interactionManager;
  }

  /**
   * Callback for any change in consistency that inform the user of inconsistencies an resolve them
   * if whished by said user.
   *
   * @param isInconsistent <i>true</i> if file(s) are currently inconsistend, <i>false</i> otherwise
   */
  private void handleConsistencyChange(Boolean isInconsistent) {
    if (isInconsistent) {
      if (!this.session.isHost()) {
        ConsistencyWatchdogClient client =
            this.session.getComponent(ConsistencyWatchdogClient.class);
        Set<IFile> paths = client.getFilesWithWrongChecksums();

        if (!paths.isEmpty()) {
          this.handleInconsistency(paths, client);
        }
      }
    }
  }

  @Override
  public void start() {
    this.isInconsistentObservable.add(this.isConsistencyListener);
  }

  @Override
  public void stop() {
    this.isInconsistentObservable.remove(this.isConsistencyListener);
  }

  /**
   * Handles reported inconsistencies and presents them to the user. If the response is answered
   * positively the issues will be resolved by the watchdog client.
   *
   * @param files Files that have been reported to be inconsistend
   * @param watchdogClient Client for resolving inconsistency issues
   */
  private void handleInconsistency(Set<IFile> files, ConsistencyWatchdogClient watchdogClient) {
    if (files.isEmpty()) {
      return;
    }

    String message =
        "These files have become unsynchronized with the host:{1} {0} {1}{1}Do you want to synchronize your project? \nYou may wish to backup those file(s) in case important changes are overwritten.";
    if (this.interactionManager.getUserInputYesNo(
        "Inconsistency Detected", MessageFormat.format(message, files, System.lineSeparator()))) {
      watchdogClient.runRecovery(new ProgressMonitor(this.client));
      this.isConsistencyListener.setValue(false);
    }
  }
}
