package eu.javaexperience.web;

import java.util.Map;

import javax.servlet.http.HttpSession;

import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.web.service.hooks.ServiceProcessable;

public interface Session extends HttpSession, ServiceProcessable
{
	public void setId(String id);
	
	public long updateUseSession(@MayNull Context ctx);
	public Map<String,Object> getEnv();
	
	/**
	 * returns less than zero if never expires
	 * */
	public long validUntil();
}