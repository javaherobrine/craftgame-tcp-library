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
import io.github.javaherobrine.net.speed.*;
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
	private SendQueue queue=null;
	private int MSS=65507;
	private boolean fail=false;
	private OutOfLength proc=OutOfLength.DISCARD;
	private final Dimension SIZE=new Dimension(595,85);
	private DataDialog dataView=new DataDialog(this);
	private double sendLoss=0,recvLoss=0;
	private boolean linewarp=false;
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
			if(b.length>MSS) {
				switch(proc) {
				case OutOfLength.DISCARD:
					byte[] temp=new byte[MSS];
					System.arraycopy(b,0,temp,0,MSS);
					PacketLosser.sendAsync(new SendDatagramEvent(socket,remote,b),EDT,sendLoss);
					b=temp;
					if(displaySend) {
						displayData(b,socket.getLocalSocketAddress(),remote,true);
					}
					break;
				case OutOfLength.SEND:
					SendDatagramEvent[] events=SendDatagramEvent.split(socket,remote,b, MSS);
					for(int j=0;j<events.length;++j) {
						PacketLosser.sendAsync(events[j],EDT,sendLoss);
						if(displaySend) {
							displayData(events[j].data(),socket.getLocalSocketAddress(),events[j].remote(),true);
						}
					}
					break;
				case OutOfLength.QUEUE:
					SendDatagramEvent[] events0=SendDatagramEvent.split(socket,remote,b,MSS);
					PacketLosser.sendAsync(events0[0],EDT,sendLoss);
					if(displaySend) {
						displayData(events0[0].data(),socket.getLocalSocketAddress(),events0[0].remote(),true);
					}
					for(int k=1;k<events0.length;++k) {
						queue.put(events0[k],"Data Fragment "+i);
					}
					break;
				}
			}else {
				PacketLosser.sendAsync(new SendDatagramEvent(socket,remote,b),EDT,sendLoss);
				if(displaySend) {
					displayData(b,socket.getLocalSocketAddress(),remote,true);
				}
			}
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(this, "Invalid Hostname", "Illegal Input", JOptionPane.ERROR_MESSAGE);
			fail=true;
			return;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Port must be an integer", "Illegal Input", JOptionPane.ERROR_MESSAGE);
			fail=true;
			return;
		}
		fail=false;
	};
	// Callback done
	@SuppressWarnings("unused")
	public DatagramSocketUI(DatagramSocket socket) {
		EDT.start();
		this.socket=socket;
		SwingUtilities.invokeLater(()->{
			setResizable(false);
			JPopupMenu ip=new JPopupMenu();
			JMenuItem warp=new JMenuItem("Warp Lines");
			warp.addActionListener(n->{
				if(linewarp) {
					warp.setText("Warp Lines");
					input.setLineWrap(false);
				}else {
					warp.setText("Don't warp lines");
					input.setLineWrap(true);
				}
				linewarp=!linewarp;
			});
			ip.add(warp);
			input.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {
					if(e.isPopupTrigger()) {
						ip.show(input,e.getX(),e.getY());
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if(e.isPopupTrigger()) {
						ip.show(input,e.getX(),e.getY());
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
			});
			setSize(600,600);
			setTitle("Datagram Socket, Local="+socket.getLocalSocketAddress());
			BoxLayout layout=new BoxLayout(display,BoxLayout.Y_AXIS);
			JScrollPane view=new JScrollPane(display);
			JScrollBar vertical=view.getVerticalScrollBar();
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
					JOptionPane.showMessageDialog(this, "Cannot write into a folder","Illegal Input",JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					FileOutputStream out=new FileOutputStream(f,cb.isSelected());
					OutputEvent to_dispatch=new OutputEvent(out,currentData);
					to_dispatch.close=true;
					EDT.put(to_dispatch);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(this, "Permission Denied", "Illegal Input",JOptionPane.ERROR_MESSAGE);
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
				vertical.setValue(vertical.getMaximum());
			});
			bottom.add(send);
			//Dialog done
			//menus
			JMenuBar bar=new JMenuBar();
			//Process Multicast DatagramSocket
			if(socket instanceof MulticastSocket multi) {
				try {
					multi.setBroadcast(true);
					JMenu multicast=new JMenu("Multicast");
					JMenuItem inter=new JMenuItem("Multicast Configurations");
					JComboBox<String> interfaces=new JComboBox<>();
					ArrayList<NetworkInterface> iList=new ArrayList<>();
					NetworkInterface.networkInterfaces().forEach(i->{
						iList.add(i);
						interfaces.addItem(i.getName());
					});
					interfaces.setSelectedIndex(0);
					//Dialog begin
					JDialog selectInterface=new JDialog(this,"Select Interface",true);
					selectInterface.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
					JPanel interf=new JPanel();
					interf.setLayout(new FlowLayout());
					interf.add(new JLabel("Interface="));
					interf.add(interfaces);
					selectInterface.add(interf);
					JPanel TTL=new JPanel();
					TTL.setLayout(new FlowLayout());
					TTL.add(new JLabel("TTL="));
					JTextField ttl=new JTextField();
					ttl.setColumns(3);
					ttl.setText(Integer.toString(multi.getTimeToLive()));
					((AbstractDocument)ttl.getDocument()).setDocumentFilter(new HexInput.NumberFilter(10));
					TTL.add(ttl);
					selectInterface.add(TTL);
					JCheckBox loop=new JCheckBox("Loopback");
					selectInterface.add(loop);
					selectInterface.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					selectInterface.addWindowListener(new WindowListener(){
						@Override
						public void windowOpened(WindowEvent e) {}
						@Override
						public void windowClosing(WindowEvent e) {
								try {
									int i=Integer.parseInt(ttl.getText());
									if(i>255) {
										JOptionPane.showMessageDialog(selectInterface,"TTL\'s range is [0,255]","Illegal Input",JOptionPane.ERROR_MESSAGE);
										return;
									}
									multi.setNetworkInterface(iList.get(interfaces.getSelectedIndex()));
									multi.setTimeToLive(i);
									multi.setOption(StandardSocketOptions.IP_MULTICAST_LOOP,loop.isSelected());
								}catch(NumberFormatException nfe) {
									JOptionPane.showMessageDialog(selectInterface,"TTL\'s range is [0,255]","Illegal Input",JOptionPane.ERROR_MESSAGE);
									return;
								} catch (IOException e1) {
									System.err.println("[WARNING] Bad Options");
								}
								selectInterface.dispose();
						}
						@Override
						public void windowClosed(WindowEvent e) {}
						@Override
						public void windowIconified(WindowEvent e) {}
						@Override
						public void windowDeiconified(WindowEvent e) {}
						@Override
						public void windowActivated(WindowEvent e) {}
						@Override
						public void windowDeactivated(WindowEvent e) {}			
					});
					//Dialog done
					inter.addActionListener(n->{
						selectInterface.setVisible(true);
					});
					JMenuItem toggle=new JMenuItem("Disable Multicast");
					toggle.addActionListener(n->{
						boolean enabled=inter.isEnabled();
						if(enabled) {
							try {
								multi.setBroadcast(false);
								inter.setEnabled(false);
								toggle.setText("Enable Multicast");
							} catch (SocketException e1) {}
						}else {
							try {
								multi.setBroadcast(true);
								inter.setEnabled(true);
								toggle.setText("Disable Multicast");
							}catch (SocketException e2) {}
						}
					});
					multicast.add(inter);
					multicast.add(toggle);
					bar.add(multicast);
				} catch (IOException e) {
					System.err.println("[FATAL] Multicast Not Supported");
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
						return;
					}
					if(!f.exists()) {
						JOptionPane.showMessageDialog(this,"java.io.FileNotFoundException: No such file","Illegal Input",JOptionPane.ERROR_MESSAGE);
						return;
					}
					long len=f.length();
					try {
						FileInputStream in=new FileInputStream(f);
						int s=(int)Math.min(MSS,len);
						SEND.accept(in.readNBytes(s));
						if(fail) {
							in.close();
							return;
						}
						len-=s;
						int i=0;
						while(len>0) {
							s=(int)Math.min(MSS,len);
							switch(proc) {
							case DISCARD:
								in.close();
								return;
							case SEND:
								SEND.accept(in.readNBytes(s));
								break;
							case QUEUE:
								queue.put(new SendDatagramEvent(socket,new InetSocketAddress(rHost.getText(),Integer.parseInt(rPort.getText())),in.readNBytes(s)),"File Fragment "+i);
								++i;
								break;
							}
							len-=s;
						}
						in.close();
					}catch(FileNotFoundException e) {
						JOptionPane.showMessageDialog(this, "","Illegal Input",JOptionPane.ERROR_MESSAGE);
					}catch(IOException e) {
						System.err.println("[ERROR] Error when reading files");
					}
				}
			});
			network.add(upload);
			JMenuItem size=new JMenuItem("Datagram Size Policy");
			JMenuItem showQueue=new JMenuItem("Show data queue");
			//Size Dialog
			JDialog sizeDialog=new JDialog(this,"Size Policy",true);
			sizeDialog.setLayout(new BorderLayout());
			JPanel sNorth=new JPanel();
			sNorth.setLayout(new FlowLayout());
			sNorth.add(new JLabel("Max Datagram Size="));
			JTextField sField=new JTextField("65507");
			((AbstractDocument)sField.getDocument()).setDocumentFilter(new HexInput.NumberFilter(10));
			sNorth.add(sField);
			ButtonGroup sBG=new ButtonGroup();
			sizeDialog.add(sNorth,BorderLayout.NORTH);
			sizeDialog.add(new JLabel("What if your data is out of max datagram length?"),BorderLayout.CENTER);
			JPanel sSouth=new JPanel();
			BoxLayout sBox=new BoxLayout(sSouth,BoxLayout.Y_AXIS);
			sSouth.setLayout(sBox);
			JRadioButton sDiscard=new JRadioButton("Just send data in interval [0,size-1]");
			sDiscard.setSelected(true);
			JRadioButton sSend=new JRadioButton("Seperate them and send them immediately");
			JRadioButton sQueue=new JRadioButton("Seperate them, send the first, put the rest into a queue");
			sBG.add(sDiscard);
			sBG.add(sSend);
			sBG.add(sQueue);
			sSouth.add(sDiscard);
			sSouth.add(sSend);
			sSouth.add(sQueue);
			sizeDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			sizeDialog.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					try {
						int i=Integer.parseInt(sField.getText());
						if(i>65507) {
							JOptionPane.showMessageDialog(sizeDialog,"Max Datagram Size must less than 65508","Illegal Input",JOptionPane.ERROR_MESSAGE);
							return;
						}else if(i==0) {
							JOptionPane.showMessageDialog(sizeDialog,"Max Datagram Size = 0?","Illegal Input",JOptionPane.ERROR_MESSAGE);
							return;
						}
						MSS=i;
						OutOfLength prev=proc;
						if(sDiscard.isSelected()) {
							proc=OutOfLength.DISCARD;
						}else if(sSend.isSelected()) {
							proc=OutOfLength.SEND;
						}else {
							proc=OutOfLength.QUEUE;
						}
						if(prev==OutOfLength.QUEUE&&proc!=prev&&queue.nonEmpty()) {
							if(JOptionPane.showConfirmDialog(sizeDialog,"If you switch to another way, all the data in the queue will be discarded, will you?","Confirmation",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) {
								proc=prev;
								sSend.setSelected(false);
								sDiscard.setSelected(false);
								sQueue.setSelected(true);
							}
						}else if(prev!=OutOfLength.QUEUE&&proc==OutOfLength.QUEUE) {
							queue=new SendQueue();
							showQueue.setEnabled(true);
						}
						if(proc!=OutOfLength.QUEUE&&prev==OutOfLength.QUEUE) {
							showQueue.setEnabled(false);
							queue.dispose();
							queue=null;
						}
						sizeDialog.dispose();
					}catch(NumberFormatException nfe) {
						JOptionPane.showMessageDialog(sizeDialog,"Max Datagram Size must less than 65508","Illegal Input",JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			sizeDialog.add(sSouth,BorderLayout.SOUTH);
			sizeDialog.pack();
			//Dialog end
			showQueue.addActionListener(n->{
				queue.setVisible(true);
			});
			showQueue.setEnabled(false);
			size.addActionListener(n->{
				sizeDialog.setVisible(true);
			});
			JMenuItem losser=new JMenuItem("Datagram Loss Emulator");
			//Dialog begin
			JDialog lDialog=new JDialog(this,"the RPM Package Manager",true);
			JPanel lNorth=new JPanel();
			lNorth.setLayout(new FlowLayout());
			JPanel lSouth=new JPanel();
			lSouth.setLayout(new FlowLayout());
			lDialog.setLayout(new BorderLayout());
			JTextField up=new JTextField("0");
			JTextField down=new JTextField("0");
			up.setColumns(8);
			down.setColumns(8);
			lNorth.add(new JLabel("P(Uplink\'s datagram loss)="));
			lNorth.add(up);
			lSouth.add(new JLabel("P(Downlink\'s datagram loss)="));
			lSouth.add(down);
			lDialog.add(lNorth,BorderLayout.NORTH);
			lDialog.add(lSouth,BorderLayout.SOUTH);
			lDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			lDialog.addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					double uL=0,dL=0;
					try {
						uL=Double.parseDouble(up.getText());
						dL=Double.parseDouble(down.getText());
					}catch(NumberFormatException e1) {
						JOptionPane.showMessageDialog(lDialog,"I\'d like to tell your Probability Theory professor about thus","Illegal Input",JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(uL<0||dL<0||uL>1||dL>1) {
						JOptionPane.showMessageDialog(lDialog,"I\'d like to tell your Probability Theory professor about thus","Illegal Input",JOptionPane.ERROR_MESSAGE);
						return;
					}
					sendLoss=uL;
					recvLoss=dL;
					lDialog.dispose();
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			lDialog.pack();
			//Dialog end
			losser.addActionListener(n->{
				lDialog.setVisible(true);
			});
			network.add(upload);
			network.add(size);
			network.add(showQueue);
			network.add(losser);
			bar.add(network);
			JMenu about=new JMenu("About");
			JMenuItem license=new JMenuItem("License");
			license.addActionListener(n->{
				try {
					InputStream in=SocketUI.class.getResourceAsStream("/LICENSE");
					String str=new String(in.readAllBytes());
					in.close();
					JOptionPane.showMessageDialog(this,str,"License",JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {}
			});
			about.add(license);
			JMenuItem author=new JMenuItem("Credits");
			author.addActionListener(n->{
					JOptionPane.showMessageDialog(this,SocketUI.CREDITS,"Credits",JOptionPane.INFORMATION_MESSAGE);
			});
			JMenuItem bugs=new JMenuItem("Bugs");
			bugs.addActionListener(n->{
				try {
					InputStream in=SocketUI.class.getResourceAsStream("/BUGS");
					String str=new String(in.readAllBytes());
					in.close();
					JOptionPane.showMessageDialog(this,str,"Bugs",JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {}
			});
			about.add(author);
			about.add(bugs);
			bar.add(about);
			setJMenuBar(bar);
			//menu done
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(bottom,BorderLayout.SOUTH);
			panel.add(new JScrollPane(input),BorderLayout.NORTH);
			setLayout(new BorderLayout());
			view.setViewportView(display);
			add(panel,BorderLayout.SOUTH);
			add(view,BorderLayout.CENTER);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setVisible(true);
		});
	}
	@Override
	public void dispose() {
		super.dispose();
		if(queue!=null) {
			queue.dispose();
		}
		dataView.dispose();
		socket.close();
		EDT.interrupt();
	}
	@Override
	public void run() {
		while(!socket.isClosed()) {
			try {
				DatagramPacket packet=new DatagramPacket(new byte[65528],65528);
				PacketLosser.recv(socket,packet,recvLoss);
				byte[] data=new byte[packet.getLength()];
				System.arraycopy(packet.getData(),0,data,0,packet.getLength());
				displayData(data,packet.getSocketAddress(),socket.getLocalSocketAddress(),false);
			} catch (IOException e) {}
		}
	}
	@SuppressWarnings("unused")
	private void displayData(byte[] data,SocketAddress src,SocketAddress dst,boolean send) {
		JPanel outer=new JPanel();
		outer.setLayout(new BorderLayout());
		outer.add(new JLabel(src.toString()+"->"+dst.toString()+" Preview:"),BorderLayout.NORTH);
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
		JMenuItem delete=new JMenuItem("Delete");
		delete.addActionListener(n->{
			display.remove(outer);
			revalidate();
			repaint();
			display.revalidate();
			display.repaint();
		});
		popup.add(delete);
		if(send) {
			JMenuItem resend=new JMenuItem("Retransmit this packet");
			resend.addActionListener(n->{
				EDT.put(new SendDatagramEvent(socket, dst, data));
			});
			popup.add(resend);
		}
		JTextArea hex=new JTextArea(),text=new JTextArea();
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<data.length;++i) {
			sb.append(Hex.toHex(data[i]));
			sb.append(' ');
			if(i%20==19) {
				sb.append('\n');
			}
		};	
		JMenuItem sd=new JMenuItem("Show Data");
		sd.addActionListener(n->{
			dataView.setData(data);
			dataView.setVisible(true);
		});
		popup.add(sd);
		hex.setPreferredSize(SIZE);
		text.setPreferredSize(SIZE);
		text.addMouseListener(listener);
		hex.addMouseListener(listener);
		hex.setEditable(false);
		text.setEditable(false);
		inner.add(text);
		inner.add(hex);
		outer.add(new JScrollPane(inner),BorderLayout.SOUTH);
		if(SwingUtilities.isEventDispatchThread()) {
			display.add(outer);
			revalidate();
			repaint();
			display.revalidate();
			display.repaint();
			hex.setText(sb.toString());
			text.setText(new String(data));
		}else {
			SwingUtilities.invokeLater(()->{
				display.add(outer);
				revalidate();
				repaint();
				display.revalidate();
				display.repaint();
				hex.setText(sb.toString());
				text.setText(new String(data));
			});
		}
	}
	/*
	 * Inner classes
	 */
	private class SendQueue extends JFrame{
		private JPanel display;
		private DataDialog hexView=new DataDialog(this);
		SendQueue(){
			SwingUtilities.invokeLater(()->{
				setTitle("Datagrams to be sent");
				setDefaultCloseOperation(HIDE_ON_CLOSE);
				setSize(600,600);
				display=new JPanel();
				BoxLayout layout=new BoxLayout(display,BoxLayout.Y_AXIS);
				display.setLayout(layout);
				add(new JScrollPane(display));
			});
		}
		@SuppressWarnings("unused")
		void put(SendDatagramEvent toBeSent,String description) {
			JTextField field=new JTextField(socket.getLocalSocketAddress().toString()+"->"+toBeSent.remote().toString()+": "+description);
			field.setEditable(false);
			JScrollPane pane=new JScrollPane(field);
			JPopupMenu popup=new JPopupMenu();
			JMenuItem delete=new JMenuItem("Delete");
			delete.addActionListener(n->{
				display.remove(pane);
				revalidate();
				repaint();
				display.revalidate();
				display.repaint();
			});
			JMenuItem send=new JMenuItem("Send");
			send.addActionListener(n->{
				display.remove(pane);
				EDT.put(toBeSent);
				if(displaySend) {
					displayData(toBeSent.data(),socket.getLocalSocketAddress(),toBeSent.remote(),true);
				}
				revalidate();
				repaint();
				display.revalidate();
				display.repaint();
			});
			JMenuItem show=new JMenuItem("Show Data Inside");
			show.addActionListener(n->{
				hexView.setData(toBeSent.data());
				hexView.setVisible(true);
			});
			popup.add(delete);
			popup.add(send);
			popup.add(show);
			field.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {
					if(e.isPopupTrigger()) {
						popup.show(field,e.getX(),e.getY());
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if(e.isPopupTrigger()) {
						popup.show(field,e.getX(),e.getY());
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
			});
			display.add(pane);
			if(isShowing()) {
				revalidate();
				repaint();
				display.revalidate();
				display.repaint();
			}
		}
		boolean nonEmpty() {
			return display.getComponents().length!=0;
		}
		@Override
		public void dispose() {
			hexView.dispose();
			super.dispose();
		}
	}
	private static enum OutOfLength{
		DISCARD,SEND,QUEUE
	}
	private static class DataDialog extends JDialog{
		private JTextArea text=new JTextArea(),hex=new JTextArea();
		DataDialog(JFrame parent) {
			super(parent,"View Data",true);
			text.setColumns(60);
			text.setRows(60);
			hex.setColumns(60);
			hex.setRows(60);
			CardLayout card=new CardLayout();
			setLayout(card);
			MouseListener listener=new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {
					if(e.getButton()==MouseEvent.BUTTON1) {
						card.next(DataDialog.this.getContentPane());
					}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
			};
			text.addMouseListener(listener);
			hex.addMouseListener(listener);
			add(new JScrollPane(text));
			add(new JScrollPane(hex));
			pack();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
		void setData(byte[] data){
			text.setText(new String(data));
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<data.length;++i) {
				sb.append(Hex.toHex(data[i]));
				sb.append(' ');
				if(i%20==19) {
					sb.append('\n');
				}
			}
			hex.setText(sb.toString());
		}
	}
}
