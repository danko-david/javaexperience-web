package eu.javaexperience.web.dispatch.url.spec.compile;

import java.io.File;
import java.io.IOException;

import eu.javaexperience.io.IOTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.HttpTools;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.dispatch.url.CachedSaltedContentUrlNode;

public class CompilerUrlNode extends CachedSaltedContentUrlNode
{
	protected final WebCompiler compiler;
	protected boolean production;
	protected File destinationFile;
	
	public CompilerUrlNode(String name, File destinationFile, WebCompiler compiler, boolean production)
	{
		super(name, MIME.javascript.mime);
		this.compiler = compiler;
		this.production = production;
		this.destinationFile = destinationFile;
		this.compiler.setProduction(production);
	}
	
	public void init()
	{
		refresh();
	}
	
	public synchronized CachedSaltedContentUrlNode refresh()
	{
		callbackInvalidate();
		if(!production)
		{
			WebCompiler c = getCompiler();
			if(0 == destinationFile.lastModified() || c.getSourcesLastModification() != destinationFile.lastModified())
			{
				c.compile(true);
				c.emitMergedOutput(destinationFile.toString());
			}
		}
		lastModified = determineLastModified();
		data = transform(loadContent());
		salt.setName(HttpTools.toCacheSaltHexa(lastModified));
		callbackRefreshed();
		return this;
	}
	
	public synchronized boolean checkModificationRefreshIfNeeded()
	{
		if(!production)
		{
			lastModified = destinationFile.lastModified();
			long lastMod = determineLastModified();
			if(lastModified < lastMod)
			{
				refresh();
				return true;
			}
		}
		return false;
	}
	
	public WebCompiler getCompiler()
	{
		return compiler;
	}
	
	protected void assembleFile(File f) throws IOException
	{
		WebCompiler c = getCompiler();
		//c.cleanupDir();
		c.compile(true);
		c.emitMergedOutput(f.toString());
	}
	
	public void callbackInvalidate()
	{
		if(!production)
		{
			//destinationFile.delete();
		}
	};
	
	@Override
	public byte[] loadContent()
	{
		if(!destinationFile.exists())
		{
			try
			{
				assembleFile(destinationFile);
			}
			catch (IOException e)
			{
				Mirror.propagateAnyway(e);
				return null;
			}
		}
		
		try
		{
			return IOTools.loadFileContent(destinationFile.toString());
		}
		catch (IOException e)
		{
			Mirror.propagateAnyway(e);
			return null;
		}
	}
	
	@Override
	public long determineLastModified()
	{
		if(production)
		{
			return destinationFile.lastModified();
		}
		else
		{
			return getCompiler().getSourcesLastModification();
		}
	}
}
