package saros.lsp.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IResource;

public abstract class LspContainer extends LspResource implements IContainer {
  private static final Logger LOG = Logger.getLogger(LspContainer.class);

  public LspContainer(IWorkspacePath workspace, IPath path) {
    super(workspace, path);
  }

  @Override
  public void delete() throws IOException {
    FileUtils.deleteDirectory(getLocation().toFile());
  }

  @Override
  public List<IResource> members() throws IOException {//TODO: später hier über Editor fragen?
    LOG.info("members() -> REQUESTING");
    List<IResource> members = new ArrayList<>();

    File[] memberFiles = getLocation().toFile().listFiles();
    if (memberFiles == null) {
      LOG.info("members() -> memberFiles is NULL");
      throw new NoSuchFileException(getLocation().toOSString());
    }

    for (File f : memberFiles) {
      LOG.info(String.format("members().for() -> %s", f.toString()));
      IPath memberPath = getLocation().append(f.getName());
      IResource member;

      if (f.isDirectory()) {
        LOG.info("members().for() -> isDirectory");
        IFolder folder = new LspFolder(getWorkspace(), memberPath);
        member = folder;
      } else {
        LOG.info("members().for() -> isFile");
        member = new LspFile(getWorkspace(), memberPath);
      }

      LOG.info(String.format("members().member -> %s", member));
      members.add(member);
    }

    return members;
  }

  @Override
  public IFile getFile(IPath path) {
    return new LspFile(getWorkspace(), path);
  }

  @Override
  public IFile getFile(String pathString) {
    return getFile(LspPath.fromString(pathString));
  }

  @Override
  public IFolder getFolder(IPath path) {
    return new LspFolder(getWorkspace(), path);
  }

  @Override
  public IFolder getFolder(String pathString) {
    return getFolder(LspPath.fromString(pathString));
  }

  private IPath getFullMemberPath(IPath memberPath) {
    return getLocation().append(memberPath);
  }

  @Override
  public boolean exists(IPath path) {
    IPath childLocation = getLocation().append(path);
    return childLocation.toFile().exists();
  }
}
