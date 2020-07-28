package saros.lsp.extensions.server;

public class SarosResultResponse<T> extends SarosResponse {
    public T result;

    public SarosResultResponse(T result) {
        super();
        this.result = result;
    }
    
    public SarosResultResponse(Throwable throwable) {
        super(throwable);
    }
}