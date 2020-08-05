package saros.lsp.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IResource;

public abstract class LspContainer extends LspResource implements IContainer {

  public LspContainer(IWorkspacePath workspace, IPath path) {
    super(workspace, path);
  }

  @Override
  public void delete() throws IOException {
    FileUtils.deleteDirectory(getLocation().toFile());
  }

  @Override
  public List<IResource> members() throws IOException {//TODO: später hier über Editor fragen?
    List<IResource> members = new ArrayList<>();

    File[] memberFiles = getLocation().toFile().listFiles();
    if (memberFiles == null) {
      throw new NoSuchFileException(getLocation().toOSString());
    }

    for (File f : memberFiles) {
      IPath memberPath = getFullPath().append(f.getName());
      IResource member;

      if (f.isDirectory()) {
        member = new LspFolder(getWorkspace(), memberPath);
      } else {
        member = new LspFile(getWorkspace(), memberPath);
      }

      members.add(member);
    }

    return members;
  }

  @Override
  public IFile getFile(IPath path) {
    return new LspFile(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(LspPath.fromString(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new LspFolder(getWorkspace(), getFullMemberPath(path));
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(LspPath.fromString(pathString));
  }

  private IPath getFullMemberPath(IPath memberPath) {
    return getFullPath().append(memberPath);
  }

  @Override
  public boolean exists(IPath path) {
    IPath childLocation = getLocation().append(path);
    return childLocation.toFile().exists();
  }
}
