package saros.lsp.filesystem;

import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;

/** Describes the path to the currently open workspace. */
public interface IWorkspacePath extends IPath {

  /**
   * Gets the {@link IReferencePoint} from within
   * the workspace.
   * 
   * @param name Name of the {@link IReferencePoint}
   * @return The {@link IReferencePoint} with the given name
   */
  IReferencePoint getReferencePoint(String name);
}
