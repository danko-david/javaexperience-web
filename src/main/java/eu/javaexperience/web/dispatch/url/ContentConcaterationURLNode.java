package eu.javaexperience.web.dispatch.url;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.javaexperience.generic.TimeAttrEntry;

public abstract class ContentConcaterationURLNode extends CachedSaltedContentUrlNode
{
	protected abstract TimeAttrEntry<byte[]>[] getContentList();
	
	public ContentConcaterationURLNode(String name, String mime)
	{
		super(name, mime);
	}
	
	protected byte[] beforeContentUnit(TimeAttrEntry<byte[]> entry)
	{
		return null;
	}
	
	protected byte[] afterContentUnit(TimeAttrEntry<byte[]> entry)
	{
		return null;
	}
	
	@Override
	public byte[] loadContent()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TimeAttrEntry<byte[]>[] fs = getContentList();
		
		if(null != fs)
		{
			for(TimeAttrEntry<byte[]> sf:fs)
			{
				try
				{
					byte[] before = beforeContentUnit(sf);
					byte[] after = afterContentUnit(sf);
					if(null != before)
					{
						baos.write(before);
					}
					
					baos.write(sf.getSubject());
					
					if(null != after)
					{
						baos.write(after);
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		
		return baos.toByteArray();
	}
	
	@Override
	public long determineLastModified()
	{
		long lastModified = 0;
		TimeAttrEntry<byte[]>[] fs = getContentList();
		if(null == fs)
		{
			return 0;
		}
		
		for(TimeAttrEntry<byte[]> sf:fs)
		{
			long lm = sf.getLastModifiedTime();
			if(lm >  lastModified)
			{
				lastModified = lm;
			}
		}
		return lastModified;
	}
}
