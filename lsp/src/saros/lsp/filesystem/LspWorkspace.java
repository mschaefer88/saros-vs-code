package saros.lsp.filesystem;

import java.io.IOException;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.NullProgressMonitor;

public class LspWorkspace implements IWorkspace {

  private final Logger LOG = Logger.getLogger(LspWorkspace.class);

  @Override
  public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {

    run(runnable, null);
  }

  @Override
  public void run(IWorkspaceRunnable runnable, IResource[] resources)
      throws IOException, OperationCanceledException {

    synchronized (this) {
      runnable.run(new NullProgressMonitor()); // TODO: use my progressmonitor
    }
  }
}
