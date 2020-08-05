package saros.lsp.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import saros.filesystem.IReferencePoint;

import static saros.filesystem.IResource.Type.REFERENCE_POINT;

public class LspReferencePoint extends LspContainer implements IReferencePoint {

  public LspReferencePoint(IWorkspacePath workspace, String name) {
    super(workspace, LspPath.fromString(name));
  }

  @Override
  public Type getType() {
    return REFERENCE_POINT;
  }

  /**
   * Creates the underlying folder structure for the project
   *
   * @throws IOException
   */
  public void create() throws IOException {
    Files.createDirectory(toNioPath());
  }
}
