package io.github.javaherobrine.net;
import java.net.*;
import io.github.javaherobrine.*;
public class UrgentDataEvent extends AbstractEvent{
	private Socket socket;
	private byte[] data;
	private int offset,length;
	public UrgentDataEvent(Socket tcp,byte[] d,int off,int len) {
		socket=tcp;
		data=d;
		offset=off;
		length=len;
	}
	public UrgentDataEvent(Socket tcp,byte[] d) {
		this(tcp,d,0,d.length);
	}
	public UrgentDataEvent(Socket tcp,int d) {
		this(tcp,null,d,0);
	}
	@Override
	public void process() throws Exception {
		if(data==null) {
			socket.sendUrgentData(offset);
			return;
		}
		for(int i=offset;i<offset+length;++i) {
			socket.sendUrgentData(data[i]);
		}
	}
}
