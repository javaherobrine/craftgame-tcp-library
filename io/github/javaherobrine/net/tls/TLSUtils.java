package io.github.javaherobrine.net.tls;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
public class TLSUtils {
	/**
	 * It's dangerous!!!
	 */
	@Deprecated(forRemoval=false)
	public static final TrustManager[] TRUST_ALL=new TrustManager[] {
		new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] c,String s) {}
			@Override
			public void checkServerTrusted(X509Certificate[] c,String s) {}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		}
	};
	public static final KeyManager[] NO_KEY=null;
	public static Socket defaultSSLSocket(String host,int port) throws IOException {
   return SSLSocketFactory.getDefault().createSocket(host,port);
	}
	public static SSLContext getFromKSFile(String file,String version,String encrypt,char[] pwd) throws IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, CertificateException{
		SSLContext context = SSLContext.getInstance(version);
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in=new FileInputStream(file);
		keystore.load(in,pwd);
		in.close();
		KeyManagerFactory km=KeyManagerFactory.getInstance(encrypt);
		km.init(keystore,pwd);
		TrustManagerFactory tmf=TrustManagerFactory.getInstance(encrypt);
		tmf.init(keystore);
		context.init(km.getKeyManagers(),tmf.getTrustManagers(),null);
		return context;
	}
	/**
	 * It's dangerous!!!
	 */
	@Deprecated
	public static SSLContext trustAllCert() throws KeyManagementException, NoSuchAlgorithmException {//man in middle is a problem
		SSLContext ssl=SSLContext.getInstance("SSL");
		ssl.init(NO_KEY,TRUST_ALL,new SecureRandom());
		return ssl;
	}
	/**
	 * It's dangerous!!!
	 */
	@Deprecated
	public static SSLContext trustAllCert(String version) throws KeyManagementException, NoSuchAlgorithmException {//man in middle is a problem
		SSLContext ssl=SSLContext.getInstance(version);
		ssl.init(NO_KEY,TRUST_ALL,new SecureRandom());
		return ssl;
	}
	public static SSLSocket proxiedTLS(SSLSocketFactory factory,Proxy proxy,SocketAddress remote) throws IOException{
		Socket underlying=new Socket(proxy);
		underlying.connect(remote);
		InetSocketAddress proxyT=(InetSocketAddress)proxy.address();
		if(proxy!=Proxy.NO_PROXY)
			return (SSLSocket)factory.createSocket(underlying, proxyT.getHostString(),proxyT.getPort(), true);
		return (SSLSocket)factory.createSocket(underlying, ((InetSocketAddress)remote).getHostString(),((InetSocketAddress)remote).getPort(), true);
	}
	public static TrustManager[] trust(String file,char[] pwd) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		KeyStore keystore=KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in=new FileInputStream(file);
		keystore.load(in, pwd);
		in.close();
		TrustManagerFactory tmf=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keystore);
		return tmf.getTrustManagers();
	}
	public static KeyManager[] key(String file,char[] pwd) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
		KeyStore keystore=KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in=new FileInputStream(file);
		keystore.load(in, pwd);
		in.close();
		KeyManagerFactory tmf=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(keystore,pwd);
		return tmf.getKeyManagers();
	}
}
