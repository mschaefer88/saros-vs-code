package saros.lsp.extensions.path; //TODO: best package location?

import org.apache.log4j.Logger;

import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;

public class PathFactory implements IPathFactory {

    private static final Logger LOG = Logger.getLogger(PathFactory.class);

    @Override
    public String fromPath(IPath path) {
        // TODO Auto-generated method stub

        LOG.info(String.format("fromPath(%s)", path.toString()));

        return null;
    }

    @Override
    public IPath fromString(String name) {
        // TODO Auto-generated method stub
        LOG.info(String.format("fromString(%s)", name));

        return null;
    }

}