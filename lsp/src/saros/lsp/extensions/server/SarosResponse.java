package saros.lsp.extensions.server;

import org.apache.log4j.Logger;

public class SarosResponse {
    public boolean success;

    public String error;

    private static final Logger LOG = Logger.getLogger(SarosResponse.class);

    public SarosResponse(Throwable throwable) {
        LOG.error(throwable);
        throwable.printStackTrace();

        this.success = false;
        this.error = throwable.getMessage();
    }

    public SarosResponse() {
        this.success = true;
    }
}