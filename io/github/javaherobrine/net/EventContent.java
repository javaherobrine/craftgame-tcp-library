package io.github.javaherobrine.net;
import java.io.*;
import static java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import io.github.javaherobrine.*;
public abstract class EventContent extends AbstractEvent implements Serializable,Cloneable{
	private static final long serialVersionUID = 1;
	//If it takes long time,it had better be interruptible
	public abstract void recvExec(boolean serverside) throws Exception;
	public transient AbstractClient recver;
	//The next 2 methods are used to serialize and deserialize, override them if needed
	public void valueOf(Map<String,Object> input) {}
	public SimpleEntry<String,Object>[] values(){
	    return null;
	}
	@Override
	public EventContent clone() {
	    try {
			return (EventContent)super.clone();
	    } catch (CloneNotSupportedException e) {
			throw new Error("panic");
	    }
	}
	@Override
	public void process() throws Exception{
		recvExec(recver instanceof AbstractServerSideClient);
	}
}
