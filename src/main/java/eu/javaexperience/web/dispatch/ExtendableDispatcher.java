package eu.javaexperience.web.dispatch;

import java.util.HashMap;

import eu.javaexperience.collection.map.RWLockMap;
import eu.javaexperience.dispatch.Dispatcher;
import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.dispatch.url.PreparedURL;

/**
 * Ha nincs több elem akkor ennek kellene kiszolgálnia
 * 
 * A Controllerek a végpontban lévő Dispatcherek
 * */
public abstract class ExtendableDispatcher<CTX extends Context> implements Dispatcher<CTX>
{
	private RWLockMap<String, Dispatcher<CTX>> dispatchers =
		new RWLockMap<>(new HashMap<String, Dispatcher<CTX>>());	
	
	public Dispatcher<CTX> addDispatcher(@MayNull String path_element, Dispatcher<CTX> disp)
	{
		return dispatchers.put(path_element, disp);
	}

	@Override
	public boolean dispatch(CTX ctx)
	{
		PreparedURL url = ctx.getRequestUrl();
		String element = null;
		if(!url.isEndOfPath())
		{
			element = url.getCurrentURLElement();
		}
		Dispatcher<CTX> dp = dispatchers.get(element);
		
		if(null != dp)
		{
			dp.dispatch(ctx);
			return true;
		}
		
		return false;
	}
}