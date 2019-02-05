package eu.javaexperience.web;

import eu.javaexperience.asserts.AssertArgument;

public class HttpResponseStatusException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final HttpResponseStatusCode status;
	
	public HttpResponseStatusException(HttpResponseStatusCode resp)
	{
		AssertArgument.assertNotNull(this.status = resp, "status");
	}
	
	public HttpResponseStatusException(HttpResponseStatusCode resp, Throwable t)
	{
		super(t);
		AssertArgument.assertNotNull(this.status = resp, "status");
	}
	
	
	public HttpResponseStatusException(HttpResponseStatusCode resp, String msg, Throwable t)
	{
		super(msg, t);
		AssertArgument.assertNotNull(this.status = resp, "status");
	}

	public HttpResponseStatusCode getStatusCode()
	{
		return status;
	}
}
