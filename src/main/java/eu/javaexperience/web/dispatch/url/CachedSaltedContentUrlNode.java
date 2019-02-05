package eu.javaexperience.web.dispatch.url;

import java.util.ArrayList;

import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.facility.SiteFacilityTools;

public abstract class CachedSaltedContentUrlNode extends SimpleCachedContentUrlNode
{
	protected byte[] data = new byte[0];
	protected long lastModified;
	protected String mime;
	
	protected boolean checkModifiedOnRequest;
	
	protected boolean redirectToSalt;
	
	public CachedSaltedContentUrlNode(String name, String mime)
	{
		super(name);
		addChild(salt);
		this.addChild(salt);
		this.mime = mime;
	}
	
	public boolean isCheckModifiedOnAccess()
	{
		return checkModifiedOnRequest;
	}
	
	public void setCheckModifiedOnRequest(boolean check)
	{
		checkModifiedOnRequest = check;
	}
	
	public boolean isRedirectToSalt()
	{
		return redirectToSalt;
	}
	
	public void setRedirectToSalt(boolean redirect)
	{
		redirectToSalt = redirect;
	}
	
	public abstract byte[] loadContent();
	
	public abstract long determineLastModified();
	
	public long getLastModified()
	{
		if(checkModifiedOnRequest)
		{
			return lastModified = determineLastModified();
		}
		
		return lastModified;
	}
	
	/**
	 * checks for background content change.
	 * if content modified:
	 * 	- updating inner lastModified field
	 *  - loadContent() background data (and transforms)
	 *  - refresh salt hexa
	 * */
	public boolean checkModificationRefreshIfNeeded()
	{
		long lastMod = determineLastModified();
		if(lastModified < lastMod)
		{
			refresh();
			return true;
		}
		return false;
	}
	
	public CachedSaltedContentUrlNode refresh()
	{
		callbackInvalidate();
		lastModified = determineLastModified();
		data = transform(loadContent());
		salt.setName(HttpTools.toCacheSaltHexa(lastModified));
		callbackRefreshed();
		return this;
	}
	
	public void callbackInvalidate()
	{
		
	}
	
	protected void callbackRefreshed()
	{}
	
	public final URLSalt salt = new URLSalt(this, HttpTools.toCacheSaltHexa(lastModified = getLastModified()))
	{
		@Override
		public String getSalt()
		{
			if(checkModifiedOnRequest)
			{
				checkModificationRefreshIfNeeded();
			}
			return super.getSalt();
		}
	};
	
	public String getSaltedUrl()
	{
		return salt.getCanonicalURL();
	}
	
	protected byte[] transform(byte[] in)
	{
		return in;
	}

	@Override
	public byte[] getContent()
	{
		if(checkModifiedOnRequest)
		{
			checkModificationRefreshIfNeeded();
		}
		return data;
	}
	


	@Override
	public String getMimeType()
	{
		return mime;
	}
	
	@Override
	public String getCanonicalURL()
	{
		return appendRawURLPath(new ArrayList<URLLink>(), false, true);
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		PreparedURL url = ctx.getRequestUrl();
		tryDispatchSubNodes(ctx);
		if(url.hasNextURLElement() || !salt.getNodeName().equals(url.getCurrentURLElement()))
		{
			SiteFacilityTools.httpRedirect(ctx, url.getUrl(url.getUrlPointer(), false)+"/"+salt.getNodeName(), true);
			return true;
		}
		else
		{
			ctx.getResponse().setContentType(getMimeType());
			return super.dispatch(ctx);
		}
	}
}
