package saros.server.filesystem;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.filesystem.ReferencePointImpl;

public class ServerReferencePointManager {
  private final ConcurrentHashMap<IReferencePoint, File> referencePointToFileMapper;

  public ServerReferencePointManager() {
    referencePointToFileMapper = new ConcurrentHashMap<IReferencePoint, File>();
  }

  public static IReferencePoint create(IWorkspace workspace) {
    IPath serverWorkspacePath = workspace.getLocation();

    return new ReferencePointImpl(serverWorkspacePath);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link File} directory
   *
   * @param referencePoint the key of the pair
   * @param directory the value of the pair
   */
  public void putIfAbsent(IReferencePoint referencePoint, File directory) {
    if (!referencePointToFileMapper.containsKey(referencePoint)) {
      referencePointToFileMapper.put(referencePoint, directory);
    }
  }

  /**
   * Returns the {@link File} directory given by the {@link IReferencePoint}
   *
   * @param referencePoint the key for which the directory should be returned
   * @return the directory given by referencePoint
   */
  public File get(IReferencePoint referencePoint) {

    return referencePointToFileMapper.get(referencePoint);
  }

  /**
   * Returns the {@link File} resource in combination of the {@link IReferencePoint} reference point
   * and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath
   */
  public File getResource(IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    File directory = get(referencePoint);

    if (directory == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a directoy.");

    return findVirtualFile(directory, referencePointRelativePath);
  }

  private File findVirtualFile(final File directory, IPath path) {

    File file = new File(directory.getPath().concat(path.toString()));

    if (!file.exists()) return null;

    return file;
  }
}
