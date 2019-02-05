package eu.javaexperience.web.dispatch.url;

import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.generic.TimeAttrEntry;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.facility.SiteFacilityTools;

public abstract class SimpleCachedContentUrlNode extends URLNode implements TimeAttrEntry<byte[]>
{
	protected boolean validForever; 
	
	protected String allowed_other_sites = null;
	
	public void allowOtherSite(String allow)
	{
		this.allowed_other_sites = allow;
	}
	
	public SimpleCachedContentUrlNode(String nodeName)
	{
		super(nodeName);
	}

	public SimpleCachedContentUrlNode(String nodeName,boolean caseSensitive, boolean validForever)
	{
		super(nodeName,caseSensitive);
		this.validForever = validForever;
	}
	
	public SimpleCachedContentUrlNode(String nodeName,Pattern regex, boolean validForever)
	{
		super(nodeName,regex);
		this.validForever = validForever;
	}
	
	public void setValidForever(boolean forever)
	{
		this.validForever = forever;
	}
	
	public boolean isValidForever()
	{
		return validForever;
	}
	
	public abstract long getLastModified();
	
	public abstract byte[] getContent();
	
	public abstract String getMimeType();
	
	@Override
	public boolean dispatch(Context ctx)
	{
		HttpServletResponse resp = ctx.getResponse();
		
		if(null != allowed_other_sites)
		{
			ctx.getResponse().addHeader("Access-Control-Allow-Origin" , allowed_other_sites);
		}
		
		long mod = HttpTools.getWebDate(getLastModified());
		
		if(validForever)//Sose változik a tartalma, érdemes az URL-t sózni minden változáskor
		{
			SiteFacilityTools.setItemValidForever(ctx);
		}
		
		
		if(!SiteFacilityTools.isContentModified(ctx, mod))
		{
			SiteFacilityTools.sendItemNotModified(ctx);
		}
		
		SiteFacilityTools.setItemLastModified(ctx, mod);
		
		resp.setContentType(getMimeType());
		byte[] cont = getContent();
		resp.setContentLength(cont.length);
		try
		{
			ctx.getResponse().addHeader("Vary", "Accept-Encoding");
			ContentCompression cc = ContentCompression.recognise(ctx.getRequest());
			cc.sendContent(resp, cont);
			ServletOutputStream os = resp.getOutputStream();
			os.flush();
			os.close();
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
		
		ctx.finishOperation();
		return true;
	}
	
	public long getLastModifiedTime()
	{
		return getLastModified();
	}
	
	public byte[] getSubject()
	{
		return getContent();
	}
	public Object getOrigin()
	{
		return this;
	}
}