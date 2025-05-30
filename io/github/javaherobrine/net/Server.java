package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
import java.util.*;
public class Server extends Thread implements Closeable{
	private ServerSocket server;
	private Map<String,ServerSideClient> connected=new HashMap<String,ServerSideClient>();
	EventHandler handler=new EventHandler();
	public Server(int port) throws IOException {
		server=new ServerSocket(port);
		start();
	}
	public Client removeClient(String name) {
		synchronized(connected) {
			return connected.remove(name);
		}
	}
	@SuppressWarnings("resource")
	public void accept() throws IOException {
		ServerSideClient c=new ServerSideClient(server.accept(),this,handler);
		if(c.protocol instanceof Protocol.NullProtocol) {
			c.close();//bad protocol
			return;
		}
		c.start();
	}
	@Override
	public void run() {
		while(!server.isClosed()) {
			try {
				accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void close() throws IOException{
		server.close();
		handler.close();
		synchronized(connected) {
			connected.values().forEach(n->{
				try {
					//TODO Close Connectons
				} catch (Exception e) {}
			});
			connected.clear();
		}
	}
	public Client getClient(String player) {
		synchronized(connected) {
			return connected.get(player);
		}
	}
	public int connected() {
		synchronized(connected) {
			return connected.size();
		}
	}
	public void sendAll(EventContent ec) throws IOException{
		synchronized(connected) {
			connected.values().forEach(n->{
				try {
					n.send(ec);
				} catch (IOException e) {}
			});
		}
	}
}
