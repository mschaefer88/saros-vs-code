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

    private static final Logger LOG = Logger.getLogger(LspWorkspace.class);

    public LspWorkspace(IPath root) {
        LOG.info("Root is " + root.toString());
        LOG.info("Hash is " + this.hashCode());
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
        return new LspProject(this.getLocation().append(name));
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