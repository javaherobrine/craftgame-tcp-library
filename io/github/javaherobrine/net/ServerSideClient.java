package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
import io.github.javaherobrine.*;
public abstract class ServerSideClient<T> extends Client implements AbstractServerSideClient{
	private EventDispatchThread EDT;
	public T player;
	public Server<T> s;
	protected ServerSideClient(Socket sc,Server<T> server,EventDispatchThread handle) throws IOException {
		super(sc);
		s=server;
		EDT=handle;
	}
	@Override
	public void run() {
		while(!disconnected) {
			EDT.put(recv());
		}
		s.removeClient(player);
	}
	@Override
	public void close() throws IOException{
		disconnected=true;
		client.close();
	}
}
