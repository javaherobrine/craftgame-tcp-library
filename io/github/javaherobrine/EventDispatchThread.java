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
				} catch (Throwable e) {
					e.printStackTrace(System.out);
					try {
						event.exception(e);
					} catch (Exception e1) {}
				}
				synchronized(event) {
					event.notifyAll();
				}
			}
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	public void put(AbstractEvent e) {
		queue.add(e);
	}
}
