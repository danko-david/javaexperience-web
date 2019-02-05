package eu.javaexperience.web.dispatch.url;

import java.net.URL;

import eu.javaexperience.web.Context;

public abstract class StartsWithUrlNode extends TldUrlNode
{
	protected final PreparedURL matcher;
	protected boolean resetPointer;
	
	protected URLNode[] seoPath;
	
	protected boolean allow_partial_match = false;
	
	public StartsWithUrlNode(URL starts, boolean resetPointer)
	{
		super("");
		matcher = new PreparedURL(starts);
		seoPath = URLNodeTools.createFrom(matcher);
		seoPath[seoPath.length-1].addChild(this);
		this.resetPointer = resetPointer;
	}
	
	public void modifyCanonocalPath(PreparedURL matcher)
	{
		seoPath[seoPath.length-1].removeChild(this);
		seoPath = URLNodeTools.createFrom(matcher);
		seoPath[seoPath.length-1].addChild(this);
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		PreparedURL req = ctx.getRequestUrl();
		
		if
		(
				req.path.length < matcher.path.length
			||
				(!allow_partial_match && req.domainSize != matcher.domainSize)
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
			req.pathPointer = req.domainSize;
		}
		
		return onMatch(ctx);
	}
	
	public abstract boolean onMatch(Context ctx);

	public boolean getAllowPartialMatch() {
		return allow_partial_match;
	}

	public void setAllowPartialMatch(boolean allow_partial_match) {
		this.allow_partial_match = allow_partial_match;
	}
}
