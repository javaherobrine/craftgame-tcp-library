package io.github.javaherobrine.net;
import java.io.*;
public class FixedLengthProtocol extends DataBlockProtocol{
	public int length;
	@Override
	public EventContent next() {
		try {
			return encoder.decode(in.readNBytes(length));
		} catch (IOException e) {
			exception=e;
			return null;
		}
	}
}
