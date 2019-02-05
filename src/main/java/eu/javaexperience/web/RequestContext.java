package eu.javaexperience.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.web.dispatch.url.PreparedURL;

public class RequestContext extends AbstractContext
{
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	
	protected Map<String, Object> env = new SmallMap<>();
	
	public RequestContext(HttpServletRequest req, HttpServletResponse resp)
	{
		/*this.contextParams = this.owner.getDefaultParams();
		this.ctxParamImpl = this.contextParams.getImpl();*/
		this.request = req;
		this.response = resp;
		this.requestURL = new PreparedURL(req);
	}
	
	/****************************************************************************
	 * 																			*
	 * 				DataObject és Scriptable implementációs rész.				*
	 * 																			*
	 ****************************************************************************/
	
	@Override
	public HttpServletRequest getRequest()
	{
		return request;
	}
	
	@Override
	public HttpServletResponse getResponse()
	{
		return response;
	}
	
	@Override
	public Map<String, Object> getEnv()
	{
		return env;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		HttpServletRequest req = getRequest();
		sb.append("<RequestContext>\n");
			sb.append("\tMethod:");
			sb.append(req.getMethod());
			sb.append("\n");
		
			sb.append("\tRequest URL: ");
			sb.append(getRequestUrl().toUrl().toString());
			sb.append("\n");
			
			sb.append("\tRequestParameters: ");
			sb.append(HttpTools.renderRequestParams((Map)getRequest().getParameterMap()));
			sb.append("\n");
			
			sb.append("\tUserAgent: ");
			sb.append(req.getHeader("User-Agent"));
			sb.append("\n");
			
			sb.append("\tIP: ");
			sb.append(req.getRemoteAddr());
			sb.append("\n");
			
		sb.append("</RequestContext>");
		return sb.toString();
	}
}