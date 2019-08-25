package saros.intellij.filesystem;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import saros.activities.SPath;
import saros.filesystem.IPath;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.filesystem.ReferencePointImpl;
import saros.intellij.project.filesystem.IntelliJPathImpl;
import saros.repackaged.picocontainer.annotations.Inject;

/**
 * The IntelliJReferencePointManager maps an {@link IReferencePoint} reference point to {@link
 * Module} module
 */
public class IntelliJReferencePointManager {

  private final ConcurrentHashMap<IReferencePoint, Module> referencePointToModuleMapper;

  public IntelliJReferencePointManager() {
    referencePointToModuleMapper = new ConcurrentHashMap<IReferencePoint, Module>();
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link Module} module,
   * or null, if {@link Module} module is null or its path is empty. The reference point points on
   * the module's root full path.
   *
   * @param module, for which a reference point should be created
   * @return the reference point of given module
   */
  public static IReferencePoint create(Module module) {
    if (module == null) return null;

    IPath path = IntelliJPathImpl.fromString(module.getName());

    return new ReferencePointImpl(path);
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link VirtualFile}
   * virtual file, or null, if {@link VirtualFile} virtual file is null. The reference point points
   * on the module's root full path.
   *
   * @param virtualFile, for which a reference point should be created
   * @return the reference point of given virtual file
   */
  public static IReferencePoint create(VirtualFile virtualFile) {
    if (virtualFile == null) return null;

    Module module = FilesystemUtils.findModuleForVirtualFile(virtualFile);

    return create(module);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link Module} module, if the
   * IntelliJReferencePointManager doesn't contain this pair. The reference point will created
   * automatically.
   *
   * @param module, which should be inserted to the IntelliJReferencePointManager.
   * @exception IllegalArgumentException if the {@link Module} module is null
   */
  public void putIfAbsent(@NotNull Module module) {
    IReferencePoint referencePoint = create(module);

    putIfAbsent(referencePoint, module);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link Module} module
   *
   * @param referencePoint, which should be inserted to the IntelliJReferencePointManager.
   * @param module, which should be inserted to the IntelliJReferencePointManager.
   * @exception IllegalArgumentException if the {@link Module} module or the {@link IReferencePoint}
   *     reference point is null
   */
  public void putIfAbsent(@NotNull IReferencePoint referencePoint, @NotNull Module module) {
    referencePointToModuleMapper.putIfAbsent(referencePoint, module);
  }

  /**
   * Returns the {@link Module} module given by the {@link IReferencePoint} reference point
   *
   * @param referencePoint the key for which the module should be returned
   * @return the module given by referencePoint
   * @exception IllegalArgumentException if the {@link IReferencePoint} reference point is null
   */
  public Module getModule(@NotNull IReferencePoint referencePoint) {
    Module module = referencePointToModuleMapper.get(referencePoint);

    return module;
  }

  /**
   * Returns the {@link VirtualFile} resource in combination of the {@link IReferencePoint}
   * reference point and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the virtualFile of the reference point from referencePointRelativePath
   * @exception IllegalArgumentException if for {@link IReferencePoint} reference point doesn't
   *     exists a module or the {@link IPath} relative path is null
   */
  public VirtualFile getResource(
      @NotNull IReferencePoint referencePoint, @NotNull IPath referencePointRelativePath) {
    Module module = getModule(referencePoint);

    if (module == null)
      throw new IllegalArgumentException(
          "For reference point " + referencePoint + " doesn't exist a module.");

    return FilesystemUtils.findVirtualFile(module, referencePointRelativePath);
  }

  /**
   * Returns the {@link VirtualFile} resource represented by given {@link SPath} Saros Path.
   *
   * @param sPath to th virtualFile outgoing from the reference point in sPath
   * @return the virtualFile represented by sPath
   * @exception IllegalArgumentException if for {@link IReferencePoint} reference point doesn't
   *     exists a module, which is contained in sPath
   */
  public VirtualFile getResource(@NotNull SPath sPath) {
    return getResource(sPath.getReferencePoint(), sPath.getProjectRelativePath());
  }

  /**
   * Returns the {@link IResource} resource in combination of the {@link IReferencePoint} reference
   * point and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath
   * @exception IllegalArgumentException if for {@link IReferencePoint} reference point doesn't
   *     exists a module or the {@link IPath} relative path is null
   */
  public IResource getSarosResource(
      @NotNull IReferencePoint referencePoint, @NotNull IPath referencePointRelativePath) {
    Module module = getModule(referencePoint);
    VirtualFile vFile = getResource(referencePoint, referencePointRelativePath);

    return VirtualFileConverter.convertToResource(module.getProject(), vFile);
  }

  private static class FilesystemUtils {

    @Inject private static Project project;

    /**
     * * Returns the {@link Module} module of the given {@link VirtualFile virtualfile}
     *
     * @param virtualFile of the module
     * @return the module of the virtualFile
     */
    public static Module findModuleForVirtualFile(VirtualFile virtualFile) {

      return ModuleUtil.findModuleForFile(virtualFile, project);
    }

    /**
     * Determines and returns the {@link VirtualFile} virtual file given by the {@link Module}
     * module and {@link IPath} relative path, or null, if the relative path is absolute or has no
     * segments, or the virtual file is not found.
     *
     * @param module in which the virtual file is contained
     * @param path to the virtual file
     * @return the virtual file, if exists, otherwise null
     */
    public static VirtualFile findVirtualFile(final Module module, IPath path) {

      VirtualFile moduleRoot = getModuleRoot(module);

      if (path.isAbsolute()) return null;

      if (path.segmentCount() == 0) return moduleRoot;

      VirtualFile virtualFile = moduleRoot.findFileByRelativePath(path.toString());

      if (virtualFile != null
          && ModuleRootManager.getInstance(module).getFileIndex().isInContent(virtualFile)) {
        return virtualFile;
      }

      return null;
    }

    /**
     * Returns the {@link VirtualFile} module root of the given {@link Module} module.
     *
     * <p><b>Note:</b> The Module given {@link Module} module must have exactly one module root!
     *
     * @param module, for which the module root should be returned
     * @return the module root of the module
     */
    private static VirtualFile getModuleRoot(Module module) {
      if (module == null) return null;

      ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

      VirtualFile[] contentRoots = moduleRootManager.getContentRoots();

      int numberOfContentRoots = contentRoots.length;

      if (numberOfContentRoots != 1) {
        return null;
      }

      VirtualFile moduleRoot = contentRoots[0];

      return moduleRoot;
    }
  }
}
