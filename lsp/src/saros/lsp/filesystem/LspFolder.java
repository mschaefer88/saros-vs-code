package saros.lsp.filesystem;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import saros.filesystem.IFolder;
import saros.filesystem.IPath;
import saros.filesystem.IResource;
import saros.filesystem.IWorkspace;

public class LspFolder extends LspContainer implements IFolder {
    public LspFolder(IWorkspace workspace, IPath path) {
        super(workspace, path);
      }
    
      @Override
      public int getType() {
        return IResource.FOLDER;
      }
    
      @Override
      public void create(int updateFlags, boolean local) throws IOException {
        try {
          Files.createDirectory(toNioPath());
        } catch (FileAlreadyExistsException e) {
          /*
           * That the resource already exists is only a problem for us if it's
           * not a directory.
           */
          if (!Files.isDirectory(Paths.get(e.getFile()))) {
            throw e;
          }
        }
      }
    
      @Override
      public void create(boolean force, boolean local) throws IOException {
        create(IResource.NONE, local);
      }
}