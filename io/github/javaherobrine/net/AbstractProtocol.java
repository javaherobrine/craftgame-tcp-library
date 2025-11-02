package io.github.javaherobrine.net;
import java.util.*;
import java.io.*;
/**
 * Maybe it will be used in other protocols(transport layer) like UDP and SCTP
 */
public abstract class AbstractProtocol implements Iterator<EventContent>, Cloneable {
	public abstract void send(EventContent ec) throws IOException;
}
