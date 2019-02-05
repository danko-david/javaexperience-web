package eu.javaexperience.web.dispatch;

import eu.javaexperience.patterns.behavioral.cor.link.CorWrappedDispatch;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.dispatch.CorMatchSubdispatch.PreparedUrlDispatchPointerReset;
import eu.javaexperience.web.dispatch.url.PreparedURL;

/**
 * A CorLinkDispatcher that resets the pointer of PreparedURL of the handled Context.
 * The dispatching procedure modifies the PreparedURL's pointer inside of the Context,
 * but this link will reset if it's can't be dispatched, so other link/chains may try
 * to dispatch.
 * */
public class CorMatchSubdispatch<CTX extends Context> extends CorWrappedDispatch<CTX, PreparedUrlDispatchPointerReset>
{
	public static class PreparedUrlDispatchPointerReset
	{
		public PreparedUrlDispatchPointerReset(PreparedURL url, int origin)
		{
			this.purl = url;
			this.origin = origin;
		}
		
		PreparedURL purl;
		int origin;
		
		public void reset()
		{
			purl.setPathPointer(origin);
		}
	}
	
	@Override
	protected PreparedUrlDispatchPointerReset doBefore(Context ctx)
	{
		PreparedURL url = ctx.getRequestUrl();
		int old = url.getPathPointer();
		url.setPathPointer(url.getDomainElements());
		return new PreparedUrlDispatchPointerReset(url, old);
	}

	@Override
	protected void doAfter(Context ctx, PreparedUrlDispatchPointerReset extraData)
	{
		extraData.reset();
	}
}
