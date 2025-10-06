package io.github.javaherobrine.net;
public interface DataEncoder {
	EventContent decode(byte[] input);
	byte[] encode(EventContent ec);
}
