package saros.lsp.filesystem;

import java.net.URI;
import java.nio.file.Paths;
import saros.filesystem.IReferencePoint;

/** Implementation of {@link IWorkspacePath}. */
public class WorkspacePath extends LspPath implements IWorkspacePath {
  public WorkspacePath(URI path) {
    super(Paths.get(path));
  }

  @Override
  public IReferencePoint getReferencePoint(String name) {
    return new LspReferencePoint(this, name);
  }
}
