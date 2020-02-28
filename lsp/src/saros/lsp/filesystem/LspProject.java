package saros.lsp.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public class LspProject extends LspContainer implements IProject {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public LspProject(String path) {
        this(LspPath.fromString(path));
    }

    public LspProject(IPath path) {
        super(new LspWorkspace(path), LspPath.EMPTY);
    }
    
      @Override
      public int getType() {
        return IResource.PROJECT;
      }
    
      @Override
      public String getDefaultCharset() {
        // TODO: Read default character set from the project metadata files.
        return DEFAULT_CHARSET;
      }
    
      @Override
      public IResource findMember(IPath path) {
        if (path.segmentCount() == 0) {
          return this;
        }
    
        IPath memberLocation = getLocation().append(path);
        File memberFile = memberLocation.toFile();
    
        if (memberFile.isFile()) {
          return new LspFile(getWorkspace(), getFullMemberPath(path));
        } else if (memberFile.isDirectory()) {
          return new LspFolder(getWorkspace(), getFullMemberPath(path));
        } else {
          return null;
        }
      }
    
      @Override
      public IFile getFile(IPath path) {
          System.out.println(String.format("getFile(%s)", path));
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
    
      /**
       * Creates the underlying folder structure for the project
       *
       * @throws IOException
       */
      public void create() throws IOException {
        Files.createDirectory(toNioPath());
      }
}