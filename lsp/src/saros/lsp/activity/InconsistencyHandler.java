package saros.lsp.activity;

import java.text.MessageFormat;
import java.util.Set;
import org.eclipse.lsp4j.MessageType;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.filesystem.IFile;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.dto.ShowMessageParams;
import saros.monitoring.IProgressMonitor;
import saros.observables.ValueChangeListener;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

public class InconsistencyHandler extends AbstractActivityConsumer implements Startable {

  private final ISarosSession session;
  private final ISarosLanguageClient languageClient;
  private final IProgressMonitor progressMonitor;
  private final IsInconsistentObservable isInconsistentObservable;

  private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;

  public InconsistencyHandler(
      ISarosSession session,
      IsInconsistentObservable isInconsistentObservable,
      ISarosLanguageClient languageClient,
      IProgressMonitor progressMonitor) {
    this.session = session;
    this.languageClient = languageClient;
    this.progressMonitor = progressMonitor;
    this.isInconsistentObservable = isInconsistentObservable;
  }

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

  private void handleInconsistency(Set<IFile> files, ConsistencyWatchdogClient watchdogClient) {
    if (files.isEmpty()) {
      return;
    }

    String message =
        "These files have become unsynchronized with the host:{1} {0} {1}{1}Do you want to synchronize your project? \nYou may wish to backup those file(s) in case important changes are overwritten.";
    this.languageClient
        .showMessageRequest(
            new ShowMessageParams(
                MessageType.Warning,
                "Inconsistency Detected",
                MessageFormat.format(message, files, System.lineSeparator()),
                "Yes",
                "No"))
        .thenAccept(
            action -> {
              if (action.getTitle().equals("Yes")) {
                watchdogClient.runRecovery(this.progressMonitor);
              }
              this.isConsistencyListener.setValue(false);
            });
  }
}
