package io.github.javaherobrine.net;
import java.io.*;
/**
 * Maybe it will be used in UDP, SCTP or other transport layer protocols
 */
public interface AbstractClient extends Closeable{
	void send(EventContent ec) throws IOException;
	EventContent recv() throws IOException;
}
