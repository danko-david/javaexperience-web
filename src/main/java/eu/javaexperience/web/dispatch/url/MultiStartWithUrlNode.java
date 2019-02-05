package eu.javaexperience.web.dispatch.url;

import java.net.URL;
import java.util.ArrayList;

import eu.javaexperience.web.Context;

public abstract class MultiStartWithUrlNode extends URLNode
{
	protected boolean resetPointer;
	
	protected ArrayList<PreparedURL> matchers = new ArrayList<>();
	
	public MultiStartWithUrlNode(boolean resetPointer, URL... starts)
	{
		this.resetPointer = resetPointer;
		for(URL u:starts)
		{
			matchers.add(new PreparedURL(u));	
		}
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		PreparedURL req = ctx.getRequestUrl();
		
		for(PreparedURL matcher:matchers)
		{
			if
			(
					req.path.length < matcher.path.length
				||
					req.domainSize != matcher.domainSize
			)
			{
				return false;
			}
			
			String[] a = matcher.path;
			String[] b = req.path;
			for(int i=0;i<a.length;++i)
			{
				if(!a[i].equals(b[i]))
				{
					return false;
				}
			}
			
			if(resetPointer)
			{
				req.pathPointer = a.length;
			}
			
			return onMatch(ctx);
		}
		
		return false;
	}
	
	public abstract boolean onMatch(Context ctx);
}
