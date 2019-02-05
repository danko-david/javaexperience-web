package eu.javaexperience.web.template;

import java.util.Collection;

/**
 * Template kezelő, névvel hivatkozhatunk a template nevére
 * ő azt szolgáltatja
 * 
 * A template-re névvel hivatkozunk (String)
 * */
public interface TemplateManager
{
	public Template getView(String label);
	public Collection<String> getViews();
}