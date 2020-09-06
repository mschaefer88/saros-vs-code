package saros.lsp.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.filesystem.IFile;
import saros.lsp.editor.EditorManager;
import saros.lsp.utils.FileUtils;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

public class FileActivityHandler extends AbstractActivityConsumer {
  private static final Logger LOG = Logger.getLogger(FileActivityHandler.class);

  private EditorManager editorManager;

  public FileActivityHandler(ISarosSession session, EditorManager editorManager) {
    this.editorManager = editorManager;
    session.addActivityConsumer(this, Priority.ACTIVE);
  }

  @Override
  public void receive(FileActivity fileActivity) {
    switch (fileActivity.getType()) {
      case CREATED:
        this.handleCreate(fileActivity);
        break;
      case REMOVED:
        this.handleDelete(fileActivity);
        break;
      case MOVED:
        // NOP
        break;
    }
  }

  @Override
  public void receive(FolderCreatedActivity folderCreatedActivity) {
    try {
      folderCreatedActivity.getResource().create();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void receive(FolderDeletedActivity folderDeletedActivity) {
    try {
      folderDeletedActivity.getResource().delete();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  private void handleDelete(FileActivity fileActivity) {
    this.editorManager.closeEditor(fileActivity.getResource());

    IFile file = fileActivity.getResource();
    if (file.exists()) {
      try {
        file.delete();
      } catch (IOException e) {
        LOG.error(e);
      }
    }
  }

  private void handleCreate(FileActivity fileActivity) {
    final byte[] newContent = fileActivity.getContent();
    IFile file = fileActivity.getResource();

    byte[] actualContent = null;

    if (file.exists()) {
      actualContent = this.editorManager.getContent(file).getBytes();
    }

    if (!Arrays.equals(newContent, actualContent)) {
      FileUtils.writeFile(new ByteArrayInputStream(newContent), file);
    } else {
      LOG.debug("FileActivity " + fileActivity + " dropped (same content)");
    }
  }
}
