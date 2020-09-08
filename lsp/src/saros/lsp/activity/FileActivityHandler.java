package saros.lsp.activity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.log4j.Logger;
import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.filesystem.IFile;
import saros.lsp.editor.EditorManager;
import saros.lsp.utils.FileUtils;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;

/** 
 * The ActivityHandler is responsible for receiving activities
 * regarding file/folder creations and deletions and applying them
 * to the local workspace.
 */
public class FileActivityHandler extends AbstractActivityConsumer {
  private static final Logger LOG = Logger.getLogger(FileActivityHandler.class);

  private final EditorManager editorManager;

  public FileActivityHandler(final ISarosSession session, final EditorManager editorManager) {
    this.editorManager = editorManager;
    session.addActivityConsumer(this, Priority.ACTIVE);
  }

  @Override
  public void receive(final FileActivity fileActivity) {
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
  public void receive(final FolderCreatedActivity folderCreatedActivity) {
    try {
      folderCreatedActivity.getResource().create();
    } catch (final IOException e) {
      LOG.error(e);
    }
  }

  @Override
  public void receive(final FolderDeletedActivity folderDeletedActivity) {
    try {
      folderDeletedActivity.getResource().delete();
    } catch (final IOException e) {
      LOG.error(e);
    }
  }

  /**
   * Applies the received deletion of a file or folder.
   * 
   * @param fileActivity {@link FileActivity} of type {@link FileActivity#Type} REMOVED
   */
  private void handleDelete(final FileActivity fileActivity) {
    this.editorManager.closeEditor(fileActivity.getResource());

    final IFile file = fileActivity.getResource();
    if (file.exists()) {
      try {
        file.delete();
      } catch (final IOException e) {
        LOG.error(e);
      }
    }
  }

  /**
   * Applies the received creation of a file or folder.
   * 
   * @param fileActivity {@link FileActivity} of type {@link FileActivity#Type} CREATED
   */
  private void handleCreate(final FileActivity fileActivity) {
    final byte[] newContent = fileActivity.getContent();
    final IFile file = fileActivity.getResource();

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
