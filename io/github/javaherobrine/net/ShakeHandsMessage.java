package io.github.javaherobrine.net;
import java.beans.*;
@JavaBean
public class ShakeHandsMessage {
	public int id;
	public TransmissionStatus status;
	public TransmissionFormat format;
	public boolean connected;
}
