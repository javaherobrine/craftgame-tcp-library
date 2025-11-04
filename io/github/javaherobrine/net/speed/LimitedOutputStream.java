package io.github.javaherobrine.net.speed;
import java.io.*;
/**
 * Used to limit the upload speed
 */
public class LimitedOutputStream extends FilterOutputStream {
	public long speed=0;//bytes per second, 0 for no limitation
	public static final long FACTOR1=1000;
	public static final long FACTOR2=1000000;
	public LimitedOutputStream(OutputStream out) {
		super(out);
	}
	@Override
	public void write(int i) throws IOException{
		pause(1);
		out.write(i);
	}
	@Override
	public void write(byte[] b,int off,int len) throws IOException{
		pause(len);
		out.write(b, off, len);
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
	public static OutputStream rawStream(OutputStream out) {
		return out instanceof LimitedOutputStream ? ((LimitedOutputStream)out).out:out;
	}
}
