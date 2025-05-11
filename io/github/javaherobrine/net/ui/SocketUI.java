package io.github.javaherobrine.net.ui;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
public class SocketUI extends JFrame implements Runnable{
	private JTextArea show=new JTextArea();
	private TextArea input=new TextArea();
	private Socket socket;
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
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout());
		show.setEditable(false);
		JButton send=new JButton("·¢ËÍ");
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
		setLayout(new BorderLayout());
		add(panel,BorderLayout.CENTER);
		add(send,BorderLayout.SOUTH);
		setSize(600,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}
