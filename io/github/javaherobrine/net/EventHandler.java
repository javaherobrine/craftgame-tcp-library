package io.github.javaherobrine.net;
import java.util.concurrent.*;
public class EventHandler extends Thread{
	private BlockingQueue<EventContent> q=new LinkedBlockingQueue<>();
	boolean disconnected;
	@Override
	public void run() {
		while(!disconnected) {
			try {
				q.take().recvExec(true);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void push(EventContent c) {
		try {
			q.put(c);
		} catch (InterruptedException e) {
			//it shouldn't happen
		}
	}
}
