package eu.javaexperience.web.dispatch.url;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.publish.SimplePublish2;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.Format;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.facility.SiteFacilityTools;

public class AttachFileUrlNode extends URLNode
{
	protected final AbstractFile rootPath;
	protected final boolean listDir;
	
	protected String allowed_other_sites = null;
	
	public void allowOtherSite(String allow)
	{
		this.allowed_other_sites = allow;
	}

	protected boolean followSymlinks = true;
	
	/**
	 * Breaks the root jail so serving files outside the directory is permitted.
	 * This has a secutiry risk, but you max need if you works with soft links
	 * ponits outside the specified directory
	 * */
	public void setFollowSymlinks(boolean follow)
	{
		followSymlinks = follow;
	}
	
	public boolean isFollowingSymlinks()
	{
		return !followSymlinks;
	}
	
	protected static final String renderFileRow(AbstractFile c)
	{
		String name = c.getFileName();
		return renderRow
		(
			"td",
			"<a href=\"./"+name+(c.isDirectory()?"/":"")+"\">"+Format.shortenStringIfLongerThan(name, 20)+(c.isDirectory()?"/":"")+"</a>",
			Format.sqlTimestamp(new Date(c.lastModified())),
			c.isDirectory()?c.listFiles().length+" files":FileTools.toBytesKbMbGbOrTb(c.getSize())
		);
	}
	
	protected static String renderRow(String tag, String... cols)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>");
		for(String col:cols)
		{
			sb.append("<");sb.append(tag);sb.append(">");
				sb.append(col);
			sb.append("</");sb.append(tag);sb.append(">");
		}
		sb.append("</tr>");
		return sb.toString();
	}
	
	public AttachFileUrlNode(String nodeName, AbstractFile localDir, boolean listDir)
	{
		super(nodeName);
		try
		{
			rootPath = localDir.getCanonicalFile();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		this.listDir = listDir;
	}
	
	public AttachFileUrlNode(String nodeName, boolean caseSenitive, AbstractFile localDir,boolean listDir)
	{
		super(nodeName, caseSenitive);
		try
		{
			rootPath = localDir.getCanonicalFile();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		this.listDir = listDir;
	}
	
	public AttachFileUrlNode(String nodeName, Pattern regex, AbstractFile localDir, boolean listDir)
	{
		super(nodeName, regex);
		try
		{
			rootPath = localDir.getCanonicalFile();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		this.listDir = listDir;
	}
	
	public static final GetBy1<String, AbstractFile> DEFAULT_MIME_RECOGNISER = (f)->MIME.recogniseFileExtension(f.getUrl()).mime;
	
	protected GetBy1<String, AbstractFile> mimeRecogniser = DEFAULT_MIME_RECOGNISER;
	
	public GetBy1<String, AbstractFile> getMimeRecogniser()
	{
		return mimeRecogniser;
	}

	public void setMimeRecogniser(GetBy1<String, AbstractFile> mimeRecogniser)
	{
		this.mimeRecogniser = mimeRecogniser;
	}
	
	public static final SimplePublish2<Context, AbstractFile> DEFAULT_FILE_OUTPUT_METHOD = new SimplePublish2<Context, AbstractFile>()
	{
		@Override
		public void publish(Context ctx, AbstractFile f)
		{
			int len = (int) f.getSize();
			ctx.getResponse().setContentLength(len);
			try(InputStream is = f.openRead())
			{
				IOTools.copyStream(is, ctx.getResponse().getOutputStream());
			}
			catch (IOException e)
			{
				Mirror.propagateAnyway(e);
			}
		}
	};
	
	protected SimplePublish2<Context, AbstractFile> fileOutputMethod = DEFAULT_FILE_OUTPUT_METHOD;
	
	public SimplePublish2<Context, AbstractFile> getFileOutputMethod()
	{
		return fileOutputMethod;
	}

	public void setFileOutputMethod(SimplePublish2<Context, AbstractFile> fileOutputMethod)
	{
		this.fileOutputMethod = fileOutputMethod;
	}
	
	@Override
	public boolean dispatch(Context ctx)
	{
		PreparedURL url = ctx.getRequestUrl();
		StringBuilder sb = new StringBuilder();
		StringBuilder webFile = new StringBuilder();
		
		final String rootUrl = rootPath.getUrl();
		sb.append(rootUrl);
		
		String separator = rootPath.getFileSystem().getFileSeparator();
		
		for(int i=0;i<url.getRemainElementNum();i++)
		{
			webFile.append(separator);					
			webFile.append(url.getNextURLElement(i));
		}

		sb.append(webFile);
		
		try
		{
			AbstractFile f = rootPath.getFileSystem().fromUri(sb.toString());
			if(null == f || !f.exists())
			{
				return false;
			}
			
			if(followSymlinks)
			{
				f = f.getCanonicalFile();
			}
			String fstr = f.getUrl();
			if(fstr.indexOf(rootUrl)!=0)
			{
				return false;
			}
			
			//Megvan a file és valid!
			if(null != allowed_other_sites)
			{
				ctx.getResponse().addHeader("Access-Control-Allow-Origin" , allowed_other_sites);
			}
			
			long mod = HttpTools.getWebDate(f.lastModified());
			
			if(SiteFacilityTools.isContentModified(ctx, mod))
			{
				HttpServletResponse resp = ctx.getResponse();
				SiteFacilityTools.setItemLastModified(ctx, mod);
				
				
				if(f.isDirectory() && listDir)
				{
					StringBuilder c = new StringBuilder();
					c.append("<html><head><meta charset=\"utf-8\"><title>");
						c.append("Listing directory: ");
						c.append(webFile.toString());
					c.append("</title></head><body><table>");
					
					sb.append(renderRow("th", "File name", "Last modify", "Size"));

					//Ha ez a root akkor ennek üres Stringnek kell lennie
					AbstractFile[] files = f.listFiles();

					sb.append(renderRow("td", ".", Format.sqlTimestamp(new Date(f.lastModified())), f.listFiles().length+" file(s)"));
					
					if(!rootPath.equals(f))
					{
						AbstractFile parent = f.getParentFile();
						
						if(null != parent)
						{
							int ps = 0;
							AbstractFile[] afs = parent.listFiles();
							if(null != afs)
							{
								ps = afs.length;
							}
							
							sb.append
							(
								renderRow
								(
									"td",
									"<a href=\"../\">../</a>",
									ps+" file(s)"
								)
							);
						}
					}
					
					for(AbstractFile file:files)
					{
						c.append(renderFileRow(file));
					}
					
					SiteFacilityTools.finishWithMimeSend(ctx, MIME.html, c.toString());
				}
				else
				{
					resp.setContentType(mimeRecogniser.getBy(f));
					getFileOutputMethod().publish(ctx, f);
					ctx.finishOperation();
				}
				
				return true;
			}
			else
			{
				SiteFacilityTools.sendItemNotModified(ctx);
			}
		}
		catch(Exception e)
		{
			Mirror.throwSoftOrHardButAnyway(e);
		}
		return true;
	}
}
