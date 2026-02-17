package io.github.javaherobrine.net;
import module java.base;
public class MultiInflaterInputStream extends InflaterInputStream{
	public MultiInflaterInputStream(InputStream in, Inflater inf, int size) {
		super(in, inf, size);
	}
	public MultiInflaterInputStream(InputStream in, Inflater inf) {
		super(in, inf);
	}
	public MultiInflaterInputStream(InputStream in) {
		super(in);
	}
	public void finish() {
		inf.reset();
	}
}
