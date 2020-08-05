package saros.lsp.filesystem;

import java.net.URI;
import java.nio.file.Paths;

import saros.filesystem.IReferencePoint;

public class WorkspacePath extends LspPath implements IWorkspacePath {
    public WorkspacePath(URI path) {
        super(Paths.get(path));
    }
  
    public IReferencePoint getReferencePoint(String name) {
      return new LspReferencePoint(this, name);
    }
}