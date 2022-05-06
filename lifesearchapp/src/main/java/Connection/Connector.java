package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public interface Connector {
    // connect in timeout milliseconds
    void connect(int timeout) throws IOException;

    /*
     * connect to peer, may no timeout
     */
    void connect() throws IOException;

    // close connection between local and peer
    void closeConnection() throws IOException;

    // close connection and release resource
    void close() throws IOException;

    // reconnect to peer
    void reconnect() throws IOException;

    // get read stream to read data from peer
    InputStream getInputStream() throws IOException;

    // get write stream to write data to peer
    OutputStream getOutputStream() throws IOException;

    // if connector is in connected state
    boolean isConnected();

    // if input stream is shutdown
    boolean isInputShutdown();

    // if output stream is shutdown
    boolean isOutputShutdown();

    // set read-write timeout for connector
    void setTimeout(int timeout) throws SocketException;

    // get connector current timeout
    int getTimeout() throws SocketException;

    // set connect timeout for current connector
    void setConnectTimeout(int timeout) throws SocketException;

    // get connect timeout for current connector
    int getConnectTimeout() throws SocketException;

    // shutdown input stream
    void shutdownInput() throws IOException;

    // shutdown output stream
    void shutdownOutput() throws IOException;

    // if connector is closed
    boolean isClosed();

    // if current connection is available
    boolean isConnectionClosed();

    // set long alive connection
    void setLongConnection(boolean on) throws SocketException;
}
