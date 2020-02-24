package saros.lsp.service;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;
import saros.lsp.SarosLauncher;

//TODO: Deprecated since not used -> to null in server?
/** Empty implementation of the workspace service. */
public class WorkspaceServiceStub implements WorkspaceService {

  private static final Logger LOG = Logger.getLogger(SarosLauncher.class);

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {//TODO dat Location Änderbar? -> bessere Vorführung
    LOG.info("didChangeConfiguration");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    for (FileEvent event : params.getChanges()) {
      LOG.info(String.format("Changed: '%s' (%s)", event.getUri(), event.getType()));
    }
  }
}
