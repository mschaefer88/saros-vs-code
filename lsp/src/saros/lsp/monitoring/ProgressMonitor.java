package saros.lsp.monitoring;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.lsp.extensions.client.dto.ProgressParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressBegin;
import saros.lsp.extensions.client.dto.WorkDoneProgressCreateParams;
import saros.lsp.extensions.client.dto.WorkDoneProgressEnd;
import saros.lsp.extensions.client.dto.WorkDoneProgressReport;
import saros.monitoring.IProgressMonitor;

public class ProgressMonitor implements IProgressMonitor {

  private static final Logger LOG = Logger.getLogger(ProgressMonitor.class);

  private ExecutorService executor = Executors.newCachedThreadPool();

  private ISarosLanguageClient client;

  private String token;

  private String taskName;

  private String subTask;

  private boolean canceled;

  private int size;

  public ProgressMonitor(ISarosLanguageClient client) {
    this.client = client;
    this.token = UUID.randomUUID().toString();
  }

  @Override
  public void done() {
    LOG.debug("done");

    this.endProgress("Done");
  }

  @Override
  public void subTask(String name) {
    LOG.debug(String.format("subTask('%s')", name));

    this.subTask = name;
  }

  @Override
  public void setTaskName(String name) {
    LOG.debug(String.format("setTaskName('%s')", name));

    this.taskName = name;
  }

  @Override
  public void worked(int amount) {
    LOG.debug(String.format("worked(%d)", amount));

    if (this.canceled) {
      LOG.error("cancelling not supported");
      throw new UnsupportedOperationException();
    }

    this.reportProgress(amount);
  }

  @Override
  public void setCanceled(boolean canceled) {
    LOG.debug(String.format("setCanceled(%b)", canceled));

    if (this.canceled && !canceled) {
      LOG.error("resuming not supported");
      throw new UnsupportedOperationException();
    }

    if (canceled) {
      this.endProgress("Cancelled");
    }

    this.canceled = canceled;
  }

  @Override
  public boolean isCanceled() {
    LOG.debug(String.format("isCanceled -> %b", this.canceled));

    return this.canceled;
  }

  @Override
  public void beginTask(String name, int size) {
    LOG.debug(String.format("beginTask('%s', %d)", name, size));

    if (this.canceled) {
      LOG.error("resuming not supported");
      throw new UnsupportedOperationException();
    }

    this.size = size;

    this.setTaskName(name);
    this.createProgress();
    this.beginProgress(this.taskName);
  }

  private void createProgress() {
    LOG.info("createProgress()");
    WorkDoneProgressCreateParams c = new WorkDoneProgressCreateParams(this.token);

    this.client.createProgress(c);
    LOG.info("createProgress() -> sent");
  }

  private void beginProgress(String title) {

    ProgressParams<WorkDoneProgressBegin> p =
        new ProgressParams<WorkDoneProgressBegin>(
            this.token, new WorkDoneProgressBegin(title, null, 0, false));

    this.client.progress(p);
  }

  private void reportProgress(int amount) {

    ProgressParams<WorkDoneProgressReport> p =
        new ProgressParams<WorkDoneProgressReport>(
            this.token,
            new WorkDoneProgressReport(
                this.subTask, (int) Math.round(amount / (double) this.size * 100), false));

    this.client.progress(p);
  }

  private void endProgress(String message) {

    ProgressParams<WorkDoneProgressEnd> p =
        new ProgressParams<WorkDoneProgressEnd>(this.token, new WorkDoneProgressEnd(message));

    this.client.progress(p);
  }
}
