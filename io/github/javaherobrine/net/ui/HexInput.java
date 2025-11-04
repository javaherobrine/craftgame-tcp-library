package io.github.javaherobrine.net.ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.function.*;
import java.awt.event.*;
public class HexInput extends JFrame{
	private static final long serialVersionUID = 1L;
	private Consumer<String> callback;
	private static HexInput INSTANCE = new HexInput();
	@SuppressWarnings("unused")
	private HexInput() {
		SwingUtilities.invokeLater(()->{
			setTitle("I");
			JTextField field=new JTextField();
			AbstractDocument doc=(AbstractDocument)field.getDocument();
			doc.setDocumentFilter(new DocumentFilter() {
				@Override
				public void replace(DocumentFilter.FilterBypass fb,int offset,int length,String str,AttributeSet aset) throws BadLocationException{
					StringBuilder sb=new StringBuilder();
					for(int i=0;i<str.length();++i) {
						char ch=str.charAt(i);
						if(Character.isDigit(ch)) {
							sb.append(ch);
						}
						if(ch>='a'&&ch<='f') {
							sb.append(ch);
						}
						if(ch>='A'&&ch<='F') {
							sb.append(ch);
						}
					}
					super.replace(fb, offset, sb.length()-1, sb.toString(), aset);
				}
				@Override
				public void insertString(DocumentFilter.FilterBypass fp, int offset,String string,AttributeSet aset) throws BadLocationException {
					StringBuilder sb=new StringBuilder();
					for(int i=0;i<string.length();++i) {
						char ch=string.charAt(i);
						if(Character.isDigit(ch)) {
							sb.append(ch);
						}
						if(ch>='a'&&ch<='f') {
							sb.append(ch);
						}
						if(ch>='A'&&ch<='F') {
							sb.append(ch);
						}
					}
					super.insertString(fp, offset, sb.toString(), aset);
				}
			});
			setSize(200,100);
			setLayout(new BorderLayout());
			add(field,BorderLayout.NORTH);
			JPanel panel=new JPanel();
			panel.setLayout(new FlowLayout());
			JButton OK=new JButton("OK");
			JButton cancel=new JButton("cancel");
			OK.addActionListener(action->{
				String str=field.getText();
				if((str.length()&1)==1) {
					JOptionPane.showMessageDialog(this, "The length of hex string must be an even", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				setVisible(false);
				field.setText("");
				dispose();
				callback.accept(str);
			});
			cancel.addActionListener(action->{
				field.setText("");
				setVisible(false);
				dispose();
			});
			addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					field.setText("");
					setVisible(false);
					dispose();
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
			panel.add(OK);
			panel.add(cancel);
			add(panel,BorderLayout.SOUTH);
			dispose();
		});
	}
	public static void input(Consumer<String> input) {
		SwingUtilities.invokeLater(()->{
			INSTANCE.callback=input;
			INSTANCE.setVisible(true);
		});
	}
}
