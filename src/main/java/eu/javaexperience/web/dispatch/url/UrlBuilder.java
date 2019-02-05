package eu.javaexperience.web.dispatch.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import eu.javaexperience.arrays.ArrayTools;
import eu.javaexperience.collection.CollectionTools;
import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.StringTools;
import eu.javaexperience.url.UrlTools;
import eu.javaexperience.web.HttpTools;

public class UrlBuilder implements Cloneable
{
	protected int defaultPort;
	protected String protocol = "url";
	protected String user;
	protected int port;
	
	protected ArrayList<String> domain = new ArrayList<>();
	protected ArrayList<String> path = new ArrayList<>();
	protected Map<String,String[]> params;
	
	public UrlBuilder()
	{}
	
	public UrlBuilder(String url)
	{
		process(url);
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
	
/******************************* InitialParsing *******************************/
	
	public UrlBuilder(URL url)
	{
		process(url);
	}
	
	public UrlBuilder(String protocol,String user,String domain,int port, int defaultPort, String path,String params)
	{
		process(protocol, user, domain, port,  defaultPort, path, params);
	}
	
	public UrlBuilder(HttpServletRequest req)
	{
		process(req.getRequestURL().toString());
	}
	
	public UrlBuilder(String protocol,String user,String domain, int port, int defaultPort, String path,Map<String,String[]> params)
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
	
	private void process
	(
		String protocol,
		String user,
		String domain,
		int port,
		int defaultPort,
		String path,Map<String,String[]> params
	)
	{
		this.protocol = protocol;
		this.user = user;
		this.port = port;
		this.defaultPort = defaultPort;
		setDomain(domain);
		setPath(path);
		this.params = null == params?new SmallMap<String, String[]>():params;
	}
	
	
/*************************** Chain Linkable Setters ***************************/
	
	public UrlBuilder setProtocol(String protocol)
	{
		this.protocol = protocol;
		return this;
	}
	
	public UrlBuilder setPort(int port)
	{
		this.port = port;
		return this;
	}
	
	public String[] getPathSegments()
	{
		return path.toArray(Mirror.emptyStringArray);
	}
	
	public UrlBuilder setPathSegments(String[] arr)
	{
		path.clear();
		arr = ArrayTools.copy(arr);
		UrlTools.modifyUrlDecode(arr);
		CollectionTools.copyInto(arr, path);
		return this;
	}
	
	public UrlBuilder setPath(String path)
	{
		String[] paths = StringTools.whitoutNullAndEmptyString(PreparedURL.pathSplit.split(path));
		UrlTools.modifyUrlDecode(paths);
		this.path.clear();
		CollectionTools.copyInto(paths, this.path);
		return this;
	}
	
	public UrlBuilder setDomain(String domain)
	{
		this.domain.clear();
		CollectionTools.copyReverseInto(PreparedURL.domSplit.split(domain), this.domain);
		return this;
	}
	
	public UrlBuilder withAddedParameter(String key, String value)
	{
		String[] arr = params.get(key);
		if(null == key || null == arr)
		{
			params.put(key, new String[]{value});
			return this;
		}
		
		params.put(key, ArrayTools.arrayAppend(value, arr));
		
		return this;
	}
	
	public UrlBuilder withoutParameterValue(String key, String value)
	{
		String[] vals = params.get(key);
		if(null != vals)
		{
			if(ArrayTools.contains(vals, value))
			{
				params.put(key, ArrayTools.whitoutElement(vals, value));
			}
		}
		
		return this;
	}
	
	@Override
	public UrlBuilder clone()
	{
		UrlBuilder ret = new UrlBuilder();
		ret.defaultPort = defaultPort;
		ret.protocol = protocol;
		ret.user = user;
		ret.port = port;
		
		ret.domain = new ArrayList<>(domain);
		ret.path = new ArrayList<>(path);
		ret.params = new SmallMap<>(params);
		return ret;
	}

	public UrlBuilder withoutParameters()
	{
		params.clear();
		return this;
	}
	
	public Map<String, String[]> getParams()
	{
		return params;
	}
	
	public void renderHost(Appendable sb)
	{
		try
		{
			for(int i=domain.size()-1;i>=0;--i)
			{
				if(i != domain.size()-1)
				{
					sb.append(".");
				}
				sb.append(domain.get(i));
			}
		}
		catch(Exception e)
		{
			Mirror.propagateAnyway(e);
		}
	}
	
	public String getUrl()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(protocol);
		sb.append("://");
		
		renderHost(sb);
		
		if(port > 1 && port != defaultPort)
		{
			sb.append(":");
			sb.append(port);
		}
		
		for(String s:path)
		{
			sb.append("/");
			sb.append(s);
		}
		
		HttpTools.renderRequestParams(params, sb);
		
		return sb.toString();
	}

	public UrlBuilder withExactParameters(Map<String, String[]> params)
	{
		this.params = params;
		return this;
	}

	public String getHost()
	{
		StringBuilder sb = new StringBuilder();
		renderHost(sb);
		return sb.toString();
	}
}