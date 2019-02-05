package eu.javaexperience.web.dispatch;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.dispatch.Dispatcher;
import eu.javaexperience.exceptions.OperationSuccessfullyEnded;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.RequestContext;
import eu.javaexperience.web.facility.SiteFacilityException;
import eu.javaexperience.web.facility.SiteFacilityTools;
import eu.javaexperience.web.service.hooks.ServiceProcessHooks;

public class HttpDispatcher extends HttpServlet implements Dispatcher<Context>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ServiceProcessHooks hooks;
	protected Dispatcher<Context> root_dispatcher;
	protected Dispatcher<Context> unserved;
	
	public HttpDispatcher(Dispatcher<? extends Context> dispatcher, ServiceProcessHooks hooks)
	{
		this.root_dispatcher = (Dispatcher<Context>) dispatcher;
		this.hooks = hooks;
	}
	
	public ServiceProcessHooks getHooks()
	{
		return hooks;
	}
	
	public void setHooks(ServiceProcessHooks hooks)
	{
		this.hooks = hooks;
	}
	
	
	public Dispatcher<Context> getUnservedDispatch()
	{
		return unserved;
	}

	public void setUnservedDispatch(Dispatcher<Context> unserved)
	{
		this.unserved = unserved;
	}

	
	public Dispatcher<Context> getRootDispatcher()
	{
		return root_dispatcher;
	}

	public void setRoot_dispatcher(Dispatcher<Context> root_dispatcher)
	{
		this.root_dispatcher = root_dispatcher;
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		return root_dispatcher.dispatch(ctx);
	}
	
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		RequestContext ctx = new RequestContext(req, resp);
		ctx.setServiceProcessHooks(hooks);
		
		ServiceProcessHooks hooks = ctx.getProcessHooks();
		if(null != hooks)
		{
			SimplePublish1<Context> bs = hooks.beforeRequestDispatchStart();
			if(null != bs)
			{
				bs.publish(ctx);
			}
		}
		
		SiteFacilityTools.setCurrentContext(ctx);
		
		try
		{
			if(dispatch(ctx))
			{
				resp.flushBuffer();
				return;
			}
			//SikeresKikuldesKivetelt dob, ha kiszolgálta, ha nem akkor unservedet híjvuk.
			
			if(null != unserved)
			{
				unserved.dispatch(ctx);
			}
		}
		catch(OperationSuccessfullyEnded skk)
		{//a válasz sikeresen ki lett küldve!
			return;
		}
		catch(IOException ioe)
		{//A kapcsolat menet közben bezárult... nothing to do
			ioe.printStackTrace();
			return;
		}
		catch(StackOverflowError soe)
		{// valószinüleg egy URLNode vagy HElement vagy más magára hivatkozott.
			soe.printStackTrace();
		}
		catch(Throwable soe)
		{//portál hiba, naplózni és hivatkozni.
			//vagy nem volt elegendő jog.
				//vagy ilyesmi
					//Ezeket a kivételeket StackTrace Alapján el lehet tárolni és számolni hogy hányszor fordult elő.
			
			try
			{
				soe.printStackTrace();
				ctx.setProcessException(soe);
			//	soe.printStackTrace();
			}	
			catch(OperationSuccessfullyEnded skk)
			{
				//
				return;
			}
			catch(SiteFacilityException | StackOverflowError soex)
			{// valószinüleg egy URLNode vagy HElement vagy más magára hivatkozott.
				
			}
			catch(Exception e)
			{}
		}
		finally
		{
			if(null != hooks)
			{
				SimplePublish1<Context> end = hooks.afterRequestEnd();
				if(null != end)
				{
					end.publish(ctx);
				}
			}
			
			SiteFacilityTools.setCurrentContext(null);
		}
	}
}
