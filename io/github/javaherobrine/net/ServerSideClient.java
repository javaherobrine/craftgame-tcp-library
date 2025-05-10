package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
public class ServerSideClient extends Client {
	public Server server;
	private EventHandler handle;
	protected ServerSideClient(Socket ac,Server serv,EventHandler handler) throws IOException {
		super(ac);
		// TODO Auto-generated constructor stub
		handle=handler;
		server=serv;
	}
	@Override
	public void run() {
		while(disconnected) {
			try {
				handle.push(recv());
			} catch (IOException e) {
				//TODO process exception
				e.printStackTrace();
			}
		}
	}
}
