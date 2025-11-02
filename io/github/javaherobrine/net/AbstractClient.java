package io.github.javaherobrine.net;
import java.io.*;
/**
 * Maybe it will be used in UDP, SCTP or other transport layer protocols
 */
public abstract class AbstractClient extends Thread implements Closeable{
	public abstract void send(EventContent ec) throws IOException;
	protected abstract EventContent recv() throws IOException;
	@Override
	public void run() {
		try {
			while(true) {
				EventContent ec=recv();
				try {
					ec.recvExec(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
