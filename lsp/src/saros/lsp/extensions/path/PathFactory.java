package saros.lsp.extensions.path; //TODO: best package location?

import org.apache.log4j.Logger;

import saros.filesystem.IPath;
import saros.filesystem.IPathFactory;
import saros.lsp.filesystem.LspPath;

public class PathFactory implements IPathFactory {//TODO: use factory where static was used!

    private static final Logger LOG = Logger.getLogger(PathFactory.class);

    @Override
  public String fromPath(IPath path) {

    if (path == null) throw new NullPointerException("path is null");

    return checkRelative(path).toString();
  }

  @Override
  public IPath fromString(String name) {

    if (name == null) throw new NullPointerException("name is null");

    return checkRelative(LspPath.fromString(name));
  }

  private IPath checkRelative(IPath path) {

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    return path;
  }

}