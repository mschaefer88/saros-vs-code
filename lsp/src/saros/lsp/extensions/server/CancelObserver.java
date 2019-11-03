package saros.lsp.extensions.server;

public interface CancelObserver {
    boolean tryCancel(String token);
}