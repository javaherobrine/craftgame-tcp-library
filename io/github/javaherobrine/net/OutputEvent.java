package io.github.javaherobrine.net;
import java.io.*;
import io.github.javaherobrine.*;
public class OutputEvent extends AbstractEvent{
	private OutputStream out;
	private byte[] data;
	private int offset,length;
	public boolean close=false;
	public OutputEvent(OutputStream out,byte[] data) {
		this(out,data,0,data.length);
	}
	public OutputEvent(OutputStream out,byte[] data,int offset,int length) {
		this.out=out;
		this.data=data;
		this.offset=offset;
		this.length=length;
	}
	public OutputEvent(OutputStream out,int data) {
		this(out,null,data,0);
	}
	@Override
	public synchronized void process() throws Exception {
		if(data==null) {
			out.write(offset);
		}else {
			out.write(data,offset,length);
		}
		if(close) {
			out.close();
		}
	}
	public void setData(byte[] data) {
		setData(data,0,data.length);
	}
	public synchronized void setData(byte[] data,int offset,int length) {
		this.data=data;
		this.offset=offset;
		this.length=length;
	}
	public synchronized void setData(int data) {
		setData(null,data,0);
	}
	public synchronized void setStream(OutputStream out) {
		this.out=out;
	}
}
