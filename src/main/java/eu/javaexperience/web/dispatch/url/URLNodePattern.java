package eu.javaexperience.web.dispatch.url;

import eu.javaexperience.web.Context;

public interface URLNodePattern
{
	public boolean match(Context ctx);
}