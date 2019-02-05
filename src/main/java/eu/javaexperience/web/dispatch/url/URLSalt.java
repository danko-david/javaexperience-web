package eu.javaexperience.web.dispatch.url;

import eu.javaexperience.asserts.AssertArgument;

public class URLSalt extends URLNode
{
	public URLSalt(URLNode parent, String name)
	{
		AssertArgument.assertNotNull(this.nodeName = name, "name");
	}
	
	public void setName(String name)
	{
		AssertArgument.assertNotNull(this.nodeName = name, "name");
	}

	public String getSalt()
	{
		return getNodeName();
	}
}