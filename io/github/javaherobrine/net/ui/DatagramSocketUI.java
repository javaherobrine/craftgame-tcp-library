package io.github.javaherobrine.net.ui;
import javax.swing.*;
import java.awt.*;
import java.net.*;
public class DatagramSocketUI extends Thread{
	private JTextArea area=new JTextArea();
	private DatagramSocket socket;
	public DatagramSocketUI(DatagramSocket socket) {
		this.socket=socket;
		JFrame ui=new JFrame(socket.toString());
		SwingUtilities.invokeLater(()->{
			area.setEditable(false);
			ui.setLayout(new BorderLayout());
		});
	}
	@Override
	public void run() {
		
	}
}
