package eu.javaexperience.web.dom.build;

import java.util.List;

public interface WebSoftwareBundle
{
	public List<String> getCss();
	public List<String> getPreJs();
	public List<String> getPostJs();
}
