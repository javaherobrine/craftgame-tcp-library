package io.github.javaherobrine.net;
import CC0.UDPClient.*;
public abstract class DatagramClient extends UDPClient{
	public static final int MAX_PACKET_SIZE=65507; //Max size for IP packet is 65535. IP's header is 20 bytes, and UDP's header is 8 bytes. So the max payload is 65507
}
