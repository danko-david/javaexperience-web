package eu.javaexperience.web.dispatch.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import eu.javaexperience.arrays.ArrayTools;
import eu.javaexperience.collection.map.NullMap;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.text.StringTools;
import eu.javaexperience.url.UrlTools;
import eu.javaexperience.web.HttpTools;

public class PreparedURL
{
	public PreparedURL(URL url)
	{
		process(url);
	}

	protected PreparedURL original;
	
	public void setOriginal(PreparedURL purl)
	{
		original = purl;
	}
	
	public PreparedURL getOriginal()
	{
		return original;
	}
	
	protected int defaultPort;
	protected String protocol;
	protected String user;
	protected int port;
	protected String[] path;
	protected Map<String,String[]> params;
	
	public Map<String,String[]> getParams()
	{
		return params;
	}
	
	public String getParameter(String key)
	{
		String[] ret = params.get(key);
		if(null == ret)
		{
			return null;
		}
		
		if(ret.length > 0)
		{
			return ret[0];
		}
		
		return null;
	}
	
	
	protected int domainSize;
	
	public static final Pattern domSplit = Pattern.compile("\\.");
	public static final Pattern pathSplit = Pattern.compile("/+");
	
	public PreparedURL(String protocol,String user,String domain,int port, int defaultPort, String path,String params)
	{
		process(protocol, user, domain, port,  defaultPort, path, params);
	}
	
	public PreparedURL(HttpServletRequest req)
	{
		//this(req.getProtocol(),req.getRemoteUser(),req.getServerName(),req.getServerPort(),req.getRequestURI(),convMapMulti(req.getParameterMap()));
		process(req.getRequestURL().toString());
	}
	
	public PreparedURL(String protocol,String user,String domain, int port, int defaultPort, String path,Map<String,String[]> params)
	{
		process(protocol, user, domain, port, defaultPort, path, params);
	}
	
	protected void process(URL url)
	{
		process(url.getProtocol(),url.getUserInfo(),url.getHost(),url.getPort()==-1?url.getDefaultPort():url.getPort(), url.getDefaultPort(), url.getPath(),url.getQuery());
	}
	
	protected void process(String protocol,String user,String domain,int port, int defaultPort, String path,String params)
	{
		process(protocol,user,domain,port, defaultPort, path,params == null? null:HttpTools.resolvMap(params));
	}
	
	private void process(String url)
	{
		try
		{
			process(new URL(url));
		}
		catch(MalformedURLException e)
		{
			throw new RuntimeException(e);
			
		}
	}
	
	private void process(String protocol,String user,String domain, int port, int defaultPort, String path,Map<String,String[]> params)
	{
		this.protocol = protocol;
		this.user = user;
		this.port = port;
		this.defaultPort = defaultPort;
		String[] doma = domSplit.split(domain);
		
		domainSize = doma.length;
		
		String[] paths = StringTools.whitoutNullAndEmptyString(pathSplit.split(path));
		
		UrlTools.modifyUrlDecode(paths);
		
		if(doma.length > 0)
			ArrayTools.modifyReverse(doma);
		
		this.path = doma.length == 0?paths:ArrayTools.arrayConcat(doma, paths);
		
		if(null != params)
		{
			this.params = params;
		}
		else
		{
			this.params = NullMap.instance;
		}
	}
	
	public int getDomainElements()
	{
		return domainSize;
	}
	
	protected int pathPointer = 0;
	
	public boolean isPointerValid()
	{
		return path.length > pathPointer;
	}
	
	public int getRemainElementNum()
	{
		return path.length - pathPointer;
	}
	
	public int getUrlLength()
	{
		return path.length;
	}
	
	public int getUrlPointer()
	{
		return pathPointer;
	}
	
	public String getCurrentURLElement()
	{
		if(pathPointer >= path.length)
		{
			return null;
		}
		return path[pathPointer];
	}
	
	public String getNextURLElement()
	{
		return path[pathPointer+1];
	}

	public void jumpNextURLElement(int n)
	{
		pathPointer+=n;
	}
	
	public String getNextURLElement(int n)
	{
		return path[pathPointer+n];
	}

	public boolean hasNextURLElement()
	{
		return pathPointer < path.length-1;
	}
	
	public boolean hasPerviousURLElement()
	{
		return pathPointer > 0;
	}
	
	public String previousURLElement()
	{
		return path[pathPointer-1];
	}
	
	public String previousURLElement(int n)
	{
		return path[pathPointer-n];
	}
	
