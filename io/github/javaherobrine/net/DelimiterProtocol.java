package io.github.javaherobrine.net;
import java.io.IOException;
import io.github.javaherobrine.*;
public class DelimiterProtocol extends DataBlockProtocol{
	public Delimiter delimiter;
	@Override
	public EventContent next() {
		try {
			while(delimiter.add(in.read())) {}
			return encoder.decode(delimiter.poll());
		} catch (IOException e) {
			delimiter.poll();
			exception=e;
			return null;
		}
	}
}
