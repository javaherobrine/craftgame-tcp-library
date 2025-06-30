package io.github.javaherobrine.net;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
public class TLSUtils {
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
	@Deprecated
	public static SSLContext trustAllCert() throws KeyManagementException, NoSuchAlgorithmException {//man in middle is a problem
		SSLContext ssl=SSLContext.getInstance("SSL");
		ssl.init(null,new TrustManager[] {new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] c,String s) {}
				@Override
				public void checkServerTrusted(X509Certificate[] c,String s) {}
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}
		},new SecureRandom());
		return ssl;
	}
}
