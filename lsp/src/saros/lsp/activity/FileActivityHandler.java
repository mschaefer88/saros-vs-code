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

//TODO: naming für File/Folder
public class FileActivityHandler extends AbstractActivityConsumer {//TODO: naming Consumer?
    private static final Logger LOG = Logger.getLogger(FileActivityHandler.class);

    private EditorManager editorManager;

    public FileActivityHandler(ISarosSession session, EditorManager editorManager) {
        LOG.debug("CTOR");
        this.editorManager = editorManager;

        session.addActivityConsumer(this, Priority.ACTIVE);
    }

    @Override
    public void receive(FileActivity fileActivity) {
        LOG.debug(String.format("Retrieved activity '%s'", fileActivity.getType()));

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
            folderCreatedActivity.getPath().getFolder().create(false, true);
        } catch (IOException e) {
            LOG.error(e); //TODO: warn?
        }
    }

    @Override
    public void receive(FolderDeletedActivity folderDeletedActivity) {
        try {
            folderDeletedActivity.getPath().getFolder().delete(0);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void handleDelete(FileActivity fileActivity) {
        LOG.debug("handleDelete");
        this.editorManager.closeEditor(fileActivity.getPath());

        IFile file = fileActivity.getPath().getFile();
        if (file.exists()) {
            try {
                file.delete(0);
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    private void handleCreate(FileActivity fileActivity) {
        LOG.debug("handleCreate");
        // final String encoding = fileActivity.getEncoding(); TODO
        final byte[] newContent = fileActivity.getContent();
        IFile file = fileActivity.getPath().getFile();

        byte[] actualContent = null;

        if (file.exists()) actualContent = FileUtils.getLocalFileContent(file); //TODO: use editor manager?

        if (!Arrays.equals(newContent, actualContent)) {
            FileUtils.writeFile(new ByteArrayInputStream(newContent), file);
        } else {
            LOG.debug("FileActivity " + fileActivity + " dropped (same content)");
        }

        // if (encoding != null) updateFileEncoding(encoding, file); TODO
    }
}