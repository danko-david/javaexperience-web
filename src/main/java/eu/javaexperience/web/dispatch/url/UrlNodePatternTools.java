package eu.javaexperience.web.dispatch.url;

import java.net.URL;

import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.url.UrlPart;
import eu.javaexperience.web.Context;

public class UrlNodePatternTools
{
	/**
	 * For part getter use: {@link UrlPart}
	 * */
	public static URLNodePattern assemble(final GetBy1<String, URL> part, final GetBy1<Boolean, String> matcher)
	{
		return new URLNodePattern()
		{
			@Override
			public boolean match(Context ctx)
			{
				try
				{
					PreparedURL prep = ctx.getRequestUrl();
					String subject = part.getBy(new URL(prep.getUrl()));
					return matcher.getBy(subject);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return false;
			}
		};
	}
}
