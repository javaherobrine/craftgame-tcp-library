package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
/**
 * Specific to TCP connections
 */
public abstract class Protocol extends AbstractProtocol{
	protected Throwable exception = null;
	protected InputStream in;
	protected OutputStream out;
	public abstract void send(EventContent ec) throws IOException;
	protected Protocol(Socket soc) throws IOException {
		setSocket(soc);
	}
	protected Protocol() {
	}
	@Override
	public boolean hasNext() {
		return exception == null;
	}
	public Throwable getException() {
		return exception;
	}
	public void setSocket(Socket soc) throws IOException {
		in = soc.getInputStream();
		out = soc.getOutputStream();
	}
	// define a invalid protocol, to indicate protocol not supported
	static class NullProtocol extends Protocol {
		@Override
		public boolean hasNext() {
			return false;
		}
		@Override
		public EventContent next() {
			return null;
		}
		@Override
		public void send(EventContent ec) throws IOException {
		}
		@Override
		public Protocol clone() {
			return null;
		}
	}
}
