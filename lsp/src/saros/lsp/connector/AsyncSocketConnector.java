package saros.lsp.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AsyncSocketConnector implements Connector {

    private AsynchronousServerSocketChannel serverSocket;

    public AsyncSocketConnector(int port) throws IOException {
        this.serverSocket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost", port));
    }

    @Override
    public InputStream getInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }
}