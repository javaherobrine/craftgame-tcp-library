package io.github.javaherobrine.net;
import java.io.*;
public abstract class DataBlockProtocol extends Protocol{
	public DataEncoder encoder;
	@Override
	public void send(EventContent ec) throws IOException{
		out.write(encoder.encode(ec));
	}
}
