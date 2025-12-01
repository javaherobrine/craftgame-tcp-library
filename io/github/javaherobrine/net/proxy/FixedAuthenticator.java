package io.github.javaherobrine.net.proxy;
import java.net.*;
public class FixedAuthenticator extends Authenticator{
	private String username;
	private char[] password;
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username,password);
	}
	public FixedAuthenticator(String username, char[] password) {
		this.username = username;
		this.password = password;
	}
}
