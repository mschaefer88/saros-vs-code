package saros.lsp.filesystem;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import saros.filesystem.IContainer;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public abstract class LspResource implements IResource {
    private IWorkspace workspace;
    private IPath path;
  
    /**
     * Creates a ServerResourceImpl.
     *
     * @param workspace the containing workspace
     * @param path the resource's path relative to the workspace's root
     */
    public LspResource(IWorkspace workspace, IPath path) {
      assert !path.isAbsolute();

      this.path = path;
      this.workspace = workspace;
    }
  
    /**
     * Returns the workspace the resource belongs to.
     *
     * @return the containing workspace
     */
    public IWorkspace getWorkspace() {
      return workspace;
    }
  
    @Override
    public IPath getFullPath() {
      return path;
    }
  
    @Override
    public IPath getProjectRelativePath() {
      return getFullPath();
    }
  
    @Override
    public String getName() {
      return getFullPath().lastSegment();
    }
  
    @Override
    public IPath getLocation() {
      return workspace.getLocation().append(path);
    }
  
    @Override
    public IContainer getParent() {
      IPath parentPath = getProjectRelativePath().removeLastSegments(1);
      IProject project = getProject();
      return parentPath.segmentCount() == 0 ? project : project.getFolder(parentPath);
    }
  
    @Override
    public IProject getProject() {
      return workspace.getProject("");
    }
  
    @Override
    public boolean exists() {
      return Files.exists(toNioPath());
    }
  
    @Override
    public boolean isIgnored() {
      return false;
    }
  
    @Override
    public <T extends IResource> T adaptTo(Class<T> clazz) {
      return clazz.isInstance(this) ? clazz.cast(this) : null;
    }
  
    @Override
    public final boolean equals(Object obj) {
  
      if (this == obj) return true;
  
      if (!(obj instanceof LspResource)) return false;
  
      LspResource other = (LspResource) obj;
  
      return getType() == other.getType()
          && getWorkspace().equals(other.getWorkspace())
          && getFullPath().equals(other.getFullPath());
    }
  
    @Override
    public final int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getType();
      result = prime * result + path.hashCode();
      result = prime * result + workspace.hashCode();
      return result;
    }
  
    /**
     * Returns the resource's location as a {@link java.nio.files.Path}. This is for internal use in
     * conjunction with the utility methods of the {@link java.nio.file.Files} class.
     *
     * @return location as {@link java.nio.files.Path}
     */
    Path toNioPath() {
      return ((LspPath) getLocation()).getDelegate();
    }
}