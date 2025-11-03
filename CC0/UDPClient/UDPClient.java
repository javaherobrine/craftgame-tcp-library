package CC0.UDPClient;


import io.github.javaherobrine.net.AbstractClient;
import io.github.javaherobrine.net.AbstractProtocol;
import io.github.javaherobrine.net.EventContent;

import java.io.IOException;
import java.net.*;

public abstract class UDPClient extends AbstractClient {
    private final DatagramSocket socket;
    private final InetSocketAddress remote;
    protected volatile boolean disconnected = false;
    private Serializer serializer;
    protected AbstractProtocol protocol;

    public UDPClient(String host, int port) throws SocketException, UnknownHostException {
        this(new InetSocketAddress(InetAddress.getByName(host), port));
    }

    public UDPClient(InetSocketAddress remote) throws SocketException {
        this.remote = remote;
        this.socket = new DatagramSocket(); // let OS choose local port
        this.serializer = new JavaSerializer();
        this.protocol = new UDPProtocol(this.socket, this.remote, this.serializer);
        try {
            handshake();
        } catch (IOException e) {
            // handshake failure should close socket and rethrow as runtime
            socket.close();
            throw new RuntimeException("handshake failed", e);
        }
        start();
    }

    /**
     * 服务端在接收到数据包并识别出客户端时可使用此构造器来封装为 UDPClient 实例
     */
    protected UDPClient(DatagramSocket socket, InetSocketAddress remote) throws SocketException {
        this.remote = remote;
        this.socket = socket;
        this.serializer = new JavaSerializer();
        this.protocol = new UDPProtocol(this.socket, this.remote, this.serializer);
        try {
            handshake();
        } catch (IOException e) {
            socket.close();
            throw new RuntimeException("handshake failed", e);
        }
        start();
    }

    public void setSerializer(Serializer s) {
        this.serializer = s;
        // 如果已经创建了 protocol，可以用新的 serializer 替换 protocol（这里重建）
        this.protocol = new UDPProtocol(this.socket, this.remote, this.serializer);
    }

    @Override
    public void run() {
        try {
            while (protocol.hasNext()) {
                EventContent ec = protocol.next();
                if (ec == null) break;
                ec.recver = this;
                try {
                    ec.recvExec(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            // UDPProtocol 将 IO 异常包装为 RuntimeException
            if (!disconnected) e.printStackTrace();
        }
    }

    @Override
    public void send(EventContent ec) throws IOException {
        protocol.send(ec);
    }

    @Override
    protected EventContent recv() throws IOException {
        // 为兼容 AbstractClient 的 recv() 语义，直接从 protocol 获取下一条（可能抛出 RuntimeException）
        try {
            return protocol.next();
        } catch (RuntimeException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) throw (IOException) t;
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        disconnected = true;
        if (protocol instanceof UDPProtocol) ((UDPProtocol) protocol).close();
        socket.close();
    }

    public abstract void handshake() throws IOException;
}
