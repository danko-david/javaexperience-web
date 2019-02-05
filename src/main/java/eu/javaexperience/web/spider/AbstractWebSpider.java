package eu.javaexperience.web.spider;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.collection.set.OneShotSet;
import eu.javaexperience.document.DocumentTools;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.StringTools;
import eu.javaexperience.web.HttpTools;

public abstract class AbstractWebSpider
{
	protected final String thisDomain;
	protected URL baseURL = null;

	public void setBaseUrl(URL base)
	{
		this.baseURL = base;
	}

	public URL getBaseURL()
	{
		return baseURL;
	}
	
	protected boolean isBaseURLDynamic = true;
	
	public GetBy1<byte[], URL> downloadFacitlity = defaultDownloadFacility;

	public GetBy1<Document, String> parser = defaultParser;
			
	public AbstractWebSpider(URL startlink) throws MalformedURLException
	{
		baseURL = startlink;
		thisDomain = getLinkDomain(startlink);
	}

	/**
	 * Link alapján átugroja-e az oldal feldolgozását?
	 * */
	public abstract boolean preShallReturn(URL link);
	/**
	 * A letöltött oldal adata alapján átugorjuk-e az oldal feldolgozását?
	 * */
	public abstract boolean postShallReturn(URL link, byte[] data);

	/**
	 * A Jsouppal feldolgozott oldal adata alapján át kell-e ugrani az oldal feldolgozását?
	 * */
	public abstract boolean shallReturnByParsedDocument(URL link,String data, Document doc);

	public Set<URL> getRecursiveSiteMapWhitoutDuplication(URL cim,int depth)
	{
		return getRecursiveSM(cim, depth);
	}

	private HashSet<URL> SK = new HashSet<>();
	
	protected static final GetBy1<Boolean, Node> selectHrefs = new GetBy1<Boolean, Node>()
	{
		@Override
		public Boolean getBy(Node a)
		{
			String name = a.getNodeName();
			if(null == name || null == a.getAttributes().getNamedItem("href"))
			{
				return Boolean.FALSE;
			}

			return "a".equals(name) || "area".equals(name);
		}
	};

	
	public static final GetBy1<byte[], URL> defaultDownloadFacility = new GetBy1<byte[], URL>()
	{
		@Override
		public byte[] getBy(URL a)
		{
			try
			{
				URLConnection connection = a.openConnection();
				try(InputStream is = connection.getInputStream())
				{
					return IOTools.loadAllFromInputStream(is);
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					return null;
				}
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				return null;
			}
		}
	};
	
	public static final GetBy1<Document, String> defaultParser = new GetBy1<Document, String>()
	{
		@Override
		public Document getBy(String a)
		{
			try
			{
				Method m = Mirror.getClassMethodOrNull("org.jsoup.Jsoup", "parse", String.class);
				//return Jsoup.parse(a);
				return (Document) m.invoke(null, a);
				//return SaxHElementParser.parseDocument(a);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				return null;
			}
		}
	};
	
	protected Set<URL> singleSet(URL elem)
	{
		return new OneShotSet<URL>(elem);
	}
	
