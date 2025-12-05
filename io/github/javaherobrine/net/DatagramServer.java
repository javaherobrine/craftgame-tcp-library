package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
public abstract class DatagramServer extends DatagramClient{
	public abstract boolean dispatch(DatagramPacket packet);
	@Override
	public EventContent recv() throws IOException{
		DatagramPacket packet=recv0();
		while(dispatch(packet)) {
			packet=recv0();
		}
		return serializer.put(packet);
	}
}