	public String getURLElementAndJumpNext()
	{
		return path[pathPointer++];
	}
	
	public int getURLElementIndex()
	{
		return pathPointer;
	}
	
	public void jumpNextURLElement()
	{
		++pathPointer;
	}
	
	public String tryGetNextURLElement()
	{
		if(pathPointer-1 < path.length)
		{
			return path[pathPointer+1];
		}
	
		return null;
	}

	public String tryGetCurrentURLElement()
	{
		if(pathPointer < path.length)
		{
			return path[pathPointer];
		}
		
		return null;
	}
	
	
	public List<String> asList()
	{
		return Arrays.asList(path);
	}
	
	public Iterator<String> iterator()
	{
		return Arrays.asList(path).iterator();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<path.length;i++)
		{
			if(i == pathPointer)
				sb.append("'");
			else
				sb.append("\"");
			
			sb.append(path[i]);
	
			if(i == pathPointer)
				sb.append("' ");
			else
				sb.append("\" ");
			}
		
		return sb.toString();
	}

	public boolean isEndOfPath()
	{
		return !isPointerValid();
	}
	
	protected String url = null;
	
	public String getDomainOnly()
	{
		StringBuilder sb = new StringBuilder();
		buildDomain(sb);
		return sb.toString();
	}
	
	protected void buildDomain(StringBuilder sb)
	{
		for(int i=domainSize-1;i>=0;--i)
		{
			if(i != domainSize-1)
			{
				sb.append(".");
			}
			sb.append(path[i]);
		}
	}
	
	
	public String getUrl()
	{
		if(null == url)
		{
			url = getUrl(Integer.MAX_VALUE, true);
		}
		return url;
	}
	
	protected URL toUrl;
	
	public URL toUrl()
	{
		if(null == toUrl)
		{
			try
			{
				toUrl = new URL(getUrl());
			}
			catch (MalformedURLException e)
			{
				Mirror.throwSoftOrHardButAnyway(e);
			}
		}
		
		return toUrl;
	}
	
	public UrlBuilder toBuilder()
	{
		return new UrlBuilder(getUrl());
	}
	
	public static void main(String[] args) throws Throwable
	{
		PreparedURL purl = new PreparedURL(new URL("http://szupervigyor:david@sites.ddsi.hu/site/page/?paramG&paramA=b&paramB=v"));
		System.out.println(purl.getUrl());
	}

	public void jumpPrevURLElement()
	{
		--pathPointer;
	}

	public String getPath()
	{
		return toUrl().getPath();
	}

	public String getProtocol()
	{
		return protocol;
	}

	public int getPort()
	{
		return port;
	}
	
	public @MayNull String atPath(int index)
	{
		index += domainSize;
		if(index < 0 || index >= path.length)
		{
			return null;
		}
		return path[index];
	}

	public String getUri()
	{
		return getUrl();
	}

	public String getQuery()
	{
		StringBuilder sb = new StringBuilder();
		HttpTools.renderRequestParams(params, sb);
		return sb.toString();
	}

	public String getUrl(int urlPointer, boolean reqParam)
	{
		StringBuilder sb = new StringBuilder();
		int m = 0;
		out:
		{
			sb.append(protocol);
			sb.append("://");
			
			try
			{
				for(int i=domainSize-1;i>=0;--i)
				{
					if(i != domainSize-1)
					{
						sb.append(".");
					}
					sb.append(path[i]);
					if(++m >= urlPointer)
					{
						break out;
					}
				}
			}
			finally
			{
				if(port > 1 && port != defaultPort)
				{
					sb.append(":");
					sb.append(port);
				}
			}
			
			for(int i= domainSize;i<path.length;++i)
			{
				sb.append("/");
				sb.append(path[i]);
				if(++m >= urlPointer)
				{
					break out;
				}
			}
			
			if(domainSize == path.length)
			{
				sb.append("/");
			}
			
			if(reqParam)
			{
				HttpTools.renderRequestParams(params, sb);
			}
		}
		return sb.toString();
	}

	public void setPathPointer(int num)
	{
		pathPointer = num;
	}

	public int getPathPointer()
	{
		return pathPointer;
	}

	public String getRemainingPath()
	{
		int from = domainSize;
		if(pathPointer > from)
		{
			from = pathPointer;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=from;i<path.length;++i)
		{
			sb.append("/");
			sb.append(path[i]);
		}
		
		return sb.toString();
	}

	public String getLast()
	{
		return path[path.length-1];
	}
}