	private Set<URL> getRecursiveSM(URL cim, int depth)
	{
		try
		{
			cim = new URL(norlamizeUrl.getBy(cim.toString()));
		}
		catch (MalformedURLException e3)
		{
			e3.printStackTrace();
			return singleSet(cim);
		}

		//ha itt már jártunk vagy a depth limiten túl vagyunk akkor visszatérünk
		if(SK.contains(cim) || depth < 0)
		{
			return singleSet(cim);
		}

		SK.add(cim);

		if(preShallReturn(cim))
		{
			return singleSet(cim);
		}
		
		ArrayList<Node> c = null;
		URL currentBaseURL = null;
		
		{
			Document doc = null;
			
			{
				String tart = null;
				
				//parse-és base URL alapján
				{
					byte[] data = downloadFacitlity.getBy(cim);
					
					if(null == data)
					{
						return singleSet(cim);
					}
					
					if(postShallReturn(cim, data))
					{
						return singleSet(cim);
					}
					
					tart = new String(data);
				}
				
		
				try
				{
					doc = parser.getBy(tart);
				}
				catch (Exception e1){}//Jsoup.parse(tart);
		
				if(shallReturnByParsedDocument(cim, tart, doc))
				{
					return singleSet(cim);
				}
		
				if(tart == null || null == doc)
				{
					return singleSet(cim);
				}
			}//tart és data ekkor el lesz engedve
			
			currentBaseURL = determineBaseURL(doc, cim);
	
			if(null == baseURL && null == currentBaseURL)
			{
				currentBaseURL = cim;
			}
			else if(null == baseURL)
			{
				currentBaseURL = baseURL;
			}
		
			c = new ArrayList<>();
			DocumentTools.selectAll(doc, c, selectHrefs);
		}
		//a dokumentumis el lesz engedve
		
		
		//Elements c = doc.select("a[href] , area[href]");
		URL buffer = null;
		ArrayList<URL> out = new ArrayList<>();

		for(Node node:c)
		{
			//megkeresi href attributumát
			Node href = node.getAttributes().getNamedItem("href");
			if(null == href)
			{
				continue;
			}
			
			try
			{
				out.add(new URL(norlamizeUrl.getBy(getAbsPath(href.getTextContent(), currentBaseURL))));
			}
			catch(Exception e)
			{
				try
				{
					out.add(buffer);
				}
				catch(Exception dsfsf)
				{
					dsfsf.printStackTrace();
				}
			}
		}
		
		c = null;

		//null nélkül
		ArrayList<URL> dfd = new ArrayList<>(out.size());
		for(URL a:out)
		{
			if(null != a && isSelfURL(a))
			{
				dfd.add(a);
			}
		}

		
		//ha nem saját URL akkor törlöm a listából
		for(int i=0;i<out.size();i++)
		{
			URL at = out.get(i);
			if(null != at && !isSelfURL(at))
			{
				out.set(i, null);
			}
		}

		//elérhetővé tesszük az oldalon lévő linkeket
		publishSiteLinks(cim, out, dfd);
		
		out = dfd;
		
		HashSet<URL> ki = new HashSet<>(); 
		ki.add(cim);

/*		HashSet<URL> nextSkip = new HashSet<>();
		nextSkip.addAll(skipList);
		
		//a régi listába beleratom az új URL-eket
		for(URL u:out)
		{
			try
			{
				skipList.add(new URL(norlamizeUrl.getBy(u.toString())));
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
*/
		kint: for(URL news:out)
		{
			//ha a skiplistbe már benn volt akkor továbbmegünk az outok között
			if(SK.contains(news))
			{
				continue kint;
			}

			Set<URL> tobbi = getRecursiveSM(news, depth-1);
			
			for(URL u:tobbi)
			{
				ki.add(u);
			}
		}
		return SK;
	}

	protected void publishSiteLinks(URL cim, List<URL> page_links, List<URL> self_links)
	{
		
	}

	public static final GetBy1<Boolean, Node> selectBase = new GetBy1<Boolean, Node>()
	{
		@Override
		public Boolean getBy(Node a)
		{
			return "base".equals(a.getNodeName()) && null != a.getAttributes().getNamedItem("href");
		}
	};
	
	//http://www.avgsp.hu/index.php?option=com_content&task=view&task=view&task=view&id=24&id=855&id=522&Itemid=index.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.phpindex.php?option=com_content&Itemid=183index.phpindex.phpindex.phpjavascript:history.go(-1)index.phpjavas
	//ezt javítani kell
	public static String getAbsPath(String url,URL host)
	{
		try
		{
			url = url.trim();
			URL cim = new URL(url);
			//ha ez egy értlemes webcím akkor nincs path feloldás, azt az URL-t adjuk vissza (abba a domainba tartozót)
			if(!cim.getHost().equals(host.getHost()))
			{
				return "";
			}
			
			return url;
		}
		catch (MalformedURLException e)
		{
			if(url.startsWith("/"))// /sdfgasf -ek
			{
				String on = host.toString();
				int index = on.indexOf('/', 9);// https:// utáni / keresése: https://google.hu/ <=ez itten
				if(-1 != index)
				{
					on = on.substring(0, index);//van domain záró /, odáig levágom (/ nélkül)
				}
				return on+url;//a url-en van kezdő / jel mert itt a vezérlés
			}
			else
			{
				return host.getProtocol()+"://"+host.getHost()+relative(host.getPath(), url);
			}
		}
	}
	
