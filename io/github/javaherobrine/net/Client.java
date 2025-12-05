package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
/**
 * The TCP client, with handshake and protocol switching
 */
public abstract class Client extends Thread implements AbstractClient{
	protected Socket client;
	protected Protocol protocol;
	protected boolean disconnected=false;
	public Client(String host,int port) throws IOException {
		this(new Socket(host,port));
	}
	protected Client(Socket ac) throws IOException {//used in server
		client=ac;
		protocol=protocol();
		protocol.setSocket(client);
		if(protocol instanceof Protocol.NullProtocol) {
			throw new SocketException("Protocol not supported");
		}
		handshake();
		start();
	}
	@Override
	public void run() {
		while(protocol.hasNext()) {
			EventContent ec=recv();
			if(ec==null) {
			    break;
			}
			ec.recver=this;
			try {
				ec.recvExec(false);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void send(EventContent c) throws IOException {
		protocol.send(c);
	}
	public EventContent recv() {
		return protocol.next();
	}
	@Override
	public void close() throws IOException {
		disconnected=true;
		client.close();
	}
	public abstract Protocol protocol() throws IOException;
	public abstract void handshake() throws IOException;
}
