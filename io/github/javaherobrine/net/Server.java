package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
public class Server extends Thread implements Closeable{
	private ServerSocket server;
	private EventHandler handler;
	private boolean open=true;
	public Server(int port) throws IOException {
		server=new ServerSocket(port);
		handler=new EventHandler();
		handler.start();
	}
	public void accept() throws IOException {
		ServerSideClient c=new ServerSideClient(server.accept(),this,handler);
		//TODO check
		c.start();
	}
	@Override
	public void run() {
		while(open) {
			try {
				accept();
			} catch (IOException e) {
				//Nothing should be processed
			}
		}
	}
	@Override
	public void close() throws IOException{
		open=false;
		server.close();
		handler.interrupt();
	}
}
