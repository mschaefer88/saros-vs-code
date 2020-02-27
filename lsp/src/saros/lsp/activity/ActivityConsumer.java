package saros.lsp.activity;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import saros.activities.ChangeColorActivity;
import saros.activities.ChecksumActivity;
import saros.activities.ChecksumErrorActivity;
import saros.activities.EditorActivity;
import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.FolderMovedActivity;
import saros.activities.IActivity;
import saros.activities.JupiterActivity;
import saros.activities.NOPActivity;
import saros.activities.PermissionActivity;
import saros.activities.ProgressActivity;
import saros.activities.StartFollowingActivity;
import saros.activities.StopActivity;
import saros.activities.StopFollowingActivity;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.lsp.extensions.client.ISarosLanguageClient;
import saros.session.AbstractActivityConsumer;
import saros.session.ISarosSession;
import saros.session.User;
import saros.repackaged.picocontainer.Startable;

public class ActivityConsumer extends AbstractActivityConsumer implements Startable {
    private static final Logger LOG = Logger.getLogger(ActivityConsumer.class);

    private final ISarosSession session;

    public ActivityConsumer(ISarosSession session) {
        LOG.info("ActivityConsumer constructed");
        this.session = session;
    }

    @Override
    public void start() {
        LOG.info("ActivityConsumer start");
        this.session.addActivityConsumer(this, Priority.ACTIVE);
    }

    @Override
    public void stop() {
        LOG.info("ActivityConsumer stop");
        this.session.removeActivityConsumer(this);
    }

    @Override
    public void receive(ChangeColorActivity changeColorActivity) {
        LOG.info("ChangeColorActivity");
        super.receive(changeColorActivity);
    }

    @Override
    public void receive(ChecksumActivity checksumActivity) {
        LOG.info("ChecksumActivity");
        super.receive(checksumActivity);
    }

    @Override
    public void receive(ChecksumErrorActivity checksumErrorActivity) {
        LOG.info("ChecksumErrorActivity");
        super.receive(checksumErrorActivity);
    }

    @Override
    public void receive(EditorActivity editorActivity) {
        LOG.info("EditorActivity");
        super.receive(editorActivity);
    }

    @Override
    public void receive(FileActivity fileActivity) {
        LOG.info("FileActivity");
        super.receive(fileActivity);
    }

    @Override
    public void receive(FolderCreatedActivity folderCreatedActivity) {
        LOG.info("FolderCreatedActivity");
        super.receive(folderCreatedActivity);
    }

    @Override
    public void receive(FolderDeletedActivity folderDeletedActivity) {
        LOG.info("FolderDeletedActivity");
        super.receive(folderDeletedActivity);
    }

    @Override
    public void receive(FolderMovedActivity folderMovedActivity) {
        LOG.info("FolderMovedActivity");
        super.receive(folderMovedActivity);
    }

    @Override
    public void receive(JupiterActivity jupiterActivity) {
        LOG.info("JupiterActivity");
        super.receive(jupiterActivity);
    }

    @Override
    public void receive(NOPActivity nopActivity) {
        LOG.info("NOPActivity");
        super.receive(nopActivity);
    }

    @Override
    public void receive(PermissionActivity permissionActivity) {
        LOG.info("PermissionActivity");
        super.receive(permissionActivity);
    }

    @Override
    public void receive(ProgressActivity progressActivity) {
        LOG.info("ProgressActivity");
        super.receive(progressActivity);
    }

    @Override
    public void receive(StartFollowingActivity startFollowingActivity) {
        LOG.info("StartFollowingActivity");
        super.receive(startFollowingActivity);
    }

    @Override
    public void receive(StopActivity stopActivity) {
        LOG.info("StopActivity");
        super.receive(stopActivity);
    }

    @Override
    public void receive(StopFollowingActivity stopFollowingActivity) {
        LOG.info("StopFollowingActivity");
        super.receive(stopFollowingActivity);
    }

    @Override
    public void receive(TextEditActivity textEditActivity) {
        LOG.info("TextEditActivity");
        super.receive(textEditActivity);
    }

    @Override
    public void receive(TextSelectionActivity textSelectionActivity) {
        LOG.info("TextSelectionActivity");
        super.receive(textSelectionActivity);
    }

    @Override
    public void receive(ViewportActivity viewportActivity) {
        LOG.info("ViewportActivity");
        super.receive(viewportActivity);
    }    
}