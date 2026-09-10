package io.github.javaherobrine.nio;
import java.nio.*;
public interface StateMachine {
	Object value();
	void clear();
	void advance(ByteBuffer buffer);
}
