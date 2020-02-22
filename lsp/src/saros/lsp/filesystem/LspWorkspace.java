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
import saros.monitoring.IProgressMonitor;
import saros.monitoring.remote.IRemoteProgressIndicatorFactory;

public class LspWorkspace implements IWorkspace {

    private static final Logger LOG = Logger.getLogger(LspWorkspace.class);

    public static List<IProject> projects = new LinkedList<IProject>();

    private final IProgressMonitor progressMonitor;

    public LspWorkspace(String root, IProgressMonitor progressMonitor) {
        LOG.info("Root is " + root);

        this.progressMonitor = progressMonitor;
    }

    @Override
    public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {
        this.run(runnable, null);
    }

    @Override
    public void run(IWorkspaceRunnable runnable, IResource[] resources) throws IOException, OperationCanceledException {
        // TODO Auto-generated method stub
        LOG.info("run`2");

        for (IResource resource : resources) {
            LOG.info("Resource: " + resource.getName());
        }

        runnable.run(this.progressMonitor);
    }

    @Override
    public IProject getProject(String project) {
        // TODO not supported (deprecated)
        LOG.info("getProject");
        return null;
    }

    @Override
    public IPath getLocation() {
        // TODO not supported (deprecated)
        LOG.info("getLocation");
        return null;
    }

}