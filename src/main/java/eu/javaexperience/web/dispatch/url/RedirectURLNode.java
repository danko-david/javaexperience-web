package eu.javaexperience.web.dispatch.url;

import java.util.regex.Pattern;

import eu.javaexperience.web.Context;
import eu.javaexperience.web.facility.SiteFacilityTools;

public class RedirectURLNode extends URLNode
{
	URLNode target;
	boolean temporary;
	
	@Override
	public boolean dispatch(Context ctx)
	{
		SiteFacilityTools.httpRedirect(ctx, target.getPath(), !temporary);
		return true;//have a nice day!
	};
	
	public RedirectURLNode(URLNode target,boolean temporary,String nodeName)
	{
		super(nodeName);
		this.target = target;
		this.temporary = temporary;
	}

	public RedirectURLNode(URLNode target,boolean temporary,String nodeName,boolean caseSensitive)
	{
		super(nodeName,caseSensitive);
		this.target = target;
		this.temporary = temporary;
	}

	public RedirectURLNode(URLNode target,boolean temporary,String nodeName,Pattern regex)
	{
		super(nodeName,regex);
		this.target = target;
		this.temporary = temporary;
	}
}
