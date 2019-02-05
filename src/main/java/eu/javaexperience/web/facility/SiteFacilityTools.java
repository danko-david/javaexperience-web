package eu.javaexperience.web.facility;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import eu.javaexperience.exceptions.OperationSuccessfullyEnded;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.Format;
import eu.javaexperience.time.TimeCalc;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpResponseStatusCode;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.RequestContext;
import eu.javaexperience.web.dispatch.url.URLLink;
import eu.javaexperience.web.service.hooks.ServiceProcessHooks;
import eu.javaexperience.web.template.Template;
import eu.javaexperience.web.template.TemplateContainer;

public class SiteFacilityTools
{
	private SiteFacilityTools(){}
	
	private final static ThreadLocal<Context> requests = new ThreadLocal<>();
	
	public static void setCurrentContext(Context ctx)
	{
		requests.set(ctx);
	}
	
	public static Context getCurrentContext()
	{
		//hát ez mehet vagy ThreadLocal alapján
		//vagy... ha áttérünk select/poll-ra akkor a thread-be mindig bele kell majd rakni az akutális Contextet.
		//bár ha beolvastuk a headereket csak az után jön létre így mind1
		//ja igen mert a select/poll csak arra kell hogyha a vandálok nyitvatartják az üres kapcsolatokat akkor se fussunk ki a connectionPoolból
		//De az láthatatlan lesz a containter szerver által (belső implementáció).
		
		return requests.get();
	}
	
	
	public static void finishOperation()
	{
		throw OperationSuccessfullyEnded.instance;
	}
	
	public static void finishWithRender(Context ctx)
	{
		applyView(ctx);
		ctx.finishOperation();
	}
	
	public static void finishWithLastViewSet(Context ctx, String var, Object elem)
	{
		ctx.getEnv().put(var, elem);
		applyView(ctx);
		ctx.finishOperation();
	}
	
	public static void finishWithLastViewSet(Context ctx, TemplateContainer container,Object elem)
	{
		ctx.getEnv().put(container.name(), elem);
		applyView(ctx);
		ctx.finishOperation();
	}
	
