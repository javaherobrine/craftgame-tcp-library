package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
public abstract class ServerSideClient extends Client{
	private EventHandler handler;
	public String player;
	public Server s;
	protected ServerSideClient(Socket sc,Server server,EventHandler handle) throws IOException {
		super(sc);
		s=server;
		handler=handle;
	}
	@Override
	public void run() {
		while(!disconnected) {
			try {
				handler.push(recv());
			} catch (InterruptedException e) {//the connection was closed
				e.printStackTrace();
				break;
			}
		}
		s.removeClient(player);
	}
	@Override
	public void close() throws IOException{
		disconnected=true;
		client.close();
	}
}
