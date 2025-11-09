package io.github.javaherobrine;
import java.util.concurrent.*;
public class EventDispatchThread extends Thread{
	private BlockingQueue<AbstractEvent> queue=new LinkedBlockingDeque<>();
	@Override
	public void run() {
		try {
			while(true) {
				AbstractEvent event=queue.take();
				try {
					event.process();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(InterruptedException e) {}
	}
	public void put(AbstractEvent e) {
		queue.add(e);
	}
}
