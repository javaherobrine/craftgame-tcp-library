package io.github.javaherobrine.net;
import java.net.*;
import java.io.*;
import io.github.javaherobrine.*;
public class SendDatagramEvent extends AbstractEvent{
	private DatagramSocket local;
	private SocketAddress remote;
	private byte[] data;
	private int offset,length,seg;
	public static final int MAX_DGRAM_LENGTH=65536-8;
	@Override
	public void process() throws IOException{
		if(length<=seg) {
			DatagramPacket dgram=new DatagramPacket(data,offset,length,remote);
			local.send(dgram);
		}else {
			int to=offset+length;
			for(int i=offset;i<to;i+=length) {
				int current=Math.min(length, to-i);
				byte[] temp=new byte[current];
				System.arraycopy(data, i, temp, 0, current);
				DatagramPacket dgram=new DatagramPacket(temp,current,remote);
				local.send(dgram);
			}
		}
	}
	public SendDatagramEvent(DatagramSocket socket,SocketAddress addr,byte[] d,int off,int len,int seg) {
		local=socket;
		remote=addr;
		data=d;
		offset=off;
		length=len;
		this.seg=seg;
	}
	public SendDatagramEvent(DatagramSocket socket,SocketAddress addr,byte[] d) {
		this(socket,addr,d,0,d.length,MAX_DGRAM_LENGTH);
	}
	public SendDatagramEvent(DatagramSocket socket,SocketAddress addr,byte[] d,int offset,int length) {
		this(socket,addr,d,offset,length,MAX_DGRAM_LENGTH);
	}
}
