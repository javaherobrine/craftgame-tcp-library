package io.github.javaherobrine.net.ui;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.function.*;
public class SocketUI extends JFrame implements Runnable{
	private JTextArea show=new JTextArea();
	private TextArea input=new TextArea();
	private static final JFileChooser CHOOSER=new JFileChooser();
	private Socket socket;
	/*
	 * It works like a function pointer.
	 * It's ugly, but with less temporary objects.
	 * If I put this lambda expression in the parameter of callback, there will be tons of temporary objects.
	 */
	private final Consumer<String> SEND_HEX=str->{
		byte[] block=new byte[str.length()>>1];
		for(int i=0;i<block.length;++i) {
			block[i]=(byte)(Character.digit(str.charAt(1|(i<<1)),16)+(Character.digit(str.charAt(i<<1),16)<<4));
		}
		try {
			socket.getOutputStream().write(block);
		} catch (IOException e) {}
	};
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
			Reader in=new InputStreamReader(socket.getInputStream(),"UTF-8");
			while(true) {
				int i=in.read();
				if(i==-1) {
					break;
				}
				show.append(Character.toString((char)i));
			}
			show.append("\nstream closed");
		}catch(IOException e) {
			show.append("\nstream closed");
		}
	}
	public SocketUI(Socket soc) {
		socket=soc;
		SwingUtilities.invokeLater(()->{
			JMenuBar bar=new JMenuBar();
			JMenu file=new JMenu("Network");
			JMenuItem upload=new JMenuItem("Upload");
			upload.addActionListener(m->{
				if(CHOOSER.showDialog(null, "Upload")==0) {
					try {
						File f=CHOOSER.getSelectedFile();
						InputStream in=new BufferedInputStream(new FileInputStream(f));
						in.transferTo(socket.getOutputStream());
						in.close();
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
			file.add(upload);
			file.add(close);
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
				try {
					show.append(input.getText());
					socket.getOutputStream().write(input.getText().getBytes());
					input.setText("");
				} catch (IOException e) {}
			});
			JMenuItem sendBinary=new JMenuItem("Send Binary Data");
			sendBinary.addActionListener(n->{
				HexInput.input(SEND_HEX);
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
