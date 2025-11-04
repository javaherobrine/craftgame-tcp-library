package io.github.javaherobrine.net.speed;
import java.util.Random;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
/*
 * Used to simulate UDP packet loss
 */
public class PacketLosser {
	private static final Random entropy=new Random(System.currentTimeMillis());
	public static void send(DatagramSocket socket,DatagramPacket packet,double loss) throws IOException {
		if(entropy.nextDouble(0, 1)<loss) {//packet loss
			socket.send(packet);
		}
	}
	public static void recv(DatagramSocket socket,DatagramPacket packet,double loss) throws IOException {
		while(true) {
			socket.receive(packet);
			if(entropy.nextDouble(0,1)>loss) {
				return;
			}
			//packet loss
		}
	}
}
