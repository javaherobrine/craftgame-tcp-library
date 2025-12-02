package io.github.javaherobrine.net.tls;
import java.net.*;
import javax.net.ssl.*;
import java.io.*;
/**
 * It's not a DatagramSocket object
 * And it's not an AbstractClient too :-)
 */
public class DTLSDatagramSocketClient {
	private DatagramSocket delegate;
	private SSLEngine dtls;
	private SocketAddress remote;
	private int retry=60;
	public void setRetry(int retry) {
		this.retry = retry;
	}
	public DTLSDatagramSocketClient(DatagramSocket delegate,SocketAddress remote,SSLEngine dtls) throws SocketException {
		this.dtls=dtls;
		this.delegate=delegate;
		this.remote=remote;
		delegate.connect(remote);
		//hand shake
	}
	public void handshake() throws SSLException, IOException{
		dtls.beginHandshake();
		
	}
}
