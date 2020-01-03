package eu.javaexperience.web.session;

import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.Cookie;

import eu.javaexperience.text.StringTools;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.Session;
import eu.javaexperience.web.SessionManager;
import eu.javaexperience.web.facility.SiteFacilityTools;

public class SessionTools
{
	public static Session tryAssignSession(SessionManager manager, String sessionCookieName, Context ctx)
	{
		String req_id = SiteFacilityTools.getCookieValue(ctx, sessionCookieName);
		if(null == req_id)
		{
			return null;
		}
		
		Session session = manager.getSessionById(req_id);
		if(null != session)
		{
			ctx.setSession(session);
		}
		
		return session;
	}
	
	public static Session sessionStart(SessionManager manager, String sessionCookieName, Context ctx)
	{
		String[] req_ids = HttpTools.getCookieValues(ctx, sessionCookieName);
		Session session = null;
		if(null != req_ids && req_ids.length > 0)
		{
			for(String req_id:req_ids)
			{
				session = manager.getSessionById(req_id);
				if(null != session)
				{
					break;
				}
			}
		}
		
		if(null == session)
		{
			if(null != req_ids && req_ids.length > 0)
			{
				if(null != req_ids[0] && req_ids.length > 10)
				{
					session = getOrCreateBySession(manager, req_ids[0]);
				}
			}
			
			if(null == session)
			{
				session = createSession(manager);
			}	

			Cookie c = new Cookie(sessionCookieName, session.getId());
			c.setPath("/");
			//c.setDomain("127.0.0.1");
			c.setMaxAge(10*60);
			ctx.getResponse().addCookie(c);
		}
		
		ctx.setSession(session);
		
		session.updateUseSession(ctx);
		
		return session;
	}
	
	public static final String assignSessionId(Session session,ConcurrentMap<String, Session> map)
	{
		out:while(true)
		{
			String sess = StringTools.randomString(25);
			{
				Session ret = map.putIfAbsent(sess, session);
				if(ret != null)
					continue out;
			}
			session.setId(sess);
			return sess;
		}
	}
	
	public static Session createSession(SessionManager manager)
	{
		Session session = new SessionImpl(null);
		String id = manager.registerSession(session);
		session.setId(id);
		return session;
	}

	public static Session getOrCreateBySession
	(
		SessionManager sessionManager,
		String sessId
	)
	{
		Session sess = sessionManager.getSessionById(sessId);
		if(null == sess)
		{
			sess = new SessionImpl(null);
			sess.setId(sessId);
			sessionManager.addSessionWithId(sessId, sess);
		}
		
		return sess;
	}
}
