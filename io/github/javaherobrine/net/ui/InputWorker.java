package io.github.javaherobrine.net.ui;
import javax.swing.*;
import java.io.*;
import java.util.*;
public class InputWorker extends SwingWorker<Void,Integer>{
	private InputStream in;
	private JTextArea area;
	public InputWorker(InputStream i,JTextArea a) {
		in=i;
		area=a;
	}
	@Override
	protected Void doInBackground() throws Exception {
		publish(in.read());
		return null;
	}
	@Override
	protected void process(List<Integer> chunks) {
		super.process(chunks);
		for(int i:chunks) {
			area.append(Character.toString(i));
		}
	}
	public void publish(String input) {
		SwingUtilities.invokeLater(()->{
			area.append(input);
		});
	}
}
