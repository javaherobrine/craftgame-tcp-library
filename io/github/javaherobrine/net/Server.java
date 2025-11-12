package io.github.javaherobrine.net;
import java.io.*;
import java.net.*;
import java.util.*;
import io.github.javaherobrine.*;
public abstract class Server extends Thread implements Closeable{
	private ServerSocket server;
	private Map<String,ServerSideClient> connected=new HashMap<String,ServerSideClient>();
	EventDispatchThread EDT=new EventDispatchThread();
	public Server(int port) throws IOException {
		server=new ServerSocket(port);
		start();
	}
	public Client removeClient(String name) {
		synchronized(connected) {
			return connected.remove(name);
		}
	}
	public abstract ServerSideClient accept(Socket socket);
	public void accept() throws IOException {
		ServerSideClient c=accept(server.accept());
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
		EDT.interrupt();
		synchronized(connected) {
			connected.values().forEach(n->{
				try {
					n.close();
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
