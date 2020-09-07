package saros.lsp.filesystem;

import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;

public interface IWorkspacePath extends IPath {
  IReferencePoint getReferencePoint(String name);
}
