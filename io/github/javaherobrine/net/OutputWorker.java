package io.github.javaherobrine.net;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
/**
 * Used for async output 
 */
public class OutputWorker extends Thread{
	private OutputStream out;
	public static final Runnable NULL_CALLBACK=()->{};
	private LinkedBlockingQueue<EventContent> queue=new LinkedBlockingQueue<>();
	public OutputWorker(OutputStream out) {
		super("I/O Thread: Write");
		this.out=out;
	}
	public void run() {
		while(true) {
			try {
				queue.take().recvExec(false);
			} catch (InterruptedException e) {
				return;//Thread::interrupt() is the right way to stop this thread
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	@SuppressWarnings("serial")
	private static class OutputEvent extends EventContent{
		private Runnable callback;
		private byte[] data;
		private OutputStream out;
		@Override
		public void recvExec(boolean serverside) throws IOException {
			out.write(data);
			callback.run();
		}
		OutputEvent(byte[] data,Runnable callback,OutputStream out){
			this.data=data;
			this.callback=callback;
			this.out=out;
		}
	}
	@SuppressWarnings("serial")
	private static class Transfer extends EventContent{
		private InputStream in;
		private Runnable callback;
		private OutputStream out;
		@Override
		public void recvExec(boolean serverside) throws Exception {
			in.transferTo(out);
			in.close();
			callback.run();
		}
		Transfer(OutputStream o,InputStream i,Runnable cb){
			in=i;
			out=o;
			callback=cb;
		}
	}
	@SuppressWarnings("serial")
	private static class Urgent extends EventContent{
		private Socket socket;
		private Runnable callback;
		private byte[] data;
		@Override
		public void recvExec(boolean serverside) throws Exception {
			for(int i=0;i<data.length;++i) {
				socket.sendUrgentData(data[i]);
			}
			callback.run();
		}
		public Urgent(Socket socket,Runnable callback,byte[] data) {
			this.socket=socket;
			this.callback=callback;
			this.data=data;
		}
	}
	public void write(byte[] data,Runnable callback) {
		queue.add(new OutputEvent(data,callback,out));
	}
	public void write(byte[] data) {
		write(data,NULL_CALLBACK);
	}
	public void transfer(InputStream in,Runnable callback) {
		queue.add(new Transfer(out,in,callback));
	}
	public void transfer(InputStream in) {
		transfer(in,NULL_CALLBACK);
	}
	public void urgent(Socket socket,byte[] data,Runnable callback) {
		queue.add(new Urgent(socket,callback,data));
	}
	public void urgent(Socket socket,byte[] data) {
		urgent(socket,data,NULL_CALLBACK);
	}
}
