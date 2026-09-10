package io.github.javaherobrine;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public class EventDispatchThread implements Runnable, AutoCloseable{
	private Thread EDT;
	private AtomicBoolean b=new AtomicBoolean(true);
	private BlockingQueue<AbstractEvent> queue=new LinkedBlockingDeque<>();
	public EventDispatchThread() {
		EDT=new Thread(this);
		EDT.start();
	}
	@Override
	public void run() {
		try {
			while(b.get()) {
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
	@Override
	public void close() throws Exception {
		shutdown();
	}
	public void shutdown() {
		EDT.interrupt();
		b.set(false);
	}
}
