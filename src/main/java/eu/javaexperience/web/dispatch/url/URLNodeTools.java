package eu.javaexperience.web.dispatch.url;

public class URLNodeTools
{
	public static URLNode[] createFrom(PreparedURL url)
	{
		URLNode[] ret = new URLNode[url.path.length];
		for(int i=0;i<url.path.length;++i)
		{
			if(0 == i)
			{
				TldUrlNode n = new TldUrlNode(url.path[0]);
				n.protocol = url.protocol;
				if(!"http".equals(n.protocol) || 80 != url.port)
				{
					n.port = url.port;
				}
				ret[0] = n;
			}
			else
			{
				ret[i] = new URLNode(url.path[i]);
				ret[i].isDomainNode = i < url.domainSize;
				ret[i-1].addChild(ret[i]);
			}
		}
		return ret;
	}
}
