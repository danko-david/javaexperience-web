package eu.javaexperience.web.template;

import java.io.IOException;

import eu.javaexperience.web.Context;

public abstract class TemplateSimpleWrapper<T> implements Template
{
	protected final T elem;
	public TemplateSimpleWrapper(T elem)
	{
		this.elem = elem;
	}
	
	public void render(Context ctx, Appendable app) throws IOException
	{
		app.append(elem.toString());
	}
}
