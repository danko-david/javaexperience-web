package eu.javaexperience.web;

import java.util.Map.Entry;
import java.util.Set;

//TODO rewrite
public interface SessionManager
{
	public void destroySession(String str);
	
	public void destroySession(Context ctx);

	public String registerSession(Session sess);

	public String getSessionStringBySession(Session sess);
	
	public Session getSessionById(String id);

	public Set<Entry<String, Session>> listAllSession();

	public void addSessionWithId(String sessionCookieValue, Session sess);
}