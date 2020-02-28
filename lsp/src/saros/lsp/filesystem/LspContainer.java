package saros.lsp.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import saros.filesystem.IContainer;
import saros.filesystem.IPath;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public abstract class LspContainer extends LspResource implements IContainer {
    
    public LspContainer(IWorkspace workspace, IPath path) {
        super(workspace, path);
      }
    
      @Override
      public void delete(int updateFlags) throws IOException {
        FileUtils.deleteDirectory(getLocation().toFile());
      }
    
      @Override
      public void move(IPath destination, boolean force) throws IOException {
        IPath destinationBase =
            destination.isAbsolute()
                ? getWorkspace().getLocation()
                : getLocation().removeLastSegments(1);
    
        IPath absoluteDestination = destinationBase.append(destination);
        FileUtils.moveDirectory(getLocation().toFile(), absoluteDestination.toFile());
      }
    
      @Override
      public IResource[] members() throws IOException {
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
    
        return members.toArray(new IResource[members.size()]);
      }
    
      @Override
      public IResource[] members(int memberFlags) throws IOException {
        return members();
      }
    
      @Override
      public boolean exists(IPath path) {
        IPath childLocation = getLocation().append(path);
        return childLocation.toFile().exists();
      }
    
      @Override
      public String getDefaultCharset() throws IOException {
        return getParent().getDefaultCharset();
      }
}