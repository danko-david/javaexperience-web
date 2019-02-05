package eu.javaexperience.web.spider;

import static eu.javaexperience.log.LogLevel.*;
import static eu.javaexperience.log.LoggingTools.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSession;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.getBy.GetBy2;
import eu.javaexperience.interfaces.simple.publish.SimplePublish2;
import eu.javaexperience.interfaces.simple.publish.SimplePublish3;
import eu.javaexperience.io.FileContentMapper;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.storage.StoragesWarehouse;
import eu.javaexperience.proxy.ProxyStorage;
import eu.javaexperience.proxy.TorProxySpawner.ProxySource;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.semantic.references.MayNull;

/**
 * Spider and spider tools created for small web data mining tasks.
 * for example: if you have to gather some data from a website, you can easly do with this tools.
 * Some frequently task:
 * 	- gather all email address from a set of domain.
 *  - gather some product data from a website (price, name, category, order id, description of an article and other post processable or plain text properties)
 * 
 * In most cases this tasks can be simplified back to some simple step:
 * 	- parse page and collect links, leads to a mediate level or the the target itself.
 * 		this step may required to download an HTML page, parse and select the valuable links,
 * 		leads to the next level. Sometimes there is a simple way to collect all required links
 * 		to the target "data source": sitemaps. There's (in proper websites) specified
 * 		in robots.txt (http://domain.tld/robots.txt) check for "Sitemap:" entry, this (in most cases)
 * 		leads to other sitemap.xml shards. It's easier to download, parse and collect links from
 * 		this files (because they are canoncial links, so there's less possibily for the bad link processing)
 * 		(Note that some site not properly update this sitemaps file so check for "last modified" date before use them.)
 * 
 *  
 * 	Some experience:
 * 		- download and store pages in binary format, if character encoding not properly set
 * 			you may facing with partially useless result (Brace yourself, árvíztűrő tükörfúrógép is coming)
 * 
 * 		- Don't try (only if feel yourself pro enough for first try) process everything "for the first try"
 * 			I mean: keep away from methods parsing a listing page, go to the target page, parse everything and
 * 			saves processed data. If any subpage has a differences what we doesn't recognized that pages
 * 			(and this subpages) will be lost and after "processing the whole site" all it's hard to find out
 * 			what's failed, then we doomed to delete everything and process whole site again (or write your code
 * 			to skip processed page and download missing ones and selectively delete bad ones)
 * 
 *	 		Instead of this (you will need a lot of disk space) separate phases:
 *	 			1) download root page
 *	 			2) parse page and generate link list
 *	 			3) goto 1 with collected links until you get the target links
 *	 			4) download target
 *	 			5) parse target and produces output
 *	 
 *	 Note: use {@link FileContentMapper} to store downloaded pages.
 * 
 * */
public class SpiderTools
{
	public static void tryLogDownloadTime(String url, long dt)
	{
		tryLogFormat(SpiderFunctions.LOG, MEASURE, "Url download \"%s\" took %s ms", url, dt);
	}
	
	public static byte[] download(String url) throws MalformedURLException, IOException
	{
		return download(url, null);
	}
	
	public static byte[] download(String url, @MayNull Map<String,String> headers) throws MalformedURLException, IOException
	{
		return download(url, headers, null);
	}
	
	public static byte[] download(Proxy proxy, String url, @MayNull Map<String,String> headers) throws MalformedURLException, IOException
	{
		return download(proxy, url, headers, null);
	}
	
	
	public static byte[] download(String url, @MayNull Map<String,String> headers, String post_data) throws MalformedURLException, IOException
	{
		return download(null, new URL(url), headers, -1, null == post_data?null: post_data.getBytes());
	}
	
	
	public static byte[] download(Proxy proxy, String url, @MayNull Map<String,String> headers, String post_data) throws MalformedURLException, IOException
	{
		return download(proxy, new URL(url), headers, -1, null == post_data?null: post_data.getBytes());
	}
	
	
	
	public static byte[] download(URL url, Map<String,String> headers) throws IOException
	{
		return download(null, url, headers, -1, null);
	}
	
	public static byte[] download(Proxy proxy, URL url, Map<String,String> headers) throws IOException
	{
		return download(proxy, url, headers, -1, null);
	}
	
	public static byte[] download(Proxy proxy, URL url, Map<String,String> headers, byte[] POST_data) throws IOException
	{
		return download(proxy, url, headers, 60_000, POST_data);
	}
	
