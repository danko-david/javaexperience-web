package eu.javaexperience.web.template;

import java.util.Collection;
import java.util.Map;

import eu.javaexperience.asserts.AssertArgument;
import eu.javaexperience.file.AbstractFile;

public class AbstractTemplateManager<T extends Template> implements FileBasedTemplateManager
{
	protected final Map<String, T> views;
	protected AbstractFile root;
	protected String extension;
	public AbstractTemplateManager(AbstractFile root, String extension, Map<String, T> views)
	{
		this.root = root;
		this.extension = extension;
		AssertArgument.assertNotNull(this.views = views, "views");
	}
	
	@Override
	public T getView(String label)
	{
		return views.get(label);
	}

	@Override
	public Collection<String> getViews()
	{
		return views.keySet();
	}

	@Override
	public AbstractFile getRootDir()
	{
		return root;
	}

	@Override
	public String getExtension()
	{
		return extension;
	}
}
