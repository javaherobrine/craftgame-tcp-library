package io.github.javaherobrine.net;
import java.util.concurrent.*;
import java.io.*;
/**
 * Used for async output 
 */
public class OutputWorker extends Thread{
	private OutputStream out;
	public static final Runnable NULL_CALLBACK=()->{};
	private LinkedBlockingQueue<OutputEvent> queue=new LinkedBlockingQueue<>();
	public OutputWorker(OutputStream out) {
		this.out=out;
	}
	public void run() {
		while(true) {
			try {
				queue.take().recvExec(false);
			} catch (InterruptedException e) {
				return;//Thread::interrupt() is the right way to stop this thread
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
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
	public void write(byte[] data,Runnable callback) {
		queue.add(new OutputEvent(data,callback,out));
	}
	public void write(byte[] data) {
		write(data,NULL_CALLBACK);
	}
}
