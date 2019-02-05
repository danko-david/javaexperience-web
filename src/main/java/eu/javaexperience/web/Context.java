package eu.javaexperience.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.web.dispatch.url.PreparedURL;
import eu.javaexperience.web.service.hooks.ServiceProcessable;

public interface Context extends ServiceProcessable
{
	public HttpServletRequest getRequest();
	public HttpServletResponse getResponse();
	public PreparedURL getRequestUrl();
	public void rewriteRequestUrl(PreparedURL purl);
	public void setSession(Session session);
	public Session getSession();
	public void finishOperation();
	public Map<String,Object> getEnv();
	
	public void setProcessException(Throwable t);
	public Throwable getProcessException();
	
}