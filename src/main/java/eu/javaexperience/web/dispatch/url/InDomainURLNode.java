package eu.javaexperience.web.dispatch.url;

import java.net.URL;

import eu.javaexperience.web.Context;

public abstract class InDomainURLNode extends TldUrlNode
{
	protected final PreparedURL matcher;
	protected boolean resetPointer;
	
	protected URLNode[] seoPath;
	
	public InDomainURLNode(URL starts, boolean resetPointer)
	{
		super("");
		matcher = new PreparedURL(starts);
		seoPath = URLNodeTools.createFrom(matcher);
		seoPath[seoPath.length-1].addChild(this);
		this.resetPointer = resetPointer;
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		PreparedURL req = ctx.getRequestUrl();
		
		if
		(
			req.path.length < matcher.path.length
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
	
	public abstract boolean onMatch(Context ctx);
}
