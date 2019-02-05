package eu.javaexperience.web.dispatch.url;

import java.util.regex.Pattern;

public class SimpleURLNode extends URLNode
{
	public SimpleURLNode(String nodeName)
	{
		super(nodeName);
	}

	public SimpleURLNode(String nodeName,boolean caseSensitive)
	{
		super(nodeName,caseSensitive);
	}
	
	public SimpleURLNode(String nodeName,Pattern regex)
	{
		super(nodeName,regex);
	}
	
	@Override
	public String toString()
	{
		return "SimpleUrlNode: "+nodeName;
	}
}