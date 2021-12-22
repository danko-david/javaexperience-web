package eu.javaexperience.web.features;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.collection.map.BulkTransitMap;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.patterns.behavioral.mediator.EventMediator;
import eu.javaexperience.text.Format;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.service.hooks.ServiceProcessHooks;

public class WebSocketEndpoint implements WebSocket
{
	protected final InputStream is;
	protected final OutputStream os;
	
	public WebSocketEndpoint(InputStream is, OutputStream os)
	{
		this.is = is;
		this.os = os;
	}
	
	protected String key;
	
	public String keyKey()
	{
		return key;
	}
	
	protected ArrayList<SimplePublish1<WebSocketEndpoint>> onDisconnect = new ArrayList<>();
	
	public void addOnDisconnectListener(SimplePublish1<WebSocketEndpoint> dc)
	{
		onDisconnect.add(dc);
	}
	
	public boolean hasOnDisconnectListener(SimplePublish1<WebSocketEndpoint> dc)
	{
		return onDisconnect.contains(dc);
	}
	
	public boolean removeOnDisconnectListener(SimplePublish1<WebSocketEndpoint> dc)
	{
		return onDisconnect.remove(dc);
	}
	
	protected void raiseOnDisconnectListeners()
	{
		for(SimplePublish1<WebSocketEndpoint> eps: onDisconnect)
		{
			try
			{
				eps.publish(this);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	//http://stackoverflow.com/questions/17693956/websocket-sending-messages-over-65535-bytes-fails
	public void send(byte[] data) throws IOException
	{
		synchronized (os)
		{
			long length = data.length;
			int rawDataIndex = -1;
			if (length <= 125)
			{
				rawDataIndex = 2;
			}
			else if (length >= 126 && length <= 65535)
			{
				rawDataIndex = 4;
			}
			else
			{
				rawDataIndex = 10;
			}
			byte[] frame = new byte[10];
			frame[0] = (byte)129;
			if (rawDataIndex == 2)
			{
				frame[1] = (byte)length;
			}
			else if (rawDataIndex == 4)
			{
				frame[1] = (byte)126;
				frame[2] = (byte)(( length >> 8 ) & (byte)255);
				frame[3] = (byte)(( length      ) & (byte)255);
			}
			else
			{
				frame[1] = (byte)127;
				frame[2] = (byte)(( length >> 56 ) & (byte)255);
				frame[3] = (byte)(( length >> 48 ) & (byte)255);
				frame[4] = (byte)(( length >> 40 ) & (byte)255);
				frame[5] = (byte)(( length >> 32 ) & (byte)255);
				frame[6] = (byte)(( length >> 24 ) & (byte)255);
				frame[7] = (byte)(( length >> 16 ) & (byte)255);
				frame[8] = (byte)(( length >>  8 ) & (byte)255);
				frame[9] = (byte)(( length       ) & (byte)255);
	
			}
			os.write(frame, 0, rawDataIndex);
			os.write(data);
			os.flush();
		}
	}
	
	protected byte[] packedBuffer = new byte[1024];
	
	@Override
	//http://stackoverflow.com/questions/18368130/how-to-parse-and-validate-a-websocket-frame-in-java/18371023#18371023
	public byte[] receive() throws IOException
	{
		synchronized (is)
		{
			int pEp = 0;
			boolean fin = false;
			while(!fin)
			{
				byte b = (byte) is.read();
				fin = ((b & 0x80) != 0);
				boolean rsv1 = ((b & 0x40) != 0);
				boolean rsv2 = ((b & 0x20) != 0);
				boolean rsv3 = ((b & 0x10) != 0);
				int opcode = (byte)(b & 0x0F);
				
				// TODO: add control frame fin validation here
				// TODO: add frame RSV validation here
	
				// Masked + Payload Length
				b = (byte) is.read();
				boolean masked = ((b & 0x80) != 0);
				int payloadLength = (byte)(0x7F & b);
				int byteCount = 0;
				if (payloadLength == 0x7F)
				{
					// 8 byte extended payload length
					byteCount = 8;
					payloadLength = 0;
				}
				else if (payloadLength == 0x7E)
				{
					// 2 bytes extended payload length
					byteCount = 2;
					payloadLength = 0;
				}
				
				// Decode Payload Length
				//for(int i=0;i<byteCount;++i)
				for(int i=byteCount;i > 0;--i)
				//while (--byteCount > 0)
				{
					int val = is.read();
					if(val < 0)
					{
						throw new RuntimeException("Endpoint closed");
					}
					payloadLength |= (val & 0xFF) << (8 * (i-1));
				}
	
				// TODO: add control frame payload length validation here
				//System.out.println("PAYLOAD_LENGTH: "+byteCount+" => "+payloadLength);
				
				byte maskingKey[] = null;
				if (masked)
				{
					// Masking Key
					maskingKey = new byte[4];
					for(int i=0;i < 4;)
					{
						int count = is.read(maskingKey, i, maskingKey.length-i);
						if(count < 0)
						{
							throw new RuntimeException("Endpoint closed");
						}
						i += count;
					}
				}
	
				// TODO: add masked + maskingkey validation here
	
				// Payload itself
				if(pEp+payloadLength > packedBuffer.length)
				{
					packedBuffer = Arrays.copyOf(packedBuffer, pEp+payloadLength);
				}
	
				int start = pEp;
				
				for(int i=0;i < payloadLength;)
				{
					int len = is.read(packedBuffer, pEp, payloadLength-i);
					if(len < 0)
					{
						throw new RuntimeException("Endpoint closed");
					}
					i += len;
					pEp += len;
				}
				
				// Demask (if needed)
				if (masked)
				{
					if(null == maskingKey)
					{
						throw new NullPointerException("maskingKey is null");
					}
					
					for (int i = 0; i < payloadLength; ++i)
					{
						packedBuffer[start+i] ^= maskingKey[i % 4];
					}
				}
			}
			
			return Arrays.copyOf(packedBuffer, pEp);
		}
	}
	
	public void finishConnection()
	{
		IOTools.silentClose(is);
		IOTools.silentClose(os);
		raiseOnDisconnectListeners();
	}

	@Override
	public void close() throws IOException
	{
		IOTools.silentClose(is);
		IOTools.silentClose(os);
	}
	
	public static final String RFC_6455_MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	public void fillHeaders(Map<String, String> dst) throws UnsupportedEncodingException
	{
		String enc = key+RFC_6455_MAGIC_STRING;
		
		MessageDigest cript = null;
		
		try
		{
			cript = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		
		cript.reset();
		cript.update(enc.getBytes("utf8"));
		
		dst.put("Connection", "Upgrade");
		dst.put("Upgrade", "websocket");
		dst.put("Sec-WebSocket-Accept", Format.base64Encode(cript.digest()));
/*		String response = "HTTP/1.1 101 Switching Protocols\r\n"+*/
		
	}
	
	public static WebSocketEndpoint upgradeHttpRequest
	(
		HttpServletRequest req,
		HttpServletResponse resp
	)
		throws IOException
	{
		String key = req.getHeader("Sec-WebSocket-Key");
		
		if(null == key)
		{
			throw new RuntimeException("Websocket upgrade failed, no Sec-WebSocket-Key specified in request header");
		}
		
		String enc = key+RFC_6455_MAGIC_STRING;
		
		MessageDigest cript = null;
		
		try
		{
			cript = MessageDigest.getInstance("SHA-1");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		
		cript.reset();
		cript.update(enc.getBytes("utf8"));
		
		resp.setStatus(101);
		resp.addHeader("Connection", "Upgrade");
		resp.addHeader("Upgrade", "websocket");
		resp.addHeader("Sec-WebSocket-Accept", Format.base64Encode(cript.digest()));
		
		OutputStream os = resp.getOutputStream();
		os.flush();
		
		WebSocketEndpoint ret = new WebSocketEndpoint(req.getInputStream(), os);
		ret.key = key;
		
		return ret;
	}

/*	TODO reimplement
	public static WebSocketEndpoint upgradeConnection(LightningHttpQueryContext hqc) throws IOException
	{
		OutputStream os = hqc.getLightningOutput().getBackendOutput();
		
		HttpRequest request = hqc.getRequest();
		
		WebSocketEndpoint ret = new WebSocketEndpoint(request.getInputStream(), os);

		String key = request.getHeader("Sec-WebSocket-Key");
		
		if(null == key)
		{
			key = request.getHeader("Sec-Websocket-Key");
		}
		
		
		ret.key = key;
		if(null == ret.key)
		{
			throw new RuntimeException("Websocket upgrade failed, no Sec-WebSocket-Key specified in request header");
		}
		
		Map<String, String> headers = new BulkTransitMap<>();
		
		ret.fillHeaders(headers);
		hqc.getResponse().setStatus(101);
		hqc.sendHeaders(headers);
		
		return ret;
	}

	
*/
	
	public static WebSocket upgradeRequest(Context ctx) throws IOException
	{
		ServiceProcessHooks hooks = ctx.getProcessHooks();
		if(null != hooks)
		{
			GetBy1<WebSocket, Context> uw = hooks.upgradeWebsocket();
			if(null != uw)
			{
				return uw.getBy(ctx);
			}
		}
		
		return upgradeHttpRequest(ctx.getRequest(), ctx.getResponse());
	}
}