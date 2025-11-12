package io.github.javaherobrine;
public abstract class AbstractEvent {
	public abstract void process() throws Exception;
	public void exception(Throwable except) throws Exception{}
}
