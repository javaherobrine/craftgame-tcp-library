package CC0.UDPClient;

import io.github.javaherobrine.net.AbstractProtocol;
import io.github.javaherobrine.net.EventContent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

/**
 * UDP 版的 Protocol，实现了 AbstractProtocol（Iterator<EventContent> + send）
 * 将 DatagramSocket 的收发封装成协议层，使用外部传入的 Serializer 完成序列化。
 */
public class UDPProtocol extends AbstractProtocol {
    private final DatagramSocket socket;
    private final InetSocketAddress remote;
    private final Serializer serializer;
    private volatile boolean closed = false;

    public UDPProtocol(DatagramSocket socket, InetSocketAddress remote, Serializer serializer) {
        this.socket = socket;
        this.remote = remote;
        this.serializer = serializer;
    }

    @Override
    public void send(EventContent ec) throws IOException {
        byte[] data = serializer.serialize(ec);
        if (data.length > 65507) {
            throw new IOException("payload too large for single UDP packet: " + data.length);
        }
        DatagramPacket p = new DatagramPacket(data, data.length, remote.getAddress(), remote.getPort());
        socket.send(p);
    }

    @Override
    public boolean hasNext() {
        return !closed && !socket.isClosed();
    }

    @Override
    public EventContent next() {
        if (!hasNext()) throw new NoSuchElementException("protocol closed");
        byte[] buf = new byte[65535];
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(p);
            int len = p.getLength();
            byte[] data = new byte[len];
            System.arraycopy(p.getData(), p.getOffset(), data, 0, len);
            try {
                return serializer.deserialize(data);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to deserialize EventContent", e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        closed = true;
    }
}