	protected static void delPrev(String[] arr, int pos)
	{
		for(int i=pos-1;i>=0;i--)
		{
			if(null != arr[i])
			{
				arr[i] = null;
				return;
			}
		}
	}
	
	public static String relative(String path, String rel)
	{
		if("".equals(rel))
		{
			return "";
		}
		
		if(rel.startsWith("/"))// if relative url starts with / then it's from the root
		{
			return rel;
		}
		
		String[] ps = path.split("/");
		String[] re = rel.split("/");
		
		String[] build = new String[ps.length+re.length]; 
		
		if(!path.endsWith("/"))
		{
			ps[ps.length-1] = null;
		}
		
		for(int i=0;i<ps.length;++i)
		{
			build[i] = ps[i];
		}
		
		for(int i=0;i<re.length;++i)
		{
			build[ps.length+i] = re[i];
		}
		
		for(int i=0;i<build.length;++i)
		{
			String p = build[i];
			if(".".equals(p) || "".equals(p))
			{
				build[i] = null;
			}
			else if("..".equals(p))
			{
				build[i] = null;
				delPrev(build, i);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(String s:build)
		{
			if(null != s)
			{
				sb.append("/");
				sb.append(s);
			}
		}
		
		return sb.toString();
	}
	
	public static void main(String[] args) throws MalformedURLException
	{
		System.out.println(betterNormalizeURL.getBy("http://www.site.com/list?page=1&page=7"));
		System.out.println(betterNormalizeURL.getBy("http://www.site.com/"));
		test("./index.php", new URL("https://google.hu/"), "https://google.hu/index.php");
		
		test("index.php", new URL("https://google.hu/"), "https://google.hu/index.php");
		test("/index.php", new URL("https://google.hu/"), "https://google.hu/index.php");
		
		
		test("./index.php", new URL("https://google.hu/valami/"), "https://google.hu/valami/index.php");
		test("index.php", new URL("https://google.hu/valami/"), "https://google.hu/valami/index.php");
		test("/index.php", new URL("https://google.hu/valami/"), "https://google.hu/index.php");
		
		
		test("../index.php", new URL("https://google.hu/valami/"), "https://google.hu/index.php");
		
		test("../index.php", new URL("https://google.hu/valami/asdf"), "https://google.hu/index.php");
		test("../index.php", new URL("https://google.hu/valami/asdf/"), "https://google.hu/valami/index.php");
		test("../index.php", new URL("https://google.hu/valami/asdf/../"), "https://google.hu/index.php");
		test("../../index.php", new URL("https://google.hu/valami/asdf/"), "https://google.hu/index.php");
		test("../../index.php", new URL("https://google.hu/valami/asdf/../"), "https://google.hu/index.php");
		
		test("http://google.hu/index.php", new URL("https://google.hu/valami/"), "http://google.hu/index.php");
		
		test("http://facebook.hu/index.php", new URL("https://google.hu/valami/"), "http://google.hu/index.php");
		
	}
	
	public static void test(String file, URL url, String result)
	{
		String ret = getAbsPath(file, url);
		String out = url+" "+file+" => "+result;
		if(ret.equals(result))
		{
			System.out.println(out);
		}
		else
		{
			System.out.println("FAIL: "+out+" OUT: "+ret);
		}
	}
	
	public static final GetBy1<Boolean, Node> selectBaseWithHref = new GetBy1<Boolean, Node>()
	{
		@Override
		public Boolean getBy(Node a)
		{
			return "base".equalsIgnoreCase(a.getNodeName()) && a.getAttributes().getNamedItem("href") != null;
		}
	};
	
	public static URL determineBaseURL(Document webcontent, URL url)
	{
		Node c = DocumentTools.findFirst(webcontent, selectBaseWithHref);
		if(null != c)
		{
			try
			{
				Node val = c.getAttributes().getNamedItem("href");
				if(null != val)
				{
					String ki = val.getTextContent();//TODO ebben nem vagyok biztos
					if(null == ki || ki.length() == 0)
					{
						ki = url.toString();
					}
					
					int ind = ki.lastIndexOf('/');
					if(ind != -1)
					{
						ki = ki.substring(0, ind);
						
						while(ki.charAt(ki.length()-1)=='/')
						{
							ki = ki.substring(0,ki.length()-1);
							ki.trim();
						}
					}
					
					return new URL(ki);
				}
				
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println(url);
				return url;
			}
		}
		return url;
	}
	
	public GetBy1<String, String> norlamizeUrl = defaultNormalizeURL;
	
	private static final Pattern slash = Pattern.compile("(//)+");//a kezdő http://!
	
	public static final GetBy1<String, String> defaultNormalizeURL = new GetBy1<String, String>()
	{
		@Override
		public String getBy(String s)
		{
			String ki = s;
			int n = 0;
			
			while((n = ki.indexOf("#"))!=-1)
			{
				ki = ki.substring(0,n);
			}
			
			ki = slash.matcher(ki).replaceAll("/");
			
			ki = StringTools.replaceAllStrings(ki, ":/", "://");
			
			return ki;
		}
	}; 
	
	public static final GetBy1<String, String> betterNormalizeURL = new GetBy1<String, String>()
	{
		private final Pattern slash = Pattern.compile("(//)+");//a kezdő http://!
		
		@Override
		public String getBy(String s)
		{
			try
			{
				URL re = new URL(s);
				
				StringBuilder sb = new StringBuilder();
				sb.append(re.getProtocol());
				sb.append("://");
				sb.append(re.getHost());
				if(re.getPort() > 0)
				{
					sb.append(":");
					sb.append(re.getPort());
				}
				sb.append(slash.matcher(re.getPath()).replaceAll("/"));
				
				if(null != re.getQuery())
				{
					//TODO dupla paraméterek
					Map<String,String[]> map = HttpTools.resolvMap(re.getQuery());
					if(map.size() > 0)
					{
						//sb.append("?");
						Map<String,String> out = new SmallMap<>();
						
						for(Entry<String, String[]> kv:map.entrySet())
						{
							String k = kv.getKey();
							String[] vv = kv.getValue();
							if(null != k && k.length() > 0)
							{
								if(null != vv && vv.length > 0)
								{
									for(String v:vv)
									{
										if(null != v)
										{
											out.put(k, v);
										}
									}
								}
								
							}
						}
						
						sb.append(HttpTools.renderRequestParams((Map)out));
					}
				}
					
					return sb.toString();
			}
			catch(Exception e)
			{
				return null;
			}
		}
	}; 
	
	/**
	 * Ha az URL saját oldalhoz tartozik akkor ide is tovább fogunk menni.
	 * */
	public abstract boolean isSelfURL(URL URL);

	public boolean __defaultIsSelfURL(String url,String thisHost)
	{
		try {
			URL cim= new URL(url);
			System.out.println(cim.getHost());
			return cim.getHost().equals(thisHost);
		} catch (MalformedURLException e) {
			return true;
		}

	}

	public boolean __defaultIsSelfURLbyDomain(String url,String thisHost)
	{
		try {
			URL cim= new URL(url);
			String[] domtags = cim.getHost().split("\\.");
			if(domtags.length == 0)
				return true;

			else if(domtags.length == 1)
				return domtags[0].equals(thisHost);

			else
				return (domtags[domtags.length-2]+"."+domtags[domtags.length-1]).equals(thisHost);

		} catch (MalformedURLException e) {
			return true;
		}

	}

	public static String getLinkDomain(URL link) throws MalformedURLException
	{
		String[] pre = link.getHost().split("\\.");
		if(pre.length==1)
			return pre[0];

		return pre[pre.length-2]+"."+pre[pre.length-1];
	}
}