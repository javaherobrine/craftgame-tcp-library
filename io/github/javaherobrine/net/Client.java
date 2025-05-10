package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
public class Client extends Thread implements Closeable{
	Socket client;
	protected ObjectInputStream in;
	protected ObjectOutputStream out;
	protected boolean disconnected=false;
	public Client(String host,int port) throws IOException {
		client=new Socket(host,port);
		in=new ObjectInputStream(client.getInputStream());
		out=new ObjectOutputStream(client.getOutputStream());
		//TODO check
	}
	Client(Socket ac) throws IOException {//used in server
		client=ac;
		in=new ObjectInputStream(client.getInputStream());
		out=new ObjectOutputStream(client.getOutputStream());
	}
	@Override
	public void run() {
		while(!disconnected) {
			try {
				((EventContent)in.readObject()).recvExec(false);
			} catch (ClassNotFoundException e) {
				//that is to say,the extensions are different
				//TODO show an error and then close the connection
			} catch (IOException e) {
				//that is to say,the connection is broken
				//TODO show a message that the connection is broken
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void send(EventContent c) throws IOException {
		out.writeObject(c);
	}
	protected EventContent recv() throws IOException{
		try {
			EventContent r=(EventContent) in.readObject();
			r.recver=this;
			return r;
		} catch (ClassNotFoundException e) {
			//TODO process
		}
		return null;
	}
	@Override
	public void close() throws IOException {
		//Send disconnect event
		client.close();
	}
}
