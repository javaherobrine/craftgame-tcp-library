package io.github.javaherobrine.net;
import java.io.*;
import io.github.javaherobrine.*;
public class InputTransferEvent extends AbstractEvent{
	private InputStream in;
	private OutputStream out;
	public InputTransferEvent(InputStream i,OutputStream o) {
		in=i;
		out=o;
	}
	@Override
	public void process() throws Exception {
		in.transferTo(out);
		in.close();
	}
	@Override
	public void exception(Throwable t) throws Exception{
		in.close();
	}
}
