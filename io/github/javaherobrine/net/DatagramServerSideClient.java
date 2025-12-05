package io.github.javaherobrine.net;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
public class DatagramServerSideClient extends DeligatedClient implements AbstractServerSideClient{
	public DatagramServerSideClient(DatagramServer deligated) {
		super(deligated);
	}
	public LinkedBlockingQueue<DatagramPacket> blocked=new LinkedBlockingQueue<>();
	@Override
	public EventContent recv() throws IOException {
		try {
			return ((DatagramClient)deligate).serializer.put(blocked.take());
		} catch (InterruptedException e) {
			return null;
		}
	}
	public void dispatch(DatagramPacket packet) {
		blocked.add(packet);
	}
}
