package eu.javaexperience.web.facility;

import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpResponseStatusCode;
import eu.javaexperience.web.RequestContext;

public class SiteFacilityException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SiteFacilityException(Exception e, HttpResponseStatusCode res, Object ctx)
	{
		super(e);
	}
	
	public SiteFacilityException(String msg)
	{
		super(msg);
		//_500
	}
	
	public SiteFacilityException
	(
		Exception exception,
		HttpResponseStatusCode ok,
		Context currentContext,
		String string
	)
	{
		super(string, exception);
	}

	public SiteFacilityException(HttpResponseStatusCode resp, RequestContext ctx, String msg)
	{
		super(msg);
	}
}