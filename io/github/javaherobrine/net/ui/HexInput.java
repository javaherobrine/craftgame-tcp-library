package io.github.javaherobrine.net.ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.function.*;
import java.awt.event.*;
public class HexInput extends JFrame{
	private Consumer<String> callback;
	private static HexInput INSTANCE = new HexInput();
	@SuppressWarnings("unused")
	private HexInput() {
		SwingUtilities.invokeLater(()->{
			JFormattedTextField field=new JFormattedTextField();
			AbstractDocument doc=(AbstractDocument)field.getDocument();
			doc.setDocumentFilter(new DocumentFilter() {
				@Override
				public void replace(DocumentFilter.FilterBypass fb,int offset,int length,String str,AttributeSet aset) throws BadLocationException{
					StringBuilder sb=new StringBuilder();
					for(int i=0;i<str.length();++i) {
						char ch=str.charAt(i);
						System.err.println(ch);
						System.err.println((int)ch);
						if(Character.isDigit(ch)) {
							System.err.println("digit");
							sb.append(ch);
						}
						if(ch>='a'&&ch<='f') {
							System.err.println("Hex");
							sb.append(ch);
						}
						if(ch>='A'&&ch<='F') {
							System.err.println("Hex");
							sb.append(ch);
						}
					}
					System.err.println(sb.length()+" "+length);
					super.replace(fb, offset, sb.length()-1, sb.toString(), aset);
				}
				@Override
				public void insertString(DocumentFilter.FilterBypass fp, int offset,String string,AttributeSet aset) throws BadLocationException {
					System.err.println("+");
					System.err.println(string);
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
					
				}
				callback.accept(str);
				field.setText("");
				setVisible(false);
			});
			cancel.addActionListener(action->{
				field.setText("");
				setVisible(false);
			});
			addWindowListener(new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowClosing(WindowEvent e) {
					field.setText("");
					setVisible(false);
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
		});
	}
	public static void input(Consumer<String> input) {
		INSTANCE.callback=input;
		INSTANCE.setVisible(true);
	}
}
