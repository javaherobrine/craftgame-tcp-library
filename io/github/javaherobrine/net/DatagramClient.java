package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
import CC0.UDPClient.*;
public abstract class DatagramClient implements AbstractClient,Runnable{
	public static final int MAX_PACKET_SIZE=65507; //Max size for IP packet is 65535. IP's header is 20 bytes, and UDP's header is 8 bytes. So the max payload is 65507
	private DatagramSocket socket;
	protected Serializer serializer;
	private SocketAddress remote;
	public abstract void handshake() throws IOException;
	@Override
	public void run() {
		try {
			while(true) {
				EventContent ec=recv();
				try {
					ec.recvExec(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void close() throws IOException {
		socket.close();
	}
	@Override
	public void send(EventContent ec) throws IOException {
		DatagramPacket[] packets=serializer.serialize(ec, remote);
		for(DatagramPacket packet:packets) {
			socket.send(packet);
		}
	}
	@Override
	public EventContent recv() throws IOException {
		return serializer.put(recv0());
	}
	protected DatagramPacket recv0() throws IOException{
		byte[] data=new byte[MAX_PACKET_SIZE];
		DatagramPacket packet=new DatagramPacket(data,MAX_PACKET_SIZE);
		socket.receive(packet);
		return packet;
	}
}
