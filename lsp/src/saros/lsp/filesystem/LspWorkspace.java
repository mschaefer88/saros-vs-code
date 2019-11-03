package saros.lsp.filesystem;

import java.io.IOException;

import saros.exceptions.OperationCanceledException;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;

public class LspWorkspace implements IWorkspace {

    @Override
    public void run(IWorkspaceRunnable runnable) throws IOException, OperationCanceledException {
        // TODO Auto-generated method stub

    }

    @Override
    public void run(IWorkspaceRunnable runnable, IResource[] resources) throws IOException, OperationCanceledException {
        // TODO Auto-generated method stub

    }

    @Override
    public IProject getProject(String project) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPath getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

}