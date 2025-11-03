package CC0.UDPClient;


import java.io.IOException;

/**
 * EventContent 与字节数组互转
 */
public interface Serializer {
    byte[] serialize(io.github.javaherobrine.net.EventContent ec) throws IOException;
    io.github.javaherobrine.net.EventContent deserialize(byte[] data) throws IOException, ClassNotFoundException;
}
