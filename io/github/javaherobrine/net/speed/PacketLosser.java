package io.github.javaherobrine.net.speed;
import java.util.Random;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import io.github.javaherobrine.EventDispatchThread;
import io.github.javaherobrine.net.SendDatagramEvent;
/*
 * Used to simulate UDP packet loss
 */
public class PacketLosser {
	private static final Random entropy=new Random(System.currentTimeMillis());
	public static void send(DatagramSocket socket,DatagramPacket packet,double loss) throws IOException {
		if(entropy.nextDouble(0, 1)>loss) {
			socket.send(packet);
		}//packet loss
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
	public static void sendAsync(SendDatagramEvent data,EventDispatchThread edt,double loss) {
		if(entropy.nextDouble(0,1)>loss) {
			edt.put(data);
		}
	}
}
