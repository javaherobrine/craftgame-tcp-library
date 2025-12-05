package io.github.javaherobrine.net;
import java.io.*;
public class DeligatedClient implements AbstractClient{
	protected AbstractClient deligate;
	@Override
	public void close() throws IOException {
		deligate.close();
	}
	@Override
	public void send(EventContent ec) throws IOException {
		deligate.send(ec);;
	}
	@Override
	public EventContent recv() throws IOException {
		return deligate.recv();
	}
	public DeligatedClient(AbstractClient deligated) {
		deligate=deligated;
	}
}
