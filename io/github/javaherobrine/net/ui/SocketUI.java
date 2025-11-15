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
import javax.swing.text.AbstractDocument;
import java.util.function.*;
import java.util.stream.*;
import io.github.javaherobrine.net.speed.*;
import io.github.javaherobrine.net.*;
import io.github.javaherobrine.*;
public class SocketUI extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;
	static final JFileChooser CHOOSER=new JFileChooser();
	//Fields
	private JTextArea show=new JTextArea();
	private JTextArea input=new JTextArea();
	private JMenuItem lastSelected;
	private Socket socket;
	private LimitedOutputStream out;
	private LimitedInputStream in;
	private int currentValue;
	private EventDispatchThread EDT;
	private Blocker currentJudger=BLOCKED;
	private Blocker lastJudger;
	private final HexView viewHex=new HexView();
	private final OutputEvent write=new OutputEvent(null,null,0,0);
	private final FixedLength FL_INSTANCE=new FixedLength();
	private final Blocker FLB=new Blocker(false,true,FL_INSTANCE);
	private boolean nRedirect=true;//don't output to file
	private boolean nDisplay=false;//don't display in the screen
	private boolean nSend=false;//don't display send in the screen
	private boolean confirmed=false;//whether the dialog is confirmed
	private boolean blocked=false;
	private boolean lastBlocked;
	private String TITLE;
	private String TITLE_BLOCKED;
	private byte[] HEX_TEMP;
	private boolean passed;
	private Delimiter delimiter;
	/*
	 * Fields done, then callbacks
	 * It works like a function pointer.
	 * It's ugly, but with less temporary objects.
	 * If I put this lambda expression in the parameter of callback, there will be tons of temporary objects.
	 * They are all callbacks
	 */
	@SuppressWarnings("unused")
	private static final Blocker ALLOW=new Blocker(false,false,i-> false);
	@SuppressWarnings("unused")
	private static final Blocker BLOCKED=new Blocker(true,false,i-> true);
	private final Consumer<byte[]> PASS_RETURN_VALUE=block->{
		passed=true;
		HEX_TEMP=block;
	};
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
		if(nDisplay) {
			return;
		}
		try {
			SwingUtilities.invokeAndWait(APPEND_STRING);
		} catch (InvocationTargetException | InterruptedException e) {}
	};
	private final Runnable TRANSFER_TO_FILE=()->{
		if(nRedirect) {
			return;
		}
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
	//Threading
	@Override
	public void run() {//Don't invoke interrupt
		try {
			while(true) {
				currentValue=in.read();
				if(currentValue==-1) {
					break;
				}
				boolean result=currentJudger.test(currentValue);
				if(currentJudger.bP()&&result) {
					block();
				}
				processor.run();
				if(currentJudger.aP()&&result) {
					block();
				}
			}
			EDT.interrupt();
			show.append("\nstream closed");
		}catch(IOException e) {
			EDT.interrupt();
			show.append("\nstream closed");
		}
	}
	//GUI
	@SuppressWarnings("unused")
	public SocketUI(Socket soc) {
		EDT=new EventDispatchThread();
		EDT.start();
		socket=soc;
		TITLE=socket.toString();
		TITLE_BLOCKED="[Stream Blocked]"+TITLE;
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
			JMenu trunc=new JMenu("Modify Blocking Policies");
			JMenuItem trunc_now=new JMenuItem("Block on Every Byte");
			JMenuItem nonBlock=new JMenuItem("Don't Block the Stream");
			JMenuItem length=new JMenuItem("Block after a fixed length");
			JMenuItem del=new JMenuItem("Block after specfic delimiters");
			lastSelected=trunc_now;
			lastSelected.setEnabled(false);
			trunc_now.addActionListener(n->{
				currentJudger=BLOCKED;
				select(trunc_now);
			});
			nonBlock.addActionListener(n->{
				currentJudger=ALLOW;
				select(nonBlock);
			});
			//Dialog for length
			JDialog len=new JDialog(this,"GNU's Not Unix~",true);
			len.setLayout(new BorderLayout());
			JPanel lN=new JPanel();
			lN.setLayout(new FlowLayout());
			lN.add(new JLabel("Length="));
			JTextField lenTF=new JTextField();
			((AbstractDocument)lenTF.getDocument()).setDocumentFilter(new HexInput.NumberFilter());
			lenTF.setColumns(20);
			lN.add(lenTF);
			lN.add(new JLabel("Byte(s)"));
			len.add(lN,BorderLayout.NORTH);
			FlowLayout fl=new FlowLayout();
			fl.setAlignment(FlowLayout.RIGHT);
			JPanel lS=new JPanel();
			lS.setLayout(fl);
			JButton Lok=new JButton("OK");
			JButton Lcancel=new JButton("Cancel");
			Lok.addActionListener(n->{
				long l;
				try {
					l=Long.parseLong(lenTF.getText());
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(this, "Length Limit Exceeded","Illegal Input",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(l==0) {
					JOptionPane.showMessageDialog(this, "Length = 0 byte?","Illegal Input",JOptionPane.ERROR_MESSAGE);
					return;
				}
				FL_INSTANCE.setLength(l);
				FL_INSTANCE.clear();
				confirmed=true;
				len.dispose();
			});
			Lcancel.addActionListener(n->{
				confirmed=false;
				len.dispose();
			});
			lS.add(Lok);
			lS.add(Lcancel);
			len.add(lS,BorderLayout.SOUTH);
			len.pack();
			len.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			//Dialog done
			length.addActionListener(n->{
				preBlock();
				len.setVisible(true);
				if(confirmed) {
					synchronized(SocketUI.this) {
						currentJudger=FLB;
					}
					select(length);
				}else {
					resumeBlocker();
				}
			});
			//Delimiter Dialogs
			JDialog dDialog=new JDialog(this,"Delimiters (Match one of them)",true);
			dDialog.setSize(600,600);
			JPanel dS=new JPanel();
			FlowLayout dFL=new FlowLayout();
			dFL.setAlignment(FlowLayout.RIGHT);
			dS.setLayout(dFL);
			JButton dAdd=new JButton("New Delimiter");
			JPanel dM=new JPanel();
			BoxLayout dBL=new BoxLayout(dM,BoxLayout.Y_AXIS);
			dM.setLayout(dBL);
			JButton dOK=new JButton("OK");
			JButton dCancel=new JButton("Cancel");
			dAdd.addActionListener(n->{
				passed=false;
				HexInput.inputBlocked(PASS_RETURN_VALUE);
				if(passed) {
					JPanel inner=new JPanel();
					FlowLayout infl=new FlowLayout();
					infl.setAlignment(FlowLayout.RIGHT);
					inner.setLayout(infl);
					JTextField itf=new JTextField(Hex.toHex(HEX_TEMP));
					itf.setEditable(false);
					JButton delete=new JButton("Delete");
					delete.addActionListener(n0->{
						dM.remove(inner);
						dDialog.revalidate();
						dDialog.repaint();
					});
					inner.add(new JScrollPane(itf));
					inner.add(delete);
					dM.add(inner);
					SwingUtilities.updateComponentTreeUI(dCancel);
					SwingUtilities.updateComponentTreeUI(dM);
					SwingUtilities.updateComponentTreeUI(dDialog);
				}
			});
			dOK.addActionListener(n->{
				Component components[]=dM.getComponents();
				for(int i=0;i<components.length;++i) {
					JPanel current=(JPanel)components[i];
					JScrollPane pane=(JScrollPane)current.getComponent(0);
					JTextField content=(JTextField)pane.getViewport().getComponent(0);
					delimiter.delimiter(Hex.getBytes(content.getText()));
				}
				delimiter.build();
				synchronized(SocketUI.this) {
					currentJudger=new Blocker(false,true,delimiter::walk);
				}
				confirmed=true;
				dDialog.dispose();
			});
			dCancel.addActionListener(n->{
				confirmed=false;
				dDialog.dispose();
			});
			dS.add(dOK);
			dS.add(dCancel);
			dS.add(dAdd);
			dDialog.add(dS,BorderLayout.SOUTH);
			dDialog.add(dM,BorderLayout.CENTER);
			dDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			//Dialog done
			del.addActionListener(n->{
				delimiter=new Delimiter();
				confirmed=false;
				preBlock();
				dDialog.setVisible(true);
				if(confirmed) {
					select(del);
				}else {
					resumeBlocker();
				}
			});
			JMenuItem distrunc=new JMenuItem("Resume the stream");
			JMenuItem clear=new JMenuItem("Clear Screen");
			JMenuItem sh=new JMenuItem("Hide data you sent");
			sh.addActionListener(n->{
				if(nSend) {
					sh.setText("Hide data you sent");
					nSend=false;
				}else {
					sh.setText("Show data you sent");
					nSend=true;
				}
			});
			JMenuItem vh=new JMenuItem("View Raw Data as Hex");
			vh.addActionListener(n->{
				viewHex.setVisible(true);
			});
			clear.addActionListener(n->{
				show.setText("");
			});
			distrunc.addActionListener(n->{
				synchronized(SocketUI.this) {
					SocketUI.this.notifyAll();
				}
			});
			trunc.add(trunc_now);
			trunc.add(nonBlock);
			trunc.add(length);
			trunc.add(del);
			data.add(vh);
			data.add(clear);
			data.add(trunc);
			data.add(distrunc);
			data.add(sh);
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
				input.setEditable(false);
				if(!nSend) {
					show.append(input.getText());
					viewHex.insertSend(Hex.toHex(input.getText().getBytes()));
				}
				EDT.put(new OutputEvent(out,input.getText().getBytes()));
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
					Stream.of(JFrame.getWindows()).forEach(frame->frame.dispose());
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
	 * Classes
	 * Only used internally classes
	 * Only private classes can be set private
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
	private static class FixedLength implements IntPredicate{
		private long length;
		private long current=0;
		@Override
		public boolean test(int value) {
			++current;
			if(current==length) {
				current=0;
				return true;
			}
			return false;
		}
		public void clear() {
			current=0;
		}
		public void setLength(long l) {
			length=l;
		}
	}
	private static class Blocker{
		private boolean bP,aP;
		private IntPredicate pred;
		Blocker(boolean b,boolean a,IntPredicate i){
			bP=b;
			aP=a;
			pred=i;
		}
		public boolean test(int i) {
			return pred.test(i);
		}
		public boolean aP() {
			return aP;
		}
		public boolean bP() {
			return bP;
		}
	}
	//Classes done, Functions now
	private void select(JMenuItem item) {
		lastSelected.setEnabled(true);
		item.setEnabled(false);
		lastSelected=item;
	}
	private void block() {
		setTitle(TITLE_BLOCKED);
		blocked=true;
		try {
			synchronized(this) {
				wait();
			}
		} catch (InterruptedException e) {}
		blocked=false;
		setTitle(TITLE);
	}
	private void preBlock() {
		lastBlocked=blocked;
		lastJudger=currentJudger;
		currentJudger=BLOCKED;
	}
	public void resumeBlocker() {
		synchronized(SocketUI.this) {
			currentJudger=lastJudger;
			if(!lastBlocked) {
				SocketUI.this.notifyAll();
			}
		}
	}
}
