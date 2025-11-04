package io.github.javaherobrine.net.ui;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.function.*;
import io.github.javaherobrine.net.speed.*;
import io.github.javaherobrine.net.*;
public class SocketUI extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;
	private JTextArea show=new JTextArea();
	private TextArea input=new TextArea();
	private static final JFileChooser CHOOSER=new JFileChooser();
	private Socket socket;
	private LimitedOutputStream out;
	private LimitedInputStream in;
	private int currentValue;
	/*
	 * It works like a function pointer.
	 * It's ugly, but with less temporary objects.
	 * If I put this lambda expression in the parameter of callback, there will be tons of temporary objects.
	 * They are all callbacks
	 */
	private final Consumer<String> SEND_HEX=str->{
		byte[] block=new byte[str.length()>>1];
		for(int i=0;i<block.length;++i) {
			block[i]=(byte)(Character.digit(str.charAt(1|(i<<1)),16)+(Character.digit(str.charAt(i<<1),16)<<4));
		}
		try {
			out.write(block);
		} catch (IOException e) {}
	};
	private final Consumer<Long> LIMIT_UP=speed->{
		out.speed=speed;
	};
	private final Consumer<Long> LIMIT_DOWN=speed->{
		in.speed=speed;
	};
	private final Runnable APPEND_STRING=()->{
		show.append(Character.toString((char)currentValue));
	};
	/*
	 * Callback over, now initialize some fields
	 */
	static {
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
	public void run() {
		try {
			while(true) {
				currentValue=in.read();
				if(currentValue==-1) {
					break;
				}
				SwingUtilities.invokeLater(APPEND_STRING);
			}
			show.append("\nstream closed");
		}catch(IOException e) {
			show.append("\nstream closed");
		}
	}
	@SuppressWarnings("unused")
	public SocketUI(Socket soc) {
		socket=soc;
		try {
			in=new LimitedInputStream(soc.getInputStream());
			out=new LimitedOutputStream(soc.getOutputStream());
		} catch (IOException e) {
			System.err.println("Invalid socket");
			dispose();
			return;
		}
		OutputWorker worker=new OutputWorker(out);
		worker.start();
		SwingUtilities.invokeLater(()->{
			JMenuBar bar=new JMenuBar();
			JMenu file=new JMenu("Network");
			JMenuItem upload=new JMenuItem("Upload");
			upload.addActionListener(m->{
				if(CHOOSER.showDialog(null, "Upload")==0) {
					try {
						File f=CHOOSER.getSelectedFile();
						InputStream in=new BufferedInputStream(new FileInputStream(f));
						worker.transfer(in);
						show.append("\n"+f.length()+"bytes from file were sent\n");
					}catch (Exception e) {}
				}
			});
			JMenuItem close=new JMenuItem("Close");
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
			file.add(upload);
			file.add(close);
			file.add(sendBinary);
			file.add(limit);
			bar.add(file);
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
				worker.write(input.getText().getBytes());
				show.append(input.getText());
				input.setEditable(true);
				input.setText("");
			});
			setLayout(new BorderLayout());
			setTitle(soc.toString());
			add(panel,BorderLayout.CENTER);
			add(send,BorderLayout.SOUTH);
			setSize(600,600);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		});
	}
}
