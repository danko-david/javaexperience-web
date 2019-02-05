package eu.javaexperience.web.spider;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Proxy;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import eu.javaexperience.arrays.ListMapAdapter;
import eu.javaexperience.collection.PublisherCollection;
import eu.javaexperience.functional.PointOfCreationException;
import eu.javaexperience.interfaces.simple.SimpleGet;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.getBy.GetBy2;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.io.FileContentMapper;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.storage.StoragesWarehouse;
import eu.javaexperience.log.JavaExperienceLoggingFacility;
import eu.javaexperience.log.Loggable;
import eu.javaexperience.log.Logger;
import eu.javaexperience.measurement.Measure;
import eu.javaexperience.proxy.TorProxySpawner.ProxySource;
import eu.javaexperience.proxy.ProxyStorage;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.annotation.FunctionDescription;
import eu.javaexperience.annotation.FunctionVariableDescription;
import eu.javaexperience.document.DocumentTools;
import static eu.javaexperience.log.LogLevel.*;
import static eu.javaexperience.log.LoggingTools.*;


/**
 * watch -n 0,2 "find . | wc -l"
 * */
public class SpiderProcessFunctions
{
	protected static final Logger LOG = SpiderFunctions.LOG;
	
	public static SimplePublish1<Map<String, Object>> print_time_checkpoint_ms(final String name)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			@Override
			public void publish(Map<String, Object> a)
			{
				System.out.println(Measure.checkpoint(name));
			}
		};
	}
	
	public static SimplePublish1<Map<String, Object>> print_storage_size(final String label, final String key)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			@Override
			public void publish(Map<String, Object> a)
			{
				Object o = a.get(key);
				if(null == o)
				{
					System.out.println(label+" storage_size: null");
				}
				else if(o instanceof Collection)
				{
					System.out.println(label+" collection size: "+((Collection)o).size());
				}
				else if(o instanceof Map)
				{
					System.out.println(label+" map size: "+((Map)o).size());
				}
				else if(o.getClass().isArray())
				{
					System.out.println(label+" array size: "+Array.getLength(o));
				}
				else
				{
					System.out.println(label+" is not storage: "+o);
				}
			}
		};
	}
	
	
	public static GetBy1 get_page_storage_and_insert_scope(final String warehouse, final String storage_name)
	{
		return new GetBy1<FileContentMapper<byte[]>, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			@Override
			public FileContentMapper<byte[]> getBy(Map<String, Object> env)
			{
				StoragesWarehouse<byte[]> wh = assertCastType(StoragesWarehouse.class, env.get(warehouse), POINT);
				FileContentMapper<byte[]> store = null;
				try
				{
					store = wh.getStorage(storage_name);
				}
				catch (FileNotFoundException e)
				{
					Mirror.throwSoftOrHardButAnyway(e);
				}
				env.put(storage_name, store);
				return store;
			}
		};
	}
	
	public static GetBy1<Collection<?>, Map<String,Object>> get_storage_as_collection_and_insert_scope(final String warehouse, final String storage_name)
	{
		return get_storage_as_collection_and_insert_scope(warehouse, storage_name, false);
	}
	
	public static GetBy1<Collection<?>, Map<String,Object>> get_storage_as_collection_and_insert_scope(final String warehouse, final String storage_name, final boolean clear_brefore_use)
	{
		return new GetBy1<Collection<?>, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			@Override
			public Collection<?> getBy(Map<String, Object> env)
			{
				StoragesWarehouse<byte[]> wh = assertCastType(StoragesWarehouse.class, env.get(warehouse), POINT);
				Collection<?> store = null;
				try
				{
					FileContentMapper<byte[]> in = wh.getStorage(storage_name);
					store = new ListMapAdapter<>(in, ListMapAdapter.INDEXED_KEYS);
					if(clear_brefore_use)
					{
						store.clear();
					}
				}
				catch (FileNotFoundException e)
				{
					Mirror.throwSoftOrHardButAnyway(e);
				}
				env.put(storage_name, store);
				return store;
			}
		};
	}
	
	public static GetBy1<FileContentMapper<?>, Map<String,Object>> get_storage_as_map_and_insert_scope(final String warehouse, final String storage_name, final boolean clear_brefore_use)
	{
		return new GetBy1<FileContentMapper<?>, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			@Override
			public FileContentMapper<?> getBy(Map<String, Object> env)
			{
				StoragesWarehouse<byte[]> wh = assertCastType(StoragesWarehouse.class, env.get(warehouse), POINT);
				FileContentMapper<?> store = null;
				try
				{
					store = wh.getStorage(storage_name);
					if(clear_brefore_use)
					{
						store.clear();
					}
				}
				catch (FileNotFoundException e)
				{
					Mirror.throwSoftOrHardButAnyway(e);
				}
				env.put(storage_name, store);
				return store;
			}
		};
	}
	
	public static <T> GetBy1 getAndAbsorb(final String key)
	{
		return new GetBy1<T, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public T getBy(Map<String, Object> a)
			{
				T ret = (T) a.get(key);
				a.remove(key);
				if(null == ret)
				{
					throw new RuntimeException("getAndAbsorb: value not present under key: "+key, POINT);
				}
				
				return ret;
			}
		};
	}
	
	public static <T> GetBy1 get(final String key)
	{
		return new GetBy1<T, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public T getBy(Map<String, Object> a)
			{
				T ret = (T) a.get(key);
				if(null == ret)
				{
					throw new RuntimeException("get: value not present under key: "+key, POINT);
				}
				
				return ret;
			}
		};
	}
	
	public static <T> GetBy1 createInto(final String key, final SimpleGet<?> collection_creator)
	{
		return new GetBy1<T, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public T getBy(Map<String, Object> a)
			{
				T ret = (T) collection_creator.get();
				a.put(key, ret);
				return ret;
			}
		};
	}
	
	public static <T> GetBy1 create_into_if_nonex(final String key, final SimpleGet<T> creator)
	{
		return new GetBy1<T, Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public T getBy(Map<String, Object> a)
			{
				T ret = (T) a.get(key);
				if(null == ret)
				{
					a.put(key, ret = creator.get());
				}
				
				return ret;
			}
		};
	}
	
	public static Collection<String> set(String... val)
	{
		HashSet<String> ret = new HashSet<>();
		for(String v:val)
		{
			ret.add(v);
		}
		return ret;
	}
	
	public static SimplePublish1<Map<String, Object>> download_pages_into_with_local_serial_connection
	(
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get
	)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public void publish(Map<String, Object> env)
			{
				Map<String, byte[]> dst = assertCastType(Map.class, dst_coll_get.getBy(env), POINT);
				Collection<String> src = assertCastType(Collection.class, src_coll_get.getBy(env), POINT);
				
				for(String s:src)
				{
					try
					{
						byte[] data = SpiderTools.download(s, SpiderTools.getExtraHeaders());
						if(null != data)
						{
							dst.put(s, data);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	public static SimplePublish1<Map<String, Object>> download_pages_with_single_proxy
	(
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<ProxyStorage, Map<String, Object>> get_spawnerStorage
	)
	{
		return download_pages_with_proxy_downloader(null, dst_coll_get, src_coll_get, get_spawnerStorage);
	}
	
	public static SimplePublish1<Map<String, Object>> download_pages_with_proxy_downloader
	(
		final @MayNull GetBy2<byte[], Proxy, URL> downloader,
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<ProxyStorage, Map<String, Object>> get_spawnerStorage
	)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public void publish(Map<String, Object> env)
			{
				Map<String, byte[]> dst = assertCastType(Map.class, dst_coll_get.getBy(env), POINT);
				Collection<String> src = assertCastType(Collection.class, src_coll_get.getBy(env), POINT);
				final ProxyStorage spawnerStorage = assertCastType(ProxyStorage.class, get_spawnerStorage.getBy(env), POINT);
				try
				{
					ProxySource tp = spawnerStorage.getAtOffset(0);
					Proxy p = tp.getProxy();
					
					for(String toDownload: src)
					{
						try
						{
							long t0 = System.currentTimeMillis();
							byte[] data = null;
							if(null == downloader)
							{
								data = SpiderTools.download(p, toDownload, SpiderTools.getExtraHeaders());
							}
							else
							{
								data = downloader.getBy(p, new URL(toDownload));
							}
							tryLogFormat(LOG, MEASURE, "Url download \"%s\" took %s ms", toDownload, System.currentTimeMillis()-t0);
							
							if(null != data)
							{
								dst.put(toDownload, data);
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
	}
	
	public static <T> GetBy1<T, Map<String, Object>> wrapConst(final T ret)
	{
		return new GetBy1<T, Map<String,Object>>()
		{
			@Override
			public T getBy(Map<String, Object> a)
			{
				return ret;
			}
		};
	}
	
	/*@FunctionDescription
	(
		functionDescription = "Gyüjteményben lévő URL-ek párhuzamos letöltése proxy-val.",
		parameters = 
		{
			@FunctionVariableDescription(description = "Cél weblaptárt beszerző", mayNull = false, paramName = "destinationPageStorage", type = Object.class),
			@FunctionVariableDescription(description = "URL gyüjtemény beszerző", mayNull = false, paramName = "urlCollection", type = Object.class),
			@FunctionVariableDescription(description = "Proxy tároló beszerző", mayNull = false, paramName = "proxyStorage", type = Object.class),
			@FunctionVariableDescription(description = "Felhasznált proxyk darabszámának beszerzője", mayNull = false, paramName = "usedProxies", type = Object.class),
			@FunctionVariableDescription(description = "Párhuzamosítási szám beszerzése", mayNull = false, paramName = "parallelismDegree", type = Object.class),
			@FunctionVariableDescription(description = "Letöltött lapok kihagyása", mayNull = false, paramName = "skipDownloadedPages", type = Object.class),
		},
		returning = @FunctionVariableDescription(description="Végrehajtó",mayNull=false,paramName="",type=SimplePublish1.class)
	)*/
	public static SimplePublish1<Map<String, Object>> download_pages_into_parallel_with_proxies
	(
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<ProxyStorage, Map<String, Object>> get_spawnerStorage,
		final GetBy1<Integer, Map<String, Object>> get_proxies,
		final GetBy1<Integer, Map<String, Object>> get_concurrency,
		final boolean skip_exists
	)
	{
		return download_pages_into_parallel_with_proxies(null, dst_coll_get, src_coll_get, get_spawnerStorage, get_proxies, get_concurrency, skip_exists);
	}
	
	
	public static SimplePublish1<Map<String, Object>> download_pages_into_parallel_with_proxies
	(
		final @MayNull GetBy2<byte[], Proxy, URL> downloader,
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<ProxyStorage, Map<String, Object>> get_spawnerStorage,
		final GetBy1<Integer, Map<String, Object>> get_proxies,
		final GetBy1<Integer, Map<String, Object>> get_concurrency,
		final boolean skip_exists
	)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public void publish(Map<String, Object> env)
			{
				final Map<String, byte[]> dst = assertCastType(Map.class, dst_coll_get.getBy(env), POINT);
				final ProxyStorage spawnerStorage = assertCastType(ProxyStorage.class, get_spawnerStorage.getBy(env), POINT);
				final int proxies = assertCastType(Integer.class, get_proxies.getBy(env), POINT);
				final int concurrency = assertCastType(Integer.class, get_concurrency.getBy(env), POINT);
				
				Collection<String> src = assertCastType(Collection.class, src_coll_get.getBy(env), POINT);
				
				SpiderTools.downloadPagesIntoParallelWithProxies(downloader, dst, src, spawnerStorage, proxies, concurrency, skip_exists);
			}
		};
	}
	
	public static SimplePublish1<Map<String, Object>> download_pages_into_parallel_with_proxies
	(
		final GetBy1<Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		final GetBy1<Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<ProxyStorage, Map<String, Object>> get_spawnerStorage,
		final GetBy1<Integer, Map<String, Object>> get_proxies,
		final GetBy1<Integer, Map<String, Object>> get_concurrency
	)
	{
		return download_pages_into_parallel_with_proxies(dst_coll_get, src_coll_get, get_spawnerStorage, get_proxies, get_concurrency, false);
	}
	
	public static <T> T assertCastType(Class<?> dst, Object o, PointOfCreationException marker)
	{
		if(null == o)
		{
			throw new RuntimeException("Object is null", marker);
		}
		
		if(!dst.isAssignableFrom(o.getClass()))
		{
			throw new RuntimeException("Object ["+o.getClass()+"]:\""+o+"\" is not instance of "+dst, marker);
		}
		
		return (T) o;
	}
	
	public static String tryParse(String name, byte[] data) throws IOException
	{
		if(name.endsWith(".gz"))
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			GZIPInputStream in = new GZIPInputStream(bais);
			return new String(IOTools.loadAllFromInputStream(in));
		}
		else
		{
			return new String(data);
		}
	}
	
	public static SimplePublish1<Map<String, Object>> collect_site_map_xml_loc
	(
		final GetBy1<? extends Collection<String>, Map<String, Object>> src_coll_get,
		final GetBy1<? extends Map<String, byte[]>, Map<String, Object>> dst_coll_get,
		GetBy1<String, String>... filter
	)
	{
		return new SimplePublish1<Map<String,Object>>()
		{
			PointOfCreationException POINT = new PointOfCreationException();
			
			@Override
			public void publish(Map<String, Object> env)
			{
				final Map<String, byte[]> sitemap = assertCastType(Map.class, dst_coll_get.getBy(env), POINT);
				final Collection<String> dst = assertCastType(Collection.class, src_coll_get.getBy(env), POINT);
				
				final int[] added = new int[1];
				
				for(Entry<String, byte[]> kv:sitemap.entrySet())
				{
					try
					{
						String xml = tryParse(kv.getKey(), kv.getValue());
						selectAllLoc(DocumentTools.parseDocument(xml), new PublisherCollection<Node>()
						{
							@Override
							public boolean add(Node n)
							{
								if(null != n)
								{
									String add = n.getTextContent();
									++added[0];
									for(GetBy1<String, String> f:filter)
									{
										add = f.getBy(add);
										if(null == add)
										{
											return false;
										}
									}
									
									if(null != add)
									{
										dst.add(add);
										return true;
									}
								}
								return false;
							}
						});
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	public static void selectAllLoc(Document doc, Collection<Node> node)
	{
		DocumentTools.selectAll(doc, node, DocumentTools.selectNodesByTagName("loc"));
	}

}
