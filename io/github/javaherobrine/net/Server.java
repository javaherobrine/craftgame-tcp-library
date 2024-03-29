package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
public class Server extends Thread{
	private ServerSocket server;
	public Server(int port) throws IOException {
		server=new ServerSocket(port);
		EventHandler.handler=new EventHandler();
		EventHandler.handler.serverside=true;
		EventHandler.handler.start();
	}
	public void accept() throws IOException {
		Client c=new Client(server.accept());
		//TODO check
		c.start();
	}
	@Override
	public void run() {
		while(true) {
			try {
				accept();
			} catch (IOException e) {
				//Nothing should be processed
			}
		}
	}
}
