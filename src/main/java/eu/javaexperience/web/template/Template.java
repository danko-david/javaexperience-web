package eu.javaexperience.web.template;

import java.io.IOException;

import eu.javaexperience.semantic.designedfor.ThreadSafe;
import eu.javaexperience.web.Context;


public interface Template
{
	@ThreadSafe
	public void render(Context ctx, Appendable app) throws IOException;
}