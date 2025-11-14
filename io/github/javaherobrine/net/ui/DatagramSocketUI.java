package io.github.javaherobrine.net.ui;
import java.util.function.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import io.github.javaherobrine.*;
import io.github.javaherobrine.net.*;
@SuppressWarnings("serial")
public class DatagramSocketUI extends JFrame implements Runnable{
	private byte[] currentData;
	private DatagramSocket socket;
	private JTextField rHost=new JTextField();
	private JTextField rPort=new JTextField();
	private JTextArea input=new JTextArea();
	private JPanel display=new JPanel();
	private JDialog choose=new JDialog(this,"Transfer to File",true);
	private EventDispatchThread EDT=new EventDispatchThread();
	private boolean displaySend=true;
	private int MSS=65528;
	/*
	 * They are all callbacks, too
	 * Why is Java so Object-Oriented???
	 * Why not use Function Pointers??? 
	 */
	private Consumer<byte[]> SEND=b->{
		try {
			InetAddress IP=InetAddress.getByName(rHost.getText());
			int i=Integer.parseInt(rPort.getText());
			if(i>65535||i<=0) {
				JOptionPane.showMessageDialog(this, "Port's range is (0,65536)", "Illegal Input", JOptionPane.ERROR_MESSAGE);
				return;
			}
			InetSocketAddress remote=new InetSocketAddress(IP,i);
			EDT.put(new SendDatagramEvent(socket, remote, b));
			displayData(b,socket.getLocalSocketAddress(),remote);
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(this, "Invalid Hostname", "Illegal Input", JOptionPane.ERROR_MESSAGE);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Port must be an integer", "Illegal Input", JOptionPane.ERROR_MESSAGE);
		}
	};
	// Callback done
	@SuppressWarnings("unused")
	public DatagramSocketUI(DatagramSocket socket) {
		EDT.start();
		this.socket=socket;
		SwingUtilities.invokeLater(()->{
			setSize(600,600);
			setTitle("Datagram Socket, Local="+socket.getLocalSocketAddress());
			BoxLayout layout=new BoxLayout(display,BoxLayout.Y_AXIS);
			JScrollPane view=new JScrollPane(display);
			display.setLayout(layout);
			input.setRows(5);
			//Dialogs
			choose.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			JPanel cNorth=new JPanel();
			cNorth.setLayout(new FlowLayout());
			cNorth.add(new JLabel("File: "));
			JTextField inputFile=new JTextField();
			inputFile.setColumns(20);
			cNorth.add(inputFile);
			JButton select=new JButton("Select File");
			select.addActionListener(n->{
				if(SocketUI.CHOOSER.showSaveDialog(this)==0) {
					inputFile.setText(SocketUI.CHOOSER.getSelectedFile().getAbsolutePath());
				}
			});
			cNorth.add(select);
			JPanel cSouth=new JPanel();
			cSouth.setLayout(new FlowLayout());
			JCheckBox cb=new JCheckBox("Append");
			cSouth.add(cb);
			JButton OK=new JButton("OK");
			JButton cancel=new JButton("Cancel");
			OK.addActionListener(n->{
				File f=new File(inputFile.getText());
				if(f.isDirectory()) {
					JOptionPane.showMessageDialog(this, "Cannot write into a folder","Invalid Input",JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					FileOutputStream out=new FileOutputStream(f,cb.isSelected());
					OutputEvent to_dispatch=new OutputEvent(out,currentData);
					to_dispatch.close=true;
					EDT.put(to_dispatch);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(this, "Permission Denied", "Invalid Input",JOptionPane.ERROR_MESSAGE);
					return;
				} 
				choose.dispose();
			});
			cancel.addActionListener(n->{
				choose.dispose();
			});
			cSouth.add(OK);
			cSouth.add(cancel);
			choose.add(cNorth,BorderLayout.NORTH);
			choose.add(cSouth,BorderLayout.SOUTH);
			choose.pack();
			JPanel bottom=new JPanel();
			bottom.setLayout(new FlowLayout());
			bottom.add(new JLabel("Remote Host="));
			rHost=new JTextField();
			rHost.setColumns(15);
			bottom.add(rHost);
			bottom.add(new JLabel("Remote Port="));
			rPort=new JTextField();
			rPort.setColumns(5);
			((AbstractDocument)rPort.getDocument()).setDocumentFilter(new HexInput.NumberFilter());
			bottom.add(rPort);
			JButton send=new JButton("Send");
			send.addActionListener(n->{
				SEND.accept(input.getText().getBytes());
				input.setText("");
			});
			bottom.add(send);
			//Dialog done
			//menus
			JMenuBar bar=new JMenuBar();
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
					JDialog selectInterface=new JDialog(this,"Select Interface",true);
					selectInterface.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
				} catch (SocketException e) {
					System.err.println("No Interface");
				}
			}
			//multicast done
			JMenu network=new JMenu("Network");
			JMenuItem upload=new JMenuItem("Upload File");
			upload.addActionListener(n->{
				if(SocketUI.CHOOSER.showDialog(this,"OK")==JFileChooser.APPROVE_OPTION) {
					File f=SocketUI.CHOOSER.getSelectedFile();
					if(f.isDirectory()) {
						JOptionPane.showMessageDialog(this,"Can't update a folder, maybe you can archive them?","Illegal Input", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			setJMenuBar(bar);
			//menu done
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(bottom,BorderLayout.SOUTH);
			panel.add(new JScrollPane(input),BorderLayout.NORTH);
			setLayout(new BorderLayout());
			view.setViewportView(display);
			add(panel,BorderLayout.SOUTH);
			add(view,BorderLayout.CENTER);
			setVisible(true);
		});
	}
	@Override
	public void run() {
		while(!socket.isClosed()) {
			try {
				DatagramPacket packet=new DatagramPacket(new byte[65528],65528);
				socket.receive(packet);
				byte[] data=new byte[packet.getLength()];
				System.arraycopy(packet.getData(),0,data,0,packet.getLength());
				displayData(data,packet.getSocketAddress(),socket.getLocalSocketAddress());
			} catch (IOException e) {}
		}
	}
	private void displayData(byte[] data,SocketAddress src,SocketAddress dst) {
		JPanel outer=new JPanel();
		outer.setLayout(new BorderLayout());
		outer.add(new JLabel(src.toString()+"->"+dst.toString()),BorderLayout.NORTH);
		JPanel inner=new JPanel();
		CardLayout card=new CardLayout();
		inner.setLayout(card);
		JPopupMenu popup=new JPopupMenu();
		JMenuItem toggle=new JMenuItem("Toggle View(Left Click)");
		toggle.addActionListener(n->{
			card.next(inner);
		});
		popup.add(toggle);
		JMenuItem file=new JMenuItem("Transfer to File");
		file.addActionListener(n->{
			currentData=data;
			choose.setVisible(true);
		});
		MouseListener listener=new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()) {
					popup.show(inner,e.getX(),e.getY());
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()) {
					popup.show(inner,e.getX(),e.getY());
				}
				if(e.getButton()==MouseEvent.BUTTON1) {
					card.next(inner);
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		};
		popup.add(file);
		JTextArea hex=new JTextArea(),text=new JTextArea();
		hex.setRows(5);
		text.setRows(5);
		hex.setText(Hex.toHex(data));
		text.setText(new String(data));
		text.addMouseListener(listener);
		hex.addMouseListener(listener);
		hex.setEditable(false);
		text.setEditable(false);
		inner.add(new JScrollPane(text));
		inner.add(new JScrollPane(hex));
		outer.add(inner,BorderLayout.SOUTH);
		SwingUtilities.invokeLater(()->{
			display.add(outer);
			revalidate();
			repaint();
			display.revalidate();
			display.repaint();
		});
	}
}
