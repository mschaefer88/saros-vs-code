package saros.lsp.filesystem;

import org.apache.log4j.Logger;

import saros.filesystem.IPath;
import saros.server.filesystem.ServerPathImpl;
import saros.server.filesystem.ServerProjectImpl;
import saros.server.filesystem.ServerWorkspaceImpl;

public class LspProject extends ServerProjectImpl {

    public LspProject(String root) {
        this(ServerPathImpl.fromString(root));
    }

    public LspProject(IPath rootPath) {
        super(new ServerWorkspaceImpl(rootPath.removeLastSegments(1)), rootPath.lastSegment());
    }
    
    private static final Logger LOG = Logger.getLogger(LspProject.class);

}