	/**
	 * It's wont call finishOperation(), you might not want to use this method.
	 * {@link #finishWithRender(Context)} instead?
	 * */
	@Deprecated
	public static boolean applyView(Context ctx)
	{
		ServiceProcessHooks hooks = ctx.getProcessHooks();
		if(null != hooks)
		{
			GetBy1<Boolean, Context> hook = hooks.applyView();
			if(null != hook)
			{
				if(Boolean.TRUE == hook.getBy(ctx))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static String getCookieValue(Context ctx, String cookieName)
	{
		return HttpTools.getCookieValue(ctx.getRequest().getCookies(), cookieName);
	}
	
	public static void tryCallBeforeHeaderSent(Context ctx)
	{
		ServiceProcessHooks hooks = ctx.getProcessHooks();
		if(null != hooks)
		{
			SimplePublish1<Context> hook = hooks.beforeHeaderSent();
			if(null != hook)
			{
				hook.publish(ctx);
			}
		}
	}
	
	public static void sendItemNotModified(Context ctx)
	{
		tryCallBeforeHeaderSent(ctx);
		
		ctx.getResponse().setStatus(HttpResponseStatusCode._304_not_modified.getStatus());
		ctx.finishOperation();
	}
	
	public static void setItemLastModified(Context ctx, long lastModified)
	{
		tryCallBeforeHeaderSent(ctx);
		
		//ctx.getResponse().addHeader("Cache-Control", "public, max-age=1");//31536000
		ctx.getResponse().addDateHeader("Last-Modified", lastModified);
	}
	
	public static void setItemValidForever(Context ctx)
	{
		tryCallBeforeHeaderSent(ctx);
		
		ctx.getResponse().setHeader("Pragma", "cache");
		ctx.getResponse().setHeader("Date", HttpTools.toHeaderDate(new Date()));
		ctx.getResponse().setHeader("Cache-Control", "max-age=31556000, stale-while-revalidate=31556000, min-fresh=31556000");
		ctx.getResponse().setHeader("Expires", "Mon, 12 Jan 2037 19:14:07 GMT");
	}
	
	public static void setItemValidSecounds(Context ctx, int secounds)
	{
		tryCallBeforeHeaderSent(ctx);
		
		ctx.getResponse().setHeader("Pragma", "cache");
		ctx.getResponse().setHeader("Date", HttpTools.toHeaderDate(new Date()));
		ctx.getResponse().setHeader("Cache-Control", "max-age="+secounds+", stale-while-revalidate="+secounds+", min-fresh="+secounds);
		ctx.getResponse().setHeader("Expires", HttpTools.toHeaderDate(TimeCalc.addToDate(new Date(), 0, 0, 0, 0, 0, secounds, 0)));
	}
	
	public static boolean isContentModified(Context ctx, long itemLastModified)
	{
		String ifmod = ctx.getRequest().getHeader("If-Modified-Since");
		
		if(ifmod == null)
			return true;
		
		try
		{
			Date da = HttpTools.fromHeaderDate(ifmod);
			return da.getTime() < itemLastModified;
		}
		catch(Exception e)
		{
			return true;
		}
	}
	
	public static void httpRedirect(Context ctx, String path, boolean permanently)
	{
		ctx.getResponse().addHeader("Location", path);
		if(permanently)
		{
			ctx.getResponse().setStatus(HttpResponseStatusCode._301_moved_permanently.getStatus());
		}
		else
		{
			ctx.getResponse().setStatus(HttpResponseStatusCode._307_temporary_redirect.getStatus());
		}
		ctx.finishOperation();
	}
	
	
	public static void finishWithMimeSend(Context ctx, MIME mime, String data)
	{
		finishWithMimeSend(ctx, data, mime.mime);
	}
	
	public static void finishWithMimeSend(Context ctx, MIME mime, byte[] data)
	{
		finishWithMimeSend(ctx, data, mime.mime);
	}
	
	public static void finishWithMimeSend
	(
		Context ctx,
		byte[] blob,
		String mime
	)
	{
		ctx.getResponse().setContentType(mime);
		tryCallBeforeHeaderSent(ctx);
		
		try
		{
			ctx.getResponse().getOutputStream().write(blob);
			ctx.getResponse().getOutputStream().flush();
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
		ctx.finishOperation();
	}
	
	public static void finishWithMimeSend(Context ctx, String data, MIME mime)
	{
		finishWithMimeSend(ctx, data, mime.mime);
	}
	
	public static void finishWithMimeSend(Context ctx, String data, String mime)
	{
		tryCallBeforeHeaderSent(ctx);
		
		try
		{
			ctx.getResponse().setContentType(mime);
			Writer w = ctx.getResponse().getWriter();
			w.write(data);
			w.flush();
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
		ctx.finishOperation();
	}
	
	

	public static void finishWithElementSend(Context ctx,String ele)
	{
		tryCallBeforeHeaderSent(ctx);

		try
		{
			ctx.getResponse().getWriter().append(ele);
			ctx.getResponse().getWriter().flush();
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
		
		ctx.finishOperation();
	}
	
	public static void finishOnNoMoreDispatchNode(Context ctx)
	{
		if(ctx.getRequestUrl().getRemainElementNum() == 1)
			ctx.finishOperation();
	}
	
	

	public static void preventNonCanonicalAccess(RequestContext ctx, URLLink link)
	{
		String canon = link.getCanonicalURL();
		String req = ctx.getRequest().getRequestURL().toString();
		
		if(!req.equals(canon))
		{
			httpRedirect(ctx, canon, true);
		}
	}

	public static void finishWithTemplateRender(Context ctx, Template t) throws IOException
	{
		PrintWriter pw = ctx.getResponse().getWriter();
		t.render(ctx, pw);
		ctx.finishOperation();
	}
	
	public static String renderWithIndirectParams(Template template)
	{
		Appendable app = new StringBuilder();
		try
		{
			template.render(getCurrentContext(), app);
		}
		catch(Exception e)
		{
			try
			{
				app.append(Format.getPrintedStackTrace(e));
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		return app.toString();
	}

	public static void finishWithFileSend(RequestContext ctx, File file, String mime)
	{
		tryCallBeforeHeaderSent(ctx);
		
		try
		{
			ctx.getResponse().setContentType(mime);
			OutputStream os = ctx.getResponse().getOutputStream();
			IOTools.copyFileContentToStream(file, os);
			os.flush();
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
		ctx.finishOperation();
	}
	
	public static boolean hasMoreNodeToProcess(Context ctx)
	{
		return ctx.getRequestUrl().hasNextURLElement();
	}
	
	
}
