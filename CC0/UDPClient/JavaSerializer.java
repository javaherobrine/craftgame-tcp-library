package CC0.UDPClient;


import io.github.javaherobrine.net.EventContent;
import io.github.javaherobrine.net.Serializer;

import java.io.*;

public class JavaSerializer implements Serializer {
    @Override
    public byte[] serialize(EventContent ec) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ec);
        oos.flush();
        return bos.toByteArray();
    }

    @Override
    public EventContent deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        return (EventContent) obj;
    }
}
