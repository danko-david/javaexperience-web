package eu.javaexperience.web.dispatch;

import java.io.IOException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import eu.javaexperience.collection.map.KeyVal;
import eu.javaexperience.dispatch.Dispatcher;
import eu.javaexperience.dispatch.SubdispatchVariator;
import eu.javaexperience.dispatch.WithMatchDispatcher;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.getBy.GetByTools;
import eu.javaexperience.patterns.behavioral.cor.link.CorChainLink;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.dispatch.url.PreparedURL;
import eu.javaexperience.web.dispatch.url.URLNodePattern;

public class WebDispatchTools
{
	public static <CTX extends Context> CorMatchSubdispatch<CTX> createWith(Dispatcher<CTX>... disp)
	{
		CorMatchSubdispatch<CTX> ret = new CorMatchSubdispatch<>();
		SubdispatchVariator<CTX> var = ret.getDispatchVariator();
		for(Dispatcher<CTX> d:disp)
		{
			var.addDispatcher(d);
		}
		
		return ret;
	}
	
	@SafeVarargs
	public static <T,CTX extends Context> CorMatchSubdispatch<CTX> createWithPreparator(GetBy1<URLNodePattern, T> processor, Entry<T, ? extends Dispatcher<CTX>>... ents)
	{
		CorMatchSubdispatch<CTX> ret = new CorMatchSubdispatch<>();
		SubdispatchVariator<CTX> var = ret.getDispatchVariator();
		for(Entry<T, ? extends Dispatcher<CTX>> d:ents)
		{
			final URLNodePattern patt = processor.getBy(d.getKey());
			if(null == patt)
			{
				continue;
			}
			
			WithMatchDispatcher<CTX> node = new WithMatchDispatcher<CTX>()
			{
				@Override
				public boolean isMatch(CTX ctx)
				{
					if(patt.match(ctx))
					{
						//TODO
						ctx.getRequestUrl().jumpNextURLElement();
						return true;
					}
					return false;
				}
			
			};
			node.getDispatchVariator().addDispatcher(d.getValue());
			var.addDispatcher(node);
		}
		return ret;
	}
	
	public static <CTX extends Context> CorMatchSubdispatch<CTX> createWithMatcher(Entry<URLNodePattern, Dispatcher<CTX>>... ents)
	{
		return createWithPreparator(GetByTools.getPassTrought(), ents);
	}
	
	public static final GetBy1<URLNodePattern, String> PATH_START_MATCHER = new GetBy1<URLNodePattern, String>()
	{
		@Override
		public URLNodePattern getBy(final String pathNode)
		{
			return new URLNodePattern()
			{
				@Override
				public boolean match(Context ctx)
				{
					PreparedURL url = ctx.getRequestUrl();
					String elem = url.getCurrentURLElement();
					return pathNode.equals(elem);
				}
			};
		}
	};
	
	public static final GetBy1<URLNodePattern, String> PATH_IS = new GetBy1<URLNodePattern, String>()
	{
		@Override
		public URLNodePattern getBy(final String path)
		{
			return new URLNodePattern()
			{
				@Override
				public boolean match(Context ctx)
				{
					PreparedURL url = ctx.getRequestUrl();
					return path.equals(url.getPath());
				}
			};
		}
	};
	
	public static <CTX extends Context> CorMatchSubdispatch<CTX> createWithPathNodeStart(Entry<String, ? extends Dispatcher<CTX>>... ents)
	{
		return createWithPreparator(PATH_START_MATCHER, ents);
	}
	
	public static final GetBy1<URLNodePattern, String> DOMAIN_MATCHER_PERPARATOR = new GetBy1<URLNodePattern, String>()
	{
		@Override
		public URLNodePattern getBy(final String domain)
		{
			return new URLNodePattern()
			{
				@Override
				public boolean match(Context ctx)
				{
					return ctx.getRequestUrl().toUrl().getHost().equals(domain);
				}
			};
		}
	};
	 
	public static <CTX extends Context> CorMatchSubdispatch<CTX> createWithDomain(Entry<String, ? extends Dispatcher<CTX>>... ents)
	{
		return createWithPreparator(DOMAIN_MATCHER_PERPARATOR, ents);
	}
	
	protected static final GetBy1<URLNodePattern, String> PATH_REGEX_MATCHER = new GetBy1<URLNodePattern, String>()
	{
		@Override
		public URLNodePattern getBy(String a)
		{
			final Pattern patt = Pattern.compile(a);
			return new URLNodePattern()
			{
				@Override
				public boolean match(Context ctx)
				{
					URL url = ctx.getRequestUrl().toUrl();
					return patt.matcher(url.getPath()).find();
				}
			};
		}
	};

	public static CorChainLink<Context> createWithPathRegex(KeyVal<String, Dispatcher<Context>> keyVal)
	{
		return createWithPreparator(PATH_REGEX_MATCHER, keyVal);
	}

	public static CorChainLink<Context> exactPath(final URLNodePattern pattern, final Dispatcher<Context> dispatcher)
	{
		return new CorChainLink<Context>()
		{
			@Override
			public boolean dispatch(Context ctx)
			{
				if(pattern.match(ctx))
				{
					dispatcher.dispatch(ctx);
				}
				return false;
			}
		};
		
	}

	public static Dispatcher<Context> wrap(final HttpServlet servlet)
	{
		return new Dispatcher<Context>()
		{
			@Override
			public boolean dispatch(Context ctx)
			{
				try
				{
					servlet.service(ctx.getRequest(), ctx.getResponse());
				}
				catch(Exception e)
				{
					Mirror.propagateAnyway(e);
				}
				return true;
			}
		};
	}

}
