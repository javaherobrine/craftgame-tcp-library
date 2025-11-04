package io.github.javaherobrine.net;
import java.util.concurrent.*;
import java.io.*;
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
}
