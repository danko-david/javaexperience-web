package eu.javaexperience.web.dispatch.url;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.compress.CompressTools;

public enum ContentCompression
{
	gzip
	{
		@Override
		protected void writeCompressedContent(OutputStream os, byte[] data) throws IOException
		{
			os.write(CompressTools.compressLZ77(data));
		}
		
	},
//	compress,
	deflate
	{
		@Override
		protected void writeCompressedContent(OutputStream os, byte[] data) throws IOException
		{
			os.write(CompressTools.compressZlib(data, 6));
		}
	},
	identity
	{
		@Override
		protected void writeCompressedContent(OutputStream os, byte[] data) throws IOException
		{
			os.write(data);
		}
	},
	//br
	
	;
	
	
	public static ContentCompression recognise(HttpServletRequest req)
	{
		String comp = req.getHeader("Accept-Encoding");
		if(null != comp)
		{
			try
			{
				String[] ss = comp.split(",");
				for(String s:ss)
				{
					ContentCompression ret = valueOf(s.trim());
					if(null != ret)
					{
						return ret;
					}
				}
			}
			catch(Exception e)
			{}
		}
		
		return identity;
	}
	
	protected abstract void writeCompressedContent(OutputStream os, byte[] data) throws IOException;
	
	public void sendContent(HttpServletResponse resp, byte[] data) throws IOException
	{
		resp.addHeader("Content-Encoding", name());
		OutputStream os = resp.getOutputStream();
		writeCompressedContent(os, data);
		os.flush();
	}
}
