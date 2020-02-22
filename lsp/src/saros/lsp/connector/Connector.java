package saros.lsp.connector;

import java.io.InputStream;
import java.io.OutputStream;

public interface Connector {
    InputStream getInputStream();
    OutputStream getOutputStream();
}