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

public class LspWorkspace implements IWorkspace {

    private static final Logger LOG = Logger.getLogger(LspWorkspace.class);

    public static List<IProject> projects = new LinkedList<IProject>();

    public LspWorkspace(String root) {
        
    }

    @Override
    public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {
        // TODO Auto-generated method stub
        LOG.info("run`1");
    }

    @Override
    public void run(IWorkspaceRunnable runnable, IResource[] resources) throws IOException, OperationCanceledException {
        // TODO Auto-generated method stub
        LOG.info("run`2");

    }

    @Override
    public IProject getProject(String project) {
        // TODO Auto-generated method stub
        LOG.info("getProject");
        return null;
    }

    @Override
    public IPath getLocation() {
        // TODO Auto-generated method stub
        LOG.info("getLocation");
        return null;
    }

}