package io.github.javaherobrine.nio;
import java.net.*;
import java.nio.channels.*;
import java.io.*;
import io.github.javaherobrine.net.*;
import java.util.function.*;
/**
 * You should implement the consume() to process the event.
 * Using this technique instead of closure aims to reduce memory consumption.
 */
public abstract class NIOServer implements AbstractServer, Runnable, Consumer<SelectionKey>{
	ServerSocketChannel channel;
	Selector epoll_wait;
	public NIOServer(SocketAddress addr) throws IOException{
		epoll_wait=Selector.open();
		channel=ServerSocketChannel.open();
		channel.bind(addr);
		channel.configureBlocking(false);
		channel.register(epoll_wait, SelectionKey.OP_ACCEPT, channel);
	}
	@Override
	public void close() throws IOException{
		epoll_wait.close();
	}
	@Override
	public void run() {
		while(true) {
			try {
				epoll_wait.select();
				epoll_wait.selectedKeys();
			} catch (IOException e) {
				break;
			}
		}
	}
}
