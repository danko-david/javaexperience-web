package eu.javaexperience.web.template;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.collection.map.KeyVal;
import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.exceptions.OperationSuccessfullyEnded;
import eu.javaexperience.interfaces.ExternalDataAttached;
import eu.javaexperience.interfaces.simple.getBy.GetBy2;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.LogLevel;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.log.LoggingTools;
import eu.javaexperience.patterns.behavioral.cor.link.CorChainLink;
import eu.javaexperience.patterns.behavioral.mediator.EventMediator;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.RequestContext;
import eu.javaexperience.web.SessionManager;
import eu.javaexperience.web.WebTools;
import eu.javaexperience.web.dispatch.DefaultDispatchStructure;
import eu.javaexperience.web.facility.SiteFacilityTools;
import eu.javaexperience.web.session.InMemorySessionManager;
import eu.javaexperience.web.session.SessionTools;

public class WebsiteTemplate extends HttpServlet implements ExternalDataAttached
{
	protected static final Logger LOG = JavaExperienceLoggingFacility.getLogger(new Loggable("WebsiteTemplate"));
	
	protected final DefaultDispatchStructure dds = new DefaultDispatchStructure();
	
	protected final GetBy2<Context, HttpServletRequest, HttpServletResponse> createContext;
	
	protected final EventMediator<Entry<WebsiteTemplate, Context>> initContext;
	
	protected final SessionManager sessionManager;
	protected final String sessionCookieName;
	
	protected final SimplePublish1<Context> handle404;
	protected final SimplePublish1<Context> handleApp;
	
	public static class WebsiteTemplateTemplateBuilder
	{
		public SessionManager sessionManager = null;
		public String sessionCookieName = "jvx-session";
		
		public GetBy2<Context, HttpServletRequest, HttpServletResponse> createContext = (req, resp)-> new RequestContext(req, resp);
		
		public EventMediator<Entry<WebsiteTemplate, Context>> initContext = new EventMediator<>();
		
		public SimplePublish1<Context> handleApp;
		
		public SimplePublish1<Context> handle404;
		
		public WebsiteTemplateTemplateBuilder defaultSessionManagement()
		{
			sessionManager = new InMemorySessionManager();
			return this;
		}
		
		public WebsiteTemplateTemplateBuilder default404Handler()
		{
			handle404 = ctx->
			{
				ctx.getResponse().setStatus(404);
				SiteFacilityTools.finishWithMimeSend(ctx, MIME.plain, "404");
			};
			return this;
		}
		
		public WebsiteTemplateTemplateBuilder addPostLoader()
		{
			initContext.addEventListener(ent->WebTools.acceptPostRequests(ent.getValue(), null));
			return this;
		}
		
		public WebsiteTemplateTemplateBuilder addSessionStarter()
		{
			initContext.addEventListener
			(
				ent->
				{
					WebsiteTemplate wt = ent.getKey();
					if(null != wt.getSessionManager() && null != wt.getSessionCookieName())
					{
						SessionTools.sessionStart(wt.getSessionManager(), wt.getSessionCookieName(), ent.getValue());
					}
				}
			);
			return this;
		}
		
		public WebsiteTemplateTemplateBuilder withDefaults()
		{
			addPostLoader();
			addSessionStarter();
			
			default404Handler();
			defaultSessionManagement();
			return this;
		}
		
		public static WebsiteTemplateTemplateBuilder createDefaults()
		{
			WebsiteTemplateTemplateBuilder ret = new WebsiteTemplateTemplateBuilder();
			ret.withDefaults();
			return ret;
		}
	}
	
	public WebsiteTemplate(WebsiteTemplateTemplateBuilder builder)
	{
		this.createContext = builder.createContext;
		this.handle404 = builder.handle404;
		this.handleApp = builder.handleApp;
		this.sessionManager = builder.sessionManager;
		this.sessionCookieName = builder.sessionCookieName;
		this.initContext = new EventMediator<>();
		
		for(SimplePublish1<Entry<WebsiteTemplate, Context>> l:builder.initContext.getListeners())
		{
			initContext.addEventListener(l);
		}
		
		dds.getChains().getChainByName("pre").addLink(new CorChainLink<Context>()
		{
			@Override
			public boolean dispatch(Context ctx)
			{
				if(initContext.hasListener())
				{
					KeyVal<WebsiteTemplate, Context> kv = new KeyVal<>(WebsiteTemplate.this, ctx);
					initContext.dispatchEvent(kv);
				}
				
				return false;
			}
		});
		
		dds.getChains().getChainByName("app").addLink(new CorChainLink<Context>()
		{
			@Override
			public boolean dispatch(Context ctx)
			{
				if(null != handleApp)
				{
					handleApp.publish(ctx);
				}
				return false;
			}
		});
		
		dds.getChains().getChainByName("last").addLink(new CorChainLink<Context>()
		{
			@Override
			public boolean dispatch(Context ctx)
			{
				if(null != handle404)
				{
					handle404.publish(ctx);
				}
				return false;
			}
		});
	}
	
	public EventMediator<Entry<WebsiteTemplate, Context>> getContextInitializers()
	{
		return initContext;
	}
	
	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	public String getSessionCookieName()
	{
		return sessionCookieName;
	}
	
	public DefaultDispatchStructure getDispatchStructure()
	{
		return dds;
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Context ctx = createContext.getBy(req, resp);
		try
		{
			SiteFacilityTools.setCurrentContext(ctx);
			dds.getChains().dispatch(ctx);
		}
		catch(OperationSuccessfullyEnded s)
		{
			return;
		}
		catch(Throwable e)
		{
			LoggingTools.tryLogFormatException(LOG, LogLevel.WARNING, e, "Exception while handling http request ");
			Mirror.propagateAnyway(e);
		}
		finally
		{
			resp.flushBuffer();
			SiteFacilityTools.setCurrentContext(null);
		}
	}

	protected SmallMap<String,Object> extra;
	
	@Override
	public synchronized Map<String, Object> getExtraDataMap()
	{
		if(null == extra)
		{
			 extra = new SmallMap<>();
		}
		return extra;
	}
}
