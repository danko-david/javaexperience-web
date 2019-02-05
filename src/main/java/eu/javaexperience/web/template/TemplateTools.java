package eu.javaexperience.web.template;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.semantic.designedfor.ThreadSafe;
import eu.javaexperience.web.AbstractContext;
import eu.javaexperience.web.Context;

public class TemplateTools
{
	public static GetBy1<Context, Context> wrapMapAsEnv(final Map<String, Object> env)
	{
		return new GetBy1<Context, Context>()
		{
			@Override
			public Context getBy(Context a)
			{
				return new AbstractContext()
				{
					@Override public HttpServletResponse getResponse(){return null;}
					
					@Override public HttpServletRequest getRequest() {return null;}
					
					@Override
					public Map<String, Object> getEnv()
					{
						return env;
					}
				};
			}
		};
	}
	
	public static Template createContextWrappedTemplate(final Template t, final GetBy1<Context, Context> wrapper)
	{
		return new Template()
		{
			@Override
			@ThreadSafe
			public void render(Context ctx, Appendable app) throws IOException
			{
				if(null != wrapper)
				{
					ctx = wrapper.getBy(ctx);
				}
				t.render(ctx, app);
			}
		};
	}
}
