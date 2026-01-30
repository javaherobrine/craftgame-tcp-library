package io.github.javaherobrine;
import java.io.*;
public class Delimiter {
	private ACNode tree=new ACNode();
	private ACNode current=null;
	private ByteArrayOutputStream temp=new ByteArrayOutputStream();
	public Delimiter delimiter(byte[] input) {
		tree.push(input, input);
		return this;
	}
	public Delimiter delimiter(String input) {
		return delimiter(input.getBytes());
	}
	public boolean walk(int input) {
		current=current.next[input];
		if(current.res==null) {
			return false;
		}else {
			current=tree;
			return true;
		}
	}
	public void build() {
		tree.AC();
		current=tree;
	}
	public boolean add(int input) {
		current=current.next[input];
		temp.write(input);
		if(current.res==null) {
			return false;
		}else {
			current=tree;
			return true;
		}
	}
	public byte[] poll() {
		byte[] b=temp.toByteArray();
		temp.reset();
		return b;
	}
}
