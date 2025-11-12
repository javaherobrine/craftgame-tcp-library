package io.github.javaherobrine.net.ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.util.function.*;
import io.github.javaherobrine.net.speed.*;
import io.github.javaherobrine.net.*;
import io.github.javaherobrine.*;
public class SocketUI extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;
	private JTextArea show=new JTextArea();
	private JTextArea input=new JTextArea();
	static final JFileChooser CHOOSER=new JFileChooser();
	private Socket socket;
	private LimitedOutputStream out;
	private LimitedInputStream in;
	private int currentValue;
	private EventDispatchThread EDT;
	private IntPredicate currentJudger=ALLOW;
	private HexView viewHex=new HexView();
	private OutputEvent write=new OutputEvent(null,null);
	/*
	 * It works like a function pointer.
	 * It's ugly, but with less temporary objects.
	 * If I put this lambda expression in the parameter of callback, there will be tons of temporary objects.
	 * They are all callbacks
	 */
	@SuppressWarnings("unused")
	private static final IntPredicate ALLOW=i-> false;
	@SuppressWarnings("unused")
	private static final IntPredicate BLOCKED=i-> true;
	private final Consumer<byte[]> SEND_HEX=block->{
		try {
			out.write(block);
		} catch (IOException e) {}
	};
	private final Consumer<byte[]> SEND_URG=block->{
		EDT.put(new UrgentDataEvent(socket,block));
		viewHex.insertURG(Hex.toHex(block));
	};
	private final LongConsumer LIMIT_UP=speed->{
		out.speed=speed;
	};
	private final LongConsumer LIMIT_DOWN=speed->{
		in.speed=speed;
	};
	private final Runnable APPEND_STRING=()->{
		show.append(Character.toString((char)currentValue));
		viewHex.insertRecv(Hex.toHex((byte)currentValue));
	};
	private final Runnable DISPLAY_IN_SCREEN=()->{
		try {
			SwingUtilities.invokeAndWait(APPEND_STRING);
		} catch (InvocationTargetException | InterruptedException e) {}
	};
	private final Runnable TRANSFER_TO_FILE=()->{
		write.setData(currentValue);
		synchronized(write) {
			EDT.put(write);
			try {
				write.wait();
			} catch (InterruptedException e) {}
		}
	};
	private Runnable processor=DISPLAY_IN_SCREEN;
	/*
	 * Callback over, now initialize some fields
	 */
	static {
		CHOOSER.setMultiSelectionEnabled(false);
		CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		CHOOSER.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return true;
			}
			@Override
			public String getDescription() {
				return "All Files(*/*)";
			}
		});
	}
	@Override
	public void run() {//Don't invoke interrupt
		try {
			while(true) {
				currentValue=in.read();
				if(currentValue==-1) {
					break;
				}
				if(currentJudger.test(currentValue)) {
					try {
						synchronized(this) {
							wait();
						}
					} catch (InterruptedException e) {}
				}
				processor.run();
			}
			EDT.interrupt();
			show.append("\nstream closed");
		}catch(IOException e) {
			EDT.interrupt();
			show.append("\nstream closed");
		}
	}
	@SuppressWarnings("unused")
	public SocketUI(Socket soc) {
		EDT=new EventDispatchThread();
		EDT.start();
		socket=soc;
		try {
			in=new LimitedInputStream(soc.getInputStream());
			out=new LimitedOutputStream(soc.getOutputStream());
		} catch (IOException e) {
			System.err.println("Invalid socket");
			dispose();
			return;
		}
		SwingUtilities.invokeLater(()->{
			input.setRows(4);
			JMenuBar bar=new JMenuBar();
			JMenu file=new JMenu("Network");
			JMenuItem upload=new JMenuItem("Upload");
			upload.addActionListener(m->{
				if(CHOOSER.showDialog(this, "Upload")==0) {
					try {
						File f=CHOOSER.getSelectedFile();
						if(f.isDirectory()) {
							JOptionPane.showMessageDialog(this,"Can't upload a folder, maybe you can archive it?","Illegal Input", JOptionPane.ERROR_MESSAGE);
						}
						InputStream in=new BufferedInputStream(new FileInputStream(f));
						EDT.put(new InputTransferEvent(in,out));
						show.append("\n"+f.length()+"bytes from file were sent\n");
					}catch (Exception e) {}
				}
			});
			JMenuItem close=new JMenuItem("Disconnect");
			close.addActionListener(n->{
				try {
					socket.close();
				}catch (Exception e) {}
			});
			JMenuItem sendBinary=new JMenuItem("Send Binary Data");
			sendBinary.addActionListener(n->{
				HexInput.input(SEND_HEX);
			});
			JMenuItem limit=new JMenuItem("Limit Data Rate");
			limit.addActionListener(n->{
				SpeedInput.limit(LIMIT_UP, LIMIT_DOWN);
			});
			JMenuItem urg=new JMenuItem("Urgent Data");
			urg.addActionListener(n->{
				HexInput.input(SEND_URG);
			});
			JMenu help=new JMenu("About");
			JMenuItem license=new JMenuItem("License");
			license.addActionListener(n->{
				try {
					InputStream in=SocketUI.class.getResourceAsStream("/LICENSE");
					String str=new String(in.readAllBytes());
					in.close();
					JOptionPane.showMessageDialog(this,str,"License",JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e) {}
			});
			JMenuItem author=new JMenuItem("Authors");
			author.addActionListener(n->{
				JOptionPane.showMessageDialog(this, "Java_Herobrine from CraftGame Studio\nB-a-s-d-y from USTC");
			});
			help.add(author);
			JMenu data=new JMenu("Data");
			JMenu trunc=new JMenu("Block the stream");
			JMenuItem trunc_now=new JMenuItem("Block Now");
			JMenuItem distrunc=new JMenuItem("Resume the stream");
			JMenuItem clear=new JMenuItem("Clear Screen");
			JMenuItem vh=new JMenuItem("View Raw Data as Hex");
			vh.addActionListener(n->{
				viewHex.setVisible(true);
			});
			clear.addActionListener(n->{
				show.setText("");
			});
			distrunc.addActionListener(n->{
				SocketUI.this.notify();
			});
			trunc.add(trunc_now);
			data.add(vh);
			data.add(clear);
			data.add(trunc);
			data.add(distrunc);
			file.add(upload);
			file.add(close);
			file.add(sendBinary);
			file.add(limit);
			file.add(urg);
			bar.add(file);
			help.add(license);
			bar.add(data);
			bar.add(help);
			setJMenuBar(bar);
			JPanel panel=new JPanel();
			panel.setLayout(new BorderLayout());
			show.setEditable(false);
			JButton send=new JButton("Send");
			JScrollPane scroll0=new JScrollPane();
			scroll0.getViewport().setOpaque(false);
			scroll0.setViewportView(show);
			JScrollPane scroll1=new JScrollPane();
			scroll1.getViewport().setOpaque(false);
			scroll1.setViewportView(input);
			panel.add(scroll0,BorderLayout.CENTER);
			panel.add(scroll1,BorderLayout.SOUTH);
			send.addActionListener(n->{
				viewHex.insertSend(Hex.toHex(input.getText().getBytes()));
				input.setEditable(false);
				EDT.put(new OutputEvent(out,input.getText().getBytes()));
				show.append(input.getText());
				input.setEditable(true);
				input.setText("");
			});
			setLayout(new BorderLayout());
			setTitle(soc.toString());
			add(panel,BorderLayout.CENTER);
			add(send,BorderLayout.SOUTH);
			setSize(600,600);
			addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					try {
						socket.close();
					} catch (IOException e1) {}
					dispose();
					EDT.interrupt();
				}
				@Override
				public void windowClosed(WindowEvent e) {
					viewHex.dispose();
					HexInput.INSTANCE.dispose();
					System.exit(0);
				}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			setVisible(true);
		});
	}
	/*
	 * Only used internally 
	 */
	private static class SpeedInput extends JFrame{
		private LongConsumer up;
		private LongConsumer down;
		private static final long serialVersionUID = 1L;
		private static final SpeedInput INSTANCE=new SpeedInput();
		@SuppressWarnings("unused")
		private SpeedInput() {
			SwingUtilities.invokeLater(()->{
				setTitle("Speed Limiter");
				JPanel upload=new JPanel();
				upload.setLayout(new FlowLayout());
				JLabel unit=new JLabel("byte(s)/s");
				JLabel unit0=new JLabel("byte(s)/s");
				JLabel u=new JLabel("Max Upload Speed: ");
				JLabel d=new JLabel("Max Download Speed: ");
				JFormattedTextField u1=new JFormattedTextField();
				u1.setValue(0L);
				upload.add(u);
				upload.add(u1);
				upload.add(unit);
				JFormattedTextField u2=new JFormattedTextField();
				u2.setValue(0L);
				JPanel download=new JPanel();
				download.add(d);
				download.add(u2);
				download.add(unit0);
				setLayout(new BorderLayout());
				add(upload,BorderLayout.NORTH);
				add(download,BorderLayout.CENTER);
				JLabel no=new JLabel("0 for no limitation");
				JPanel button=new JPanel();
				button.setLayout(new FlowLayout());
				button.add(no);
				JButton OK=new JButton("OK");
				OK.addActionListener(n->{
					up.accept((Long)u1.getValue());
					down.accept((Long)u2.getValue());
					dispose();
				});
				JButton cancel=new JButton("Cancel");
				cancel.addActionListener(n->{
					dispose();
				});
				button.add(OK);
				button.add(cancel);
				add(button,BorderLayout.SOUTH);
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				pack();
			});
		}
		public static void limit(LongConsumer u,LongConsumer d) {
			INSTANCE.up=u;
			INSTANCE.down=d;
			SwingUtilities.invokeLater(()->{
				INSTANCE.pack();
				INSTANCE.setVisible(true);
			});
		}
	}
	private static class HexView extends JFrame{
		private static final long serialVersionUID = 1L;
		private JTextPane pane;
		private static final SimpleAttributeSet RED=new SimpleAttributeSet(),BLUE=new SimpleAttributeSet(),GREEN=new SimpleAttributeSet();
		@SuppressWarnings("unused")
		public HexView() {
			StyleConstants.setForeground(GREEN, Color.green);
			StyleConstants.setForeground(BLUE, Color.blue);
			StyleConstants.setForeground(RED, Color.red);
			SwingUtilities.invokeLater(()->{
				setTitle("Red = Urgent Data, Blue = Data Received, Green = Data Sent");
				pane=new JTextPane();
				pane.setEditable(false);
				setSize(500,500);
				add(pane);
				JMenuBar bar=new JMenuBar();
				JMenu data=new JMenu("Data");
				JMenuItem cls=new JMenuItem("Clear Screen");
				setDefaultCloseOperation(HIDE_ON_CLOSE);
				cls.addActionListener(n->{
					pane.setText("");
				});
				data.add(cls);
				bar.add(data);
				setJMenuBar(bar);
			});
		}
		public void insertSend(String str) {
			try {
				pane.getDocument().insertString(pane.getText().length(), str, GREEN);
			} catch (BadLocationException e) {} 
		}
		public void insertRecv(String str) {
			try {
				pane.getDocument().insertString(pane.getText().length(), str, BLUE);
			} catch (BadLocationException e) {}
		}
		public void insertURG(String str) {
			try {
				pane.getDocument().insertString(pane.getText().length(), str, RED);
			} catch (BadLocationException e) {}
		}
	}
}
