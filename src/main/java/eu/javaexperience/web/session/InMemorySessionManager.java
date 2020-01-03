package eu.javaexperience.web.session;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import eu.javaexperience.interfaces.simple.SimpleCall;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.Session;
import eu.javaexperience.web.SessionManager;

public class InMemorySessionManager implements SessionManager
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("InMemorySessionManager"));
	
	protected ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
	
	public final SimpleCall sessionDestroyer = this::housekeep;
	
	@Override
	public void destroySession(String str)
	{
		if(LOG.mayLog(LogLevel.DEBUG))
		{
			LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "`%s`.destroySession(`%s`)", this, str);
		}
		Session sess = sessions.remove(str);
		if(sess != null)
			sess.invalidate();
	}

	@Override
	public String registerSession(Session sess)
	{
		if(LOG.mayLog(LogLevel.DEBUG))
		{
			LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "`%s`.registerSession(`%s`)", this, sess);
		}
		return SessionTools.assignSessionId(sess, sessions);
	}

	@Override
	public String getSessionStringBySession(Session sess)
	{
		for(Entry<String, Session> kv:sessions.entrySet())
			if(sess.equals(kv.getValue()))
				return kv.getKey();
			
		return null;
	}

	public Session getSiteObjectSession(String cookieName, HttpServletRequest req)
	{
		Cookie[] co = req.getCookies();
		if(co != null)
		{
			for(Cookie c:co)
			{
				if(cookieName.equals(c.getName()))
				{
					return sessions.get(c.getValue());
				}
			}
		}
		
		return null;
	}
	
	public Session getSessionById(String id)
	{
		return sessions.get(id);
	}

	@Override
	public void destroySession(Context ctx)
	{
		Session sess = ctx.getSession();
		if(null != sess)
		{
			String id = sess.getId();
			if(null != id)
			{
				destroySession(id);
			}
		}
	}

	@Override
	public Set<Entry<String, Session>> listAllSession()
	{
		return sessions.entrySet();
	}

	@Override
	public void addSessionWithId(String sessionCookieValue, Session sess)
	{
		if(LOG.mayLog(LogLevel.DEBUG))
		{
			LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "`%s`.addSessionWithId(`%s`, `%s`)", this, sessionCookieValue, sess);
		}
		sessions.put(sessionCookieValue, sess);
	}

	@Override
	public void housekeep()
	{
		if(LOG.mayLog(LogLevel.MEASURE))
		{
			LoggingTools.tryLogFormat(LOG, LogLevel.DEBUG, "`%s`.housekeep()", this);
		}
		
		for(Entry<String, Session> kv:sessions.entrySet())
		{
			Session sess = kv.getValue();
			if(sess == null)
				continue;
			
			if(sess.validUntil() > -1 && sess.validUntil() >= System.currentTimeMillis())
			{
				sessions.remove(kv.getKey());
				try
				{
					sess.invalidate();
				}
				catch(Exception e)
				{
					LoggingTools.tryLogFormatException(LOG, LogLevel.WARNING, e, "Exception while invalidationg session `%s` ", sess);
				}
			}
		}
	}
}