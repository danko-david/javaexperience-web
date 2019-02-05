package eu.javaexperience.web.features;

import java.io.Closeable;
import java.io.IOException;

public interface WebSocket extends AutoCloseable, Closeable
{
	public void send(byte[] data) throws IOException;
	public byte[] receive() throws IOException;
}
