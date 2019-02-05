package eu.javaexperience.web.session;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import eu.javaexperience.collection.enumerations.EnumerationFromIterator;
import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.interfaces.simple.publish.SimplePublish2;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.Session;
import eu.javaexperience.web.service.hooks.ServiceProcessHooks;


//TODO session invalidate
public class SessionImpl implements Session
{
	protected String id;
	protected ServiceProcessHooks hooks;
	protected final long creationTime;
	protected long lastAccess;
	protected boolean isNew = true;
	protected int validLADT = 60*60*1000;//1 óra
	
	protected final Map<String, Object> env;
	
	public SessionImpl(ServiceProcessHooks hooks)
	{
		this(hooks, new SmallMap<String,Object>());
	}
	
	public SessionImpl(ServiceProcessHooks hooks, Map<String,Object> params)
	{
		this.hooks = hooks;
		lastAccess = creationTime = System.currentTimeMillis();
		this.env = params;
	}

	@Override
	public long getCreationTime()
	{
		return creationTime;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public long updateUseSession(Context ctx)
	{
		long prev = lastAccess;
		lastAccess = System.currentTimeMillis();
		isNew = false;
		
		ServiceProcessHooks hooks = this.hooks;
		if(null != hooks)
		{
			SimplePublish2<Session, Context> pub = hooks.onSessionRefreshLastUse();
			if(null != pub)
			{
				pub.publish(this, ctx);
			}
		}
		
		return prev;
	}
	
	@Override
	public long validUntil()
	{
		if(validLADT<0)
			return -1L;
		
		return lastAccess+validLADT;
	}
	
	@Override
	public long getLastAccessedTime()
	{
		return lastAccess;
	}

	@Override
	public void setMaxInactiveInterval(int interval)
	{
		validLADT = interval*1000;
	}

	@Override
	public int getMaxInactiveInterval()
	{
		return validLADT;
	}

	@Override
	public Object getAttribute(String name)
	{
		return env.get(name);
	}

	@Override
	public Object getValue(String name)
	{
		return env.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return new EnumerationFromIterator<>(env.keySet());
	}

	@Override
	public String[] getValueNames()
	{
		return env.keySet().toArray(Mirror.emptyStringArray);
	}

	@Override
	public void setAttribute(String name, Object value)
	{
		Object o = env.put(name, value);

		if(value instanceof HttpSessionBindingListener)
			((HttpSessionBindingListener)value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
		
		if(o instanceof HttpSessionBindingListener)
			((HttpSessionBindingListener)o).valueUnbound(new HttpSessionBindingEvent(this, name, o));
	}

	@Override
	public void putValue(String name, Object value)
	{
		env.put(name, value);
	}

	@Override
	public void removeAttribute(String name)
	{
		Object o = env.remove(name);
		if(o instanceof HttpSessionBindingListener)
			((HttpSessionBindingListener)o).valueUnbound(new HttpSessionBindingEvent(this, name, o));
	}

	@Override
	public void removeValue(String name)
	{
		env.remove(name);
	}

	@Override
	public void invalidate(){}

	@Override
	public boolean isNew()
	{
		return isNew;
	}
	
	@Override
	public Map<String, Object> getEnv()
	{
		return env;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceProcessHooks getProcessHooks()
	{
		return hooks;
	}

	@Override
	public void setServiceProcessHooks(ServiceProcessHooks hooks)
	{
		this.hooks = hooks;
	}

	public void setId(String id)
	{
		this.id = id;
	}
}