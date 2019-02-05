package eu.javaexperience.web;

import eu.javaexperience.exceptions.OperationSuccessfullyEnded;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.web.dispatch.url.PreparedURL;
import eu.javaexperience.web.service.hooks.ServiceProcessHooks;

public abstract class AbstractContext implements Context
{
	protected PreparedURL requestURL;
	protected Session session;
	protected Throwable exception;
	protected ServiceProcessHooks hooks;
	
	@Override
	public Session getSession()
	{
		return session;
	}
	
	@Override
	public void setSession(Session sess)
	{
		session = sess;
	}
	
	@Override
	public void finishOperation()
	{
		ServiceProcessHooks hooks = getProcessHooks();
		if(null != hooks)
		{
			SimplePublish1<Context> finish = hooks.rightBeforeFinishOperation();
			if(null != finish)
			{
				finish.publish(this);
			}
		}
		
		throw OperationSuccessfullyEnded.instance;
	}
	

	@Override
	public void setProcessException(Throwable t)
	{
		exception = t;
	}

	@Override
	public Throwable getProcessException()
	{
		return exception;
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

	@Override
	public void rewriteRequestUrl(PreparedURL purl)
	{
		requestURL = purl;
	}
	
	@Override
	public PreparedURL getRequestUrl()
	{
		return requestURL;
	}
}
