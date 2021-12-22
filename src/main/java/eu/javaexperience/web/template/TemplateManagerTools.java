package eu.javaexperience.web.template;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.javaexperience.collection.PublisherCollection;
import eu.javaexperience.collection.map.OneShotMap;
import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.FileSystemTools;
import eu.javaexperience.file.fs.os.OsFile;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.regex.RegexTools;

public class TemplateManagerTools
{
	public static File toFile(File dir, String name, String fullExtension)
	{
		String[] elems = RegexTools.SLASHES.split(name);
		StringBuilder sb = new StringBuilder();
		sb.append(dir.toString());
		for(int i=0;i<elems.length-1;++i)
		{
			sb.append("/");
			sb.append(elems[i]);
		}
		
		sb.append("/");
		sb.append(elems[elems.length-1]);
		sb.append(".");
		sb.append(fullExtension);
		return new File(sb.toString());
	}
	
	public static AbstractFile toFile(AbstractFile dir, String name, String fullExtension)
	{
		String[] elems = RegexTools.SLASHES.split(name);
		StringBuilder sb = new StringBuilder();
		sb.append(dir.getUrl());
		for(int i=0;i<elems.length-1;++i)
		{
			sb.append("/");
			sb.append(elems[i]);
		}
		
		sb.append("/");
		sb.append(elems[elems.length-1]);
		sb.append(".");
		sb.append(fullExtension);
		return dir.getFileSystem().fromUri(sb.toString());
	}
	
	public static <T> void parseAndPrepareViewInto
	(
		Map<String, T> destination,
		GetBy1<T, String> parser,
		String viewName,
		String content
	)
		throws IOException
	{
		destination.put(viewName, parser.getBy(content));
	}
	
	public static <T> Map<String, T> loadViewsFromDirRecursiveRaw
	(
		final Map<String, T> ret,
		final GetBy1<T, String> parser,
		File dir,
		final String extension
	)
	{
		return loadViewsFromDirRecursiveRaw(ret, parser, new OsFile(dir), extension);
		/*final int start = Regex.slashesLinuxWin.split(dir.toString()).length;
		
		FileTools.find(dir, new PublisherCollection<File>()
		{
			@Override
			public boolean add(File file)
			{
				Pattern p = Pattern.compile("\\/(?<teil>[^/]+)\\."+extension+"$");
				Matcher m = p.matcher(file.toString());
				if(m.find())
				{
					String t = m.group("teil");
					if(null != t)
					{
						String[] paths = Regex.slashesLinuxWin.split(file.toString());
						StringBuilder tn = new StringBuilder();
						for(int i=start;i<paths.length-1;++i)
						{
							if(tn.length() > 0)
							{
								tn.append("/");
							}
							tn.append(paths[i]);
						}
						
						if(tn.length() > 0)
						{
							tn.append("/");
						}
						tn.append(t);
						
						try
						{
							parseAndPrepareViewInto(ret, parser, tn.toString(), IOTools.getFileContents(file));
						}
						catch (Exception e)
						{
							System.err.println(file);
							e.printStackTrace();
						}
					}
				}
				
				return true;
			}
		});
		
		return ret;*/
	}
	
	public static <T> Map<String, T> loadViewsFromDirRecursiveRaw
	(
		final Map<String, T> ret,
		final GetBy1<T, String> parser,
		AbstractFile dir,
		final String extension
	)
	{
		final int start = RegexTools.SLASHES_LINUX_WINDOWS.split(dir.getUrl()).length;
		
		FileSystemTools.find(dir, new PublisherCollection<AbstractFile>()
		{
			@Override
			public boolean add(AbstractFile file)
			{
				Pattern p = Pattern.compile("\\/(?<teil>[^/]+)\\."+extension+"$");
				Matcher m = p.matcher(file.toString());
				if(m.find())
				{
					String t = m.group("teil");
					if(null != t)
					{
						String[] paths = RegexTools.SLASHES_LINUX_WINDOWS.split(file.toString());
						StringBuilder tn = new StringBuilder();
						for(int i=start;i<paths.length-1;++i)
						{
							if(tn.length() > 0)
							{
								tn.append("/");
							}
							tn.append(paths[i]);
						}
						
						if(tn.length() > 0)
						{
							tn.append("/");
						}
						tn.append(t);
						
						try
						{
							parseAndPrepareViewInto(ret, parser, tn.toString(), IOTools.getFileContents(file));
						}
						catch (Exception e)
						{
							System.err.println(file);
							e.printStackTrace();
						}
					}
				}
				
				return true;
			}
		});
		
		return ret;
	}
	
	public static <T> Map<String, T> loadRecursiveWithDevelopementReload
	(
		final File dir,
		final GetBy1<T, String> parser,
		final String extension
	)
	{
		return loadRecursiveWithDevelopementReload(new OsFile(dir), parser, extension);
		/*final Map<String, T> views = loadViewsFromDirRecursiveRaw(new ConcurrentHashMap<String, T>(), parser, dir, extension);
		
		final HashMap<File, Long> mods = new HashMap<>();
		
		OneShotMap<String, T> rt = new OneShotMap<String, T>(null, null)
		{
			public T get(Object key)
			{
				File f = toFile(dir, key.toString(), extension);
				Long l = mods.get(f);
				long mod = f.lastModified();
				if(null == l || l < mod)
				{
					try
					{
						//reloading
						parseAndPrepareViewInto(views, parser, key.toString(), IOTools.getFileContents(f));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					mods.put(f, mod);
				}
				
				return views.get(key);
			}
		};
		
		return rt;*/
	}
	
	public static <T> Map<String, T> loadRecursiveWithDevelopementReload
	(
		final AbstractFile dir,
		final GetBy1<T, String> parser,
		final String extension
	)
	{
		final Map<String, T> views = loadViewsFromDirRecursiveRaw(new ConcurrentHashMap<String, T>(), parser, dir, extension);
		
		final HashMap<AbstractFile, Long> mods = new HashMap<>();
		
		OneShotMap<String, T> rt = new OneShotMap<String, T>(null, null)
		{
			public T get(Object key)
			{
				AbstractFile f = toFile(dir, key.toString(), extension);
				Long l = mods.get(f);
				long mod = f.lastModified();
				if(null == l || l < mod)
				{
					try
					{
						//reloading
						parseAndPrepareViewInto(views, parser, key.toString(), IOTools.getFileContents(f));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					mods.put(f, mod);
				}
				
				return views.get(key);
			}
		};
		
		return rt;
	}
}
