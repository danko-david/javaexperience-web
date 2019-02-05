package eu.javaexperience.web.dispatch.url;

import java.util.List;

public class TldUrlNode extends URLNode
{
	protected String protocol = "http";
	protected int port;

	public TldUrlNode(String name)
	{
		super(name);
		isDomainNode = true;
	}
	
	/**
	 * Specified if this is a root node, where protocol required for the correct URL generation
	 * */
	public String getProtocol()
	{
		return protocol;
	}

	public int getPort()
	{
		return port;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
	
	/**
	 * should be set on the root nodes to can generate URL with the correct protocol (http, https) 
	 * */
	public void setProtocol(String proto)
	{
		this.protocol = proto;
	}
	
	protected String processCanonicalUrl(List<URLLink> nodes)
	{
		if(protocol == null)
			throw new RuntimeException("No protocol set in TLD Node: "+getNodeName());
		
		URLLink[] lnk = nodes.toArray(URLNode.emptyURLLinkArray);
		
		
		String domain = getDomain(lnk);
		String path = getPath(lnk);
		
		if(port > 0)
			return protocol+"://"+domain+":"+port+path;
		else
			return protocol+"://"+domain+path;
	}
	
	@Override
	public String getCanonicalURL()
	{
		if(protocol == null)
			throw new RuntimeException("No protocol set in TLD Node: "+getNodeName());
		return protocol+"://"+getNodeName();
	}
}
