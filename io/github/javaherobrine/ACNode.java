package io.github.javaherobrine;
import java.util.*;
public class ACNode {
	public ACNode[] next=new ACNode[256];
	private ACNode kmp;//used in AC Automaton
	public Object res=null;
	public void push(byte[] input, Object obj) {
		ACNode current=this;
		for(byte b:input) {
			int index=(b>=0?b:256+b);
			if(current.next[index]==null) {
				current.next[index]=new ACNode();
			}
			current=current.next[index];
		}
		current.res=obj;
	}
	public Object get(byte[] input) {
		ACNode current=this;
		for(byte b:input) {
			int index=(b>=0?b:256+b);
			if(current.next[index]==null) {
				return null;
			}
			current=current.next[index];
		}
		return current.res;
	}
	/*
	 * Build an AC Automaton
	 */
	public void AC() {
		ACNode pre=new ACNode();
		for(int i=0;i<256;++i) {
			pre.next[i]=this;
		}
		this.kmp=pre;
		Queue<ACNode> q=new LinkedList<>();
		q.add(this);
		while(!q.isEmpty()) {
			ACNode t=q.poll();
			for(int i=0;i<256;++i) {
				if(t.next[i]!=null) {
					t.next[i].kmp=t.kmp.next[i];
					q.add(t.next[i]);
				}else {
					t.next[i]=t.kmp.next[i];
				}
			}
		}
		this.kmp=null;//For GC
	}
}
