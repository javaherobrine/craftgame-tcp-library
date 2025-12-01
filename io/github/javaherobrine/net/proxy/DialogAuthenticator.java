package io.github.javaherobrine.net.proxy;
import javax.swing.*;
import java.awt.*;
import java.net.*;
public class DialogAuthenticator extends Authenticator{
	private JDialog dialog=new JDialog((JFrame)null,"Authentication Required",true);
	private JLabel remote=new JLabel();
	private JLabel infomation=new JLabel();
	private JLabel protocol=new JLabel();
	private JTextField username=new JTextField();
	private JPasswordField password=new JPasswordField();
	@SuppressWarnings("unused")
	public DialogAuthenticator() {
			BoxLayout layout=new BoxLayout(dialog.getContentPane(),BoxLayout.Y_AXIS);
			dialog.setLayout(layout);
			JPanel protocolPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			protocolPanel.add(new JLabel("Protocol: "));
			protocolPanel.add(protocol);
			dialog.add(protocolPanel);
			JPanel remotePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			remotePanel.add(new JLabel("Remote Server: "));
			remotePanel.add(remote);
			dialog.add(remotePanel);
			JPanel infomationPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			infomationPanel.add(new JLabel("Extra Infomation:"));
			infomationPanel.add(infomation);
			dialog.add(infomationPanel);
			JPanel usernamePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			usernamePanel.add(new JLabel("Username: "));
			username.setColumns(10);
			usernamePanel.add(username);
			dialog.add(usernamePanel);
			JPanel passwordPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			password.setColumns(10);
			password.setEchoChar('*');
			passwordPanel.add(new JLabel("Password: "));
			passwordPanel.add(password);
			dialog.add(passwordPanel);
			JCheckBox checkbox=new JCheckBox("Show Password");
			checkbox.addActionListener(n->{
				password.setEchoChar(checkbox.isSelected()?0:'*');
			});
			dialog.add(checkbox);
			dialog.setResizable(false);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		protocol.setText(getRequestingProtocol());
		infomation.setText(getRequestingPrompt());
		remote.setText(getRequestingHost()+":"+getRequestingPort());
		dialog.pack();
		dialog.setVisible(true);
		return new PasswordAuthentication(username.getText(),password.getPassword());
	}
}
