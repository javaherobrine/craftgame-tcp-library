package io.github.javaherobrine.net.speed;
import java.io.*;
/**
 * Used to limit the download speed
 */
public class LimitedInputStream extends FilterInputStream {
	public long speed;//bytes per second, 0 for no limitation
	public static final long FACTOR1=1000;
	public static final long FACTOR2=1000000;
	public LimitedInputStream(InputStream in) {
		super(in);
	}
	@Override
	public int read() throws IOException{
		pause(1);
		return in.read();
	}
	@Override
	public int read(byte[] b,int off,int len) throws IOException {
		int i=in.read(b, off, len);
		pause(i);
		return i;
	}
	/**
	 * create latency to simulate the low speed link
	 * @param bytes data length
	 */
	private void pause(int bytes) {
		if(speed==0) return;
		long b=bytes*FACTOR1;
		long millis=b/speed;
		b%=speed;
		b*=FACTOR2;
		try {
			Thread.sleep(millis, (int) ((int)b/speed));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static InputStream rawStream(InputStream in) {
		return in instanceof LimitedInputStream ? ((LimitedInputStream)in).in:in;
	}
}
