package io.github.javaherobrine.net.ui;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.*;
public class DatagramSocketUI extends Thread{
	private JTextArea area=new JTextArea();
	private DatagramSocket socket;
	public DatagramSocketUI(DatagramSocket socket) {
		this.socket=socket;
		JFrame ui=new JFrame("UDP Socket, Local="+socket.getLocalAddress()+":"+socket.getLocalPort());
		SwingUtilities.invokeLater(()->{
			JMenuBar bar=new JMenuBar();
			area.setEditable(false);
			ui.setLayout(new BorderLayout());
			
			//Process Multicast DatagramSocket
			
			if(socket instanceof MulticastSocket multi) {
				try {
					JMenu multicast=new JMenu("Multicast");
					JMenuItem inter=new JMenuItem("Select Interface");
					JComboBox<String> interfaces=new JComboBox<>();
					ArrayList<NetworkInterface> iList=new ArrayList<>();
					NetworkInterface.networkInterfaces().forEach(i->{
						iList.add(i);
						
						interfaces.addItem(i.getName());
					});
					JDialog selectInterface=new JDialog(ui,"Select Interface",true);
					selectInterface.setLayout(new BoxLayout(ui,BoxLayout.Y_AXIS));
				} catch (SocketException e) {
					System.err.println("No Interface");
				}
			}
		});
	}
	@Override
	public void run() {
		
	}
}
