package eu.javaexperience.web.dummy;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import eu.javaexperience.collection.enumerations.EmptyEnumeration;
import eu.javaexperience.reflect.Mirror;

public class DummyHttpSession implements HttpSession
{
	public static final DummyHttpSession instance = new DummyHttpSession();
	
	@Override
	public Object getAttribute(String arg0)
	{
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return EmptyEnumeration.instance;
	}

	@Override
	public long getCreationTime()
	{
		return 0;
	}

	@Override
	public String getId()
	{
		return "";
	}

	@Override
	public long getLastAccessedTime()
	{
		return 0;
	}

	@Override
	public int getMaxInactiveInterval()
	{
		return 0;
	}

	@Override
	public ServletContext getServletContext()
	{
		return null;
	}

	@Override
	public HttpSessionContext getSessionContext()
	{
		return null;
	}

	@Override
	public Object getValue(String arg0)
	{
		return null;
	}

	@Override
	public String[] getValueNames()
	{
		return Mirror.emptyStringArray;
	}

	@Override
	public void invalidate()
	{
	}

	@Override
	public boolean isNew()
	{
		return true;
	}

	@Override
	public void putValue(String arg0, Object arg1)
	{
	}

	@Override
	public void removeAttribute(String arg0)
	{
	}

	@Override
	public void removeValue(String arg0)
	{
	}

	@Override
	public void setAttribute(String arg0, Object arg1)
	{
	}

	@Override
	public void setMaxInactiveInterval(int arg0)
	{
	}
}
