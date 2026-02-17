package io.github.javaherobrine.net;
import module java.base;
/**
 * If there's benefit to compress data, but they are actually events
 * use this
 */
public class MultiDeflaterOutputStream extends DeflaterOutputStream{
	public MultiDeflaterOutputStream(OutputStream out) {
		super(out);
	}
	public MultiDeflaterOutputStream(OutputStream out, boolean syncFlush) {
		super(out, syncFlush);
	}
	public MultiDeflaterOutputStream(OutputStream out, Deflater def, boolean syncFlush) {
		super(out, def, syncFlush);
	}
	public MultiDeflaterOutputStream(OutputStream out, Deflater def, int size, boolean syncFlush) {
		super(out, def, size, syncFlush);
	}
	public MultiDeflaterOutputStream(OutputStream out, Deflater def, int size) {
		super(out, def, size);
	}
	public MultiDeflaterOutputStream(OutputStream out, Deflater def) {
		super(out, def);
	}
	@Override
	public void finish() throws IOException {
		try {
			super.finish();
		} catch(IOException e) {
			throw e;
		}
		def.reset();
	}
}
