package eu.javaexperience.web;

public enum HTTPRequestMethod
{
	GET,
	HEAD,
	POST,
	PUT,
	DELETE,
	TRACE,
	OPTIONS,
	CONNECT,
	PATH;
	
	public static HTTPRequestMethod resolvMethod(String str)
	{
		if(str.equalsIgnoreCase("GET"))
			return HTTPRequestMethod.GET;
		else if(str.equalsIgnoreCase("POST"))
			return HTTPRequestMethod.POST;
		else if(str.equalsIgnoreCase("HEAD"))
			return HTTPRequestMethod.HEAD;
		else if(str.equalsIgnoreCase("PUT"))
			return HTTPRequestMethod.PUT;
		else if(str.equalsIgnoreCase("DELETE"))
			return HTTPRequestMethod.DELETE;
		else if(str.equalsIgnoreCase("TRACE"))
			return HTTPRequestMethod.TRACE;
		else if(str.equalsIgnoreCase("OPTIONS"))
			return HTTPRequestMethod.OPTIONS;
		else if(str.equalsIgnoreCase("CONNECT"))
			return HTTPRequestMethod.CONNECT;
		else if(str.equalsIgnoreCase("PATH"))
			return HTTPRequestMethod.PATH;
		else
			return null;
	}
	
}