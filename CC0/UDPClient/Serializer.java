package CC0.UDPClient;
import java.io.IOException;
import java.net.*;
import io.github.javaherobrine.net.*;
public interface Serializer {
	DatagramPacket[] serialize(EventContent ec, SocketAddress remote) throws IOException;
	EventContent put(DatagramPacket packet);
}