	public static byte[] download(Proxy proxy, URL url, Map<String,String> headers, int timeoutMs, byte[] POST_data) throws IOException
	{
		URLConnection connection = null;
		
		if(null == proxy)
		{
			connection = url.openConnection();
		}
		else
		{
			connection = url.openConnection(proxy);
		}
		
		if(timeoutMs > 0)
		{
			connection.setConnectTimeout(timeoutMs);
		}

		if(null != headers)
		{
			for(Entry<String, String> header:headers.entrySet())
			{
				if(null != header.getValue())
				{
					connection.addRequestProperty(header.getKey(), header.getValue());
				}
			}
		}
		
		if(null != POST_data)
		{
			connection.addRequestProperty("Content-Length", String.valueOf(POST_data.length));
			connection.setDoOutput(true);
			try
			(
				OutputStream os = connection.getOutputStream();
			)
			{
				if(null != POST_data)
				{
					os.write(POST_data);
					os.flush();
				}
			}
		}
		
		try(InputStream is = connection.getInputStream())
		{
			int ep = 0;
			int read = 0;
			byte[] ret = new byte[10240];
			
			while((read = is.read(ret, ep, ret.length-ep)) >= 0)
			{
				if(ep + read == ret.length)
				{
					ret = Arrays.copyOf(ret, ret.length*2);
				}
				
				ep+= read;
			}
			
			HttpURLConnection conn = ((HttpURLConnection) connection);
			int status = conn.getResponseCode();
			if(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
			{
				return download(proxy, new URL(conn.getHeaderField("Location")), headers, timeoutMs, POST_data);
			}

			return Arrays.copyOf(ret, ep);
		}
	}
	
	public static HttpRequestResult httpDownload(Proxy proxy, URL url, Map<String,String> headers, byte[] POST_data) throws IOException
	{
		URLConnection connection = null;
		
		if(null == proxy)
		{
			connection = url.openConnection();
		}
		else
		{
			connection = url.openConnection(proxy);
		}
		
		//((HttpURLConnection) connection).setInstanceFollowRedirects(true);
		
		if(null != headers)
		{
			for(Entry<String, String> header:headers.entrySet())
			{
				if(null != header.getValue())
				{
					connection.addRequestProperty(header.getKey(), header.getValue());
				}
			}
		}
		
		if(null != POST_data)
		{
			connection.addRequestProperty("Content-Length", String.valueOf(POST_data.length));
			connection.setDoOutput(true);
			try
			(
				OutputStream os = connection.getOutputStream();
			)
			{
				if(null != POST_data)
				{
					os.write(POST_data);
					os.flush();
				}
			}
		}
		
		HttpURLConnection httpConn = (HttpURLConnection) connection; 
		
		HttpRequestResult res = new HttpRequestResult();
		
		res.responseCode = httpConn.getResponseCode();
		
		res.headers = httpConn.getHeaderFields();
		
		try
		{
			res.responseStatus = res.headers.get(null).get(0);
		}
		catch(Exception e){}
		
		try(InputStream is = 200 == res.responseCode?httpConn.getInputStream():httpConn.getErrorStream())
		{
			int ep = 0;
			int read = 0;
			byte[] ret = new byte[10240];
			
			while((read = is.read(ret, ep, ret.length-ep))>0)
			{
				if(ep + read == ret.length)
				{
					ret = Arrays.copyOf(ret, ret.length*2);
				}
				
				ep+= read;
			}
			
			res.data = Arrays.copyOf(ret, ep);
		}
		
		return res;
	}
	
	public static class HttpRequestResult
	{
		public String responseStatus;
		public int responseCode;
		public byte[] data;
		public Map<String, List<String>> headers;
		
	}
	
	public static void processLinkPage(Document doc, SimplePublish2<Node, Collection<String>> selector, Collection<String> urls)
	{
		processLinkPage(doc.getChildNodes(), selector, urls);
	}
	
	public static void processLinkPage(NodeList nl, SimplePublish2<Node, Collection<String>> selector, Collection<String> urls)
	{
		for(int i=0;i<nl.getLength();++i)
		{
			Node n = nl.item(i);
			selector.publish(n, urls);
			processLinkPage(n.getChildNodes(), selector, urls);
		}
	}
	
	public static void downloadPage(String link, Map<String, byte[]> url_to_content, Map<String, String> headers) throws MalformedURLException, IOException
	{
		byte[] data = download(new URL(link), headers);
		url_to_content.put(link, data);
	}
	
	public static void downloadPages(Collection<String> links, Map<String, byte[]> url_to_content, Map<String, String> headers)
	{
		for(String url:links)
		{
			try
			{
				downloadPage(url, url_to_content, headers);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}			
		}
	}
	
	public static void processAndStoreListerPage
	(
		NodeList doc,
		String list_destination_file,
		SimplePublish2<Node, Collection<String>> selector,
		Collection<String> urls
	)
		throws FileNotFoundException, IOException
	{
		processLinkPage(doc, selector, urls);
		storeUrlList(list_destination_file, urls);
	}
	
	public static void loadFillUrlList(String source, Collection<String> urls) throws FileNotFoundException, IOException
	{
		IOTools.loadFillAllLine(source, urls);
	}
	
	public static void storeUrlList(String dst, Collection<String> urls) throws FileNotFoundException, IOException
	{
		try
		(
			PrintWriter pw = new PrintWriter(dst);
		)
		{
			for(String s:urls)
			{
				pw.println(URLEncoder.encode(s));
			}
			pw.flush();
		}
	}
	
	public static void discoverSiteInDepth
	(
		URL start_link,
		int depth,
		final Map<String, byte[]> download_to,
		final GetBy1<Boolean, URL> may_visit,
		final SimplePublish3<URL, List<URL>, List<URL>> url_map
	)
		throws MalformedURLException
	{
		AbstractWebSpider aws = new AbstractWebSpider(start_link)
		{
			@Override
			public boolean shallReturnByParsedDocument(URL link, String data, Document doc)
			{
				return false;
			}
			
			@Override
			public boolean preShallReturn(URL link)
			{
				return false;
			}
			
			@Override
			public boolean postShallReturn(URL link, byte[] data)
			{
				download_to.put(link.toString(), data);
				return false;
			}
			
			@Override
			public void publishSiteLinks(URL url, List<URL> all_link, List<URL> domain_links)
			{
				if(null != url_map)
				{
					url_map.publish(url, all_link, domain_links);
				}
			}
			
			@Override
			public boolean isSelfURL(URL URL)
			{
				return Boolean.TRUE == may_visit.getBy(URL);
			}
		};
		
		aws.getRecursiveSiteMapWhitoutDuplication(start_link, Integer.MAX_VALUE);
	}
	
	public static void fullSiteIterateOnDomain
	(
		URL start_link,
		final Map<String, byte[]> download_to,
		final GetBy1<Boolean, URL> may_visit,
		final SimplePublish3<URL, List<URL>, List<URL>> url_map
	)
			throws MalformedURLException
	{
		discoverSiteInDepth(start_link, Integer.MAX_VALUE, download_to, may_visit, url_map);
	}
	
	public static SimplePublish3<Map<String,byte[]>, Proxy, String> DOWNLOAD_INTO_STORAGE = new SimplePublish3<Map<String,byte[]>, Proxy, String>()
	{
		@Override
		public void publish(Map<String, byte[]> a, Proxy b, String c)
		{
			try
			{
				a.put(c, download(b, c, null));
			}
			catch(Exception e)
			{
				Mirror.throwSoftOrHardButAnyway(e);
			}
		}
	};
	
	protected static Map<String,String> HEADERS;
	static
	{
		Map<String, String> map = new SmallMap<>();
		map.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.104 Safari/537.36");
		map.put("Accept-Language", "hu-HU,hu;q=0.8,en-US;q=0.6,en;q=0.4");
		map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,");
		map.put("Cache-Control", "max-age=0");
		
		
		HEADERS = Collections.unmodifiableMap(map);
	}
	
	public static Map<String, String> getExtraHeaders()
	{
		return HEADERS;
	}
	
	public static SimplePublish3<Map<String,String>, Proxy, String> downloadIntoStorageWithEncoding(final String encoding)
	{
		return new SimplePublish3<Map<String, String>, Proxy, String>()
		{
			@Override
			public void publish(Map<String, String> a, Proxy b, String c)
			{
				try
				{
					byte[] data = download(b, c, null);
					a.put(c, new String(data, encoding));
				}
				catch(Exception e)
				{
					Mirror.throwSoftOrHardButAnyway(e);
				}
			}
		};
	}
	
	public static SimplePublish3<Map<String, byte[]>, Proxy, String> downloadIntoStorageRaw()
	{
		return new SimplePublish3<Map<String, byte[]>, Proxy, String>()
		{
			@Override
			public void publish(Map<String, byte[]> a, Proxy b, String c)
			{
				try
				{
					byte[] data = download(b, c, null);
					a.put(c, data);
				}
				catch(Exception e)
				{
					Mirror.throwSoftOrHardButAnyway(e);
				}
			}
		};
	}
	
	public static void downloadPagesIntoParallelWithProxies
	(
		final Map<String, byte[]> dst,
		Collection<String> src,
		final ProxyStorage spawnerStorage,
		final int proxies,
		final int concurrency,
		final boolean skip_exists
	)
	{
		downloadPagesIntoParallelWithProxies(null, dst, src, spawnerStorage, proxies, concurrency, skip_exists);
	}
	
	public static void downloadPagesIntoParallelWithProxies
	(
		final @MayNull GetBy2<byte[], Proxy, URL> downloader,
		final Map<String, byte[]> dst,
		Collection<String> src,
		final ProxyStorage spawnerStorage,
		final int proxies,
		final int concurrency,
		final boolean skip_exists
	)
	{
		final BlockingQueue<String> urls_queue = new LinkedBlockingQueue<>();
		urls_queue.addAll(src);
		
		final AtomicInteger nums = new AtomicInteger(concurrency);
		
		for(int i=0;i<concurrency;++i)
		{
			final int thread_ordinal = i;
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						String toDownload = null;
						while(null != (toDownload = urls_queue.poll()))
						{
							if(skip_exists)
							{
								if(dst.containsKey(toDownload))
								{
									continue;
								}
							}
							
							try
							{
								long t0 = System.currentTimeMillis();
								byte[] data = null;
								
								final int try_count = 10;
								for(int i=0;;++i)
								{
									try
									{
										ProxySource tp = spawnerStorage.getAtOffset(thread_ordinal % proxies);
										Proxy p = tp.getProxy();
										
										if(null == downloader)
										{
											data = SpiderTools.download(p, toDownload, SpiderTools.getExtraHeaders());
										}
										else
										{
											data = downloader.getBy(p, new URL(toDownload));
										}
										if(null != data)
										{
											break;
										}
									}
									catch(Exception e)
									{
										if(try_count == i)
										{
											tryLogFormat(SpiderFunctions.LOG, WARNING, "Can't download URL \"%s\" %s", toDownload, e.getMessage());
											break;
											//throw e;
										}
									}
								}
								
								if(null != data)
								{
									tryLogFormat(SpiderFunctions.LOG, MEASURE, "Url download \"%s\" took %s ms", toDownload, System.currentTimeMillis()-t0);
									dst.put(toDownload, data);
								}
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					catch(Throwable e)
					{
						e.printStackTrace();
					}
					
					synchronized (nums)
					{
						nums.decrementAndGet();
						nums.notifyAll();
					}
					
				};
			}.start();
		}
		
		synchronized (nums)
		{
			while(0 != nums.get())
			{
				try
				{
					nums.wait();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	//TODO better location for this method
	public static void disableSslVerification()
	{
		try
		{
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
			};

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}
	
	public static StoragesWarehouse<byte[]> getWarehouseStorage(String warehouse_dir, String unit_name)
	{
		return new StoragesWarehouse<>(warehouse_dir+"/"+unit_name+"/");
	}
	
/*	
	public static Map<String, Object> createEnvWithProxyStorage(String warehouse_dir, String unit_name)
	{
		Map<String, Object> env = new HashMap<>();
		
		StoragesWarehouse<byte[]> WAREHOUSE = getWarehouseStorage(warehouse_dir, unit_name);
		
		env.put("proxies", 5);
		env.put("concurrency", 15);
		
		env.put("warehouse", WAREHOUSE);
		env.put("tor", TOR);
		
		return env;
	}
	
	
	
	protected static LxcUtilsApi api = null;
	protected static ProxyStorage TOR = null; 
	
	static
	{
		JavaExperienceLoggingFacility.addStdOut();
		if(null == TOR)
		{
			try
			{
				TOR = new TorSpawnerStorage(new TorProxySpawner("/home/szupervigyor/tor"), 5);
			}
			catch(Exception e)
			{
				Mirror.propagateAnyway(e);
			}
		}
	}
	
	public static ProxyStorage getProxyStorage()
	{
		return TOR;
	}*/
}
