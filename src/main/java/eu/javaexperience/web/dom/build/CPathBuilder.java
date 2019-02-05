package eu.javaexperience.web.dom.build;

import eu.javaexperience.semantic.references.MayNull;

public interface CPathBuilder<T>
{
	public T createTag(String name);

	public void addChild(T elem, T t);

	public void setId(T ref, String group);

	public void setText(T ref, String group);

	public void addClass(T ref, String group);

	public void addAttribute(T ref, String group, @MayNull String group2);
	
	
	
}
