package eu.javaexperience.web.session;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import eu.javaexperience.interfaces.simple.SimpleCall;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.Session;
import eu.javaexperience.web.SessionManager;
import eu.javaexperience.web.SessionManagerTools;

public class InMemorySessionManager implements SessionManager
{
	protected ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
	
	public final SimpleCall sessionDestroyer = new SimpleCall()
	{
		@Override
		public void call()
		{
			for(Entry<String, Session> kv:sessions.entrySet())
			{
				Session sess = kv.getValue();
				if(sess == null)
					continue;//bár fura is lenne
				
				if(sess.validUntil() > -1 && sess.validUntil() >= System.currentTimeMillis())
				{
					sessions.remove(kv.getKey());
					try
					{
						sess.invalidate();
					}
					catch(Exception e)
					{
						//TODO
					}
				}
			}
		}
	};
	
	@Override
	public void destroySession(String str)
	{
			Session sess = sessions.remove(str);
			if(sess != null)
				sess.invalidate();
	}
	
/*	@Override
	public void destroySession(Context ctx)
	{
		Session sess = ctx.getSession();
		if(sess == null)
			return;
		
		SiteObject so = ctx.getOwnerSiteObject();
		
		if(so.onSessionDestory != null)
			so.onSessionDestory.publish(sess);
		
		destroySession(getSessionStringBySession(sess));
		
		Cookie[] cs = ctx.getRequest().getCookies();
		for(Cookie c:cs)
			if(so.getSessionCookieName().equals(c.getName()))
			{
				c.setMaxAge(0);
				ctx.getResponse().addCookie(c);
				break;
			}
	}
*/	
	//TODO azért nem rendeli össze a sessiont mert itt generálunk negi egy újat...
	//kell egy olyan metódus amivel nem a assignSessionId ad süti értéket hanem mi dobtaunk egyet be.

	@Override
	public String registerSession(Session sess)
	{
		return SessionManagerTools.assignSessionId(sess, sessions);
	}

/*	@Override
	public void applySession(SiteObject so, AbstractUser usr, Context ctx)
	{
		
		SiteObjectSession session = new SiteObjectSession(so, usr);
		
		String sessid = SessionManagerTools.assignSessionId(session, sessions);
		
		if(so.onNewSession != null)
		{
			so.onNewSession.publish(session);
		}
		
		if(ctx != null)
		{
			ctx.setSession(session);
			Cookie c = new Cookie(so.getSessionCookieName(), sessid);
			if(so.getCookiePath() != null)
				c.setPath(so.getCookiePath());
			
			if(so.getCookieDomain() != null)
				c.setDomain(so.getCookieDomain());
			
			if(so.getCookieExpiry() != 0)
				c.setMaxAge(so.getCookieExpiry());
			
			ctx.getResponse().addCookie(c);
		}
	}
*/
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

	//TODO
	@Override
	public void addSessionWithId(String sessionCookieValue, Session sess)
	{
		sessions.put(sessionCookieValue, sess);
	}
}