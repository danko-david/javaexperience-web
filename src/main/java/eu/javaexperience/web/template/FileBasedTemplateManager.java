package eu.javaexperience.web.template;

import eu.javaexperience.file.AbstractFile;

public interface FileBasedTemplateManager extends TemplateManager
{
	public AbstractFile getRootDir();
	public String getExtension();
}
