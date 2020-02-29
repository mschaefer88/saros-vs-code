package saros.lsp.filesystem;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import saros.exceptions.OperationCanceledException;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.NullProgressMonitor;

public class LspWorkspace implements IWorkspace {

    private final Logger LOG = Logger.getLogger(LspWorkspace.class);

    public LspWorkspace(IPath root) {
        this.location = root;
    }

    private IPath location;

    /** @deprecated See {@link IWorkspace}. */
    @Override
    @Deprecated
    public IPath getLocation() {
        return location;
    }

    /** @deprecated See {@link IWorkspace}. */
    @Override
    @Deprecated
    public IProject getProject(String name) {
        LOG.info(String.format("getProject('%s')", name));
        return new LspProject(this, name);
    }

    @Override
    public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {

        run(runnable, null);
    }

    @Override
    public void run(IWorkspaceRunnable runnable, IResource[] resources) throws IOException, OperationCanceledException {

        synchronized (this) {
            runnable.run(new NullProgressMonitor());// TODO: use my progressmonitor
        }
    }

}