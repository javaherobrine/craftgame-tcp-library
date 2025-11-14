package io.github.javaherobrine.net.ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.function.*;
import java.awt.event.*;
import java.nio.charset.*;
public class HexInput extends JFrame{
	private static final long serialVersionUID = 1L;
	private Consumer<byte[]> callback;
	static HexInput INSTANCE = new HexInput();
	@SuppressWarnings("unused")
	private HexInput() {
		SwingUtilities.invokeLater(()->{
			setTitle("Hex input");
			JTextField field=new JTextField();
			AbstractDocument doc=(AbstractDocument)field.getDocument();
			doc.setDocumentFilter(new NumberFilter(16));
			//dialogs
			JDialog dialog=new JDialog(this,"Convert String into Binary",true);
			dialog.setSize(500, 500);
			dialog.setLayout(new BorderLayout());
			JTextArea area=new JTextArea();
			dialog.add(area,BorderLayout.NORTH);
			JPanel jp=new JPanel();
			jp.setLayout(new FlowLayout());
			jp.add(new JLabel("Charset="));
			JComboBox<Charset> list=new JComboBox<>(Charset.availableCharsets().values().toArray(new Charset[0]));
			jp.add(new JScrollPane(list));
			jp.setSize(500, 50);
			JButton ok=new JButton("OK");
			JButton cANCEL=new JButton("Cancel");
			ok.addActionListener(n->{
				Charset set=(Charset) list.getSelectedItem();
				field.setEditable(false);
				String temp=Hex.toHex(area.getText().getBytes(set!=null?set:Charset.defaultCharset()));
				try {
					field.getDocument().insertString(field.getText().length(), temp, null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				field.setEditable(true);
				dialog.dispose();
			});
			cANCEL.addActionListener(n->{
				dialog.dispose();
			});
			jp.add(ok);
			jp.add(cANCEL);
			dialog.add(jp,BorderLayout.SOUTH);
			dialog.setTitle("String Input");
			//dialog done
			setLayout(new BorderLayout());
			add(new JLabel("Input Binary Data via Hex"),BorderLayout.NORTH);
			add(field,BorderLayout.CENTER);
			JPanel panel=new JPanel();
			panel.setLayout(new FlowLayout());
			JButton OK=new JButton("OK");
			JButton cancel=new JButton("Cancel");
			OK.addActionListener(action->{
				String str=field.getText();
				if((str.length()&1)==1) {
					JOptionPane.showMessageDialog(this, "The length of hex string must be an even", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				setVisible(false);
				dispose();
				callback.accept(Hex.getBytes(str));
			});
			cancel.addActionListener(action->{
				field.setText("");
				setVisible(false);
				dispose();
			});
			JButton string=new JButton("From String");
			string.addActionListener(n->{
				dialog.setVisible(true);
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
				public void windowClosed(WindowEvent e) {
					dialog.dispose();
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
			panel.add(string);
			panel.add(OK);
			panel.add(cancel);
			add(panel,BorderLayout.SOUTH);
			pack();
		});
	}
	public static void input(Consumer<byte[]> input) {
		SwingUtilities.invokeLater(()->{
			INSTANCE.pack();
			INSTANCE.callback=input;
			INSTANCE.setVisible(true);
		});
	}
	//Document Filter Class
	public static class NumberFilter extends DocumentFilter{
		int radix=10;
		public NumberFilter(int r){
			radix=r;
		}
		public NumberFilter() {}
		private boolean isDigit(char ch) {
			return Character.digit(ch,radix)!=-1;
		}
		@Override
		public void replace(DocumentFilter.FilterBypass fb,int offset,int length,String str,AttributeSet aset) throws BadLocationException{
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<str.length();++i) {
				char ch=str.charAt(i);
				if(isDigit(ch)) {
					sb.append(ch);
				}
			}
			super.replace(fb, offset, sb.length()-1, sb.toString(), aset);
		}
		@Override
		public void insertString(DocumentFilter.FilterBypass fb,int offset,String str,AttributeSet aset) throws BadLocationException {
			StringBuilder builder=new StringBuilder();
			for(int i=0;i<str.length();++i) {
				char ch=str.charAt(i);
				if(isDigit(ch)) {
					builder.append(ch);
				}
			}
			super.insertString(fb, offset, builder.toString(), aset);
		}
	}
}
