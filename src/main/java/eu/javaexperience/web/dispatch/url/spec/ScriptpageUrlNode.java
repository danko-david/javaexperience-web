package eu.javaexperience.web.dispatch.url.spec;

import java.io.File;
import java.lang.reflect.Method;

import eu.javaexperience.arrays.ArrayTools;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.dispatch.url.AttachDirectoryURLNode;
import eu.javaexperience.web.dispatch.url.FilesInDirectoryConcaterationUrlNode;
import eu.javaexperience.web.dispatch.url.JavaClassURLNode;

/**
 * opening_page
 * 
 * 
 * 
 * */
public abstract class ScriptpageUrlNode extends JavaClassURLNode
{
	@Override protected boolean beforeCall(Context ctx, Method m){return true;}
	@Override protected void afterCall(Context ctx, Method m){}
	@Override protected void backward(Context ctx){}
	
	public abstract String getPath();
	
	protected FilesInDirectoryConcaterationUrlNode preload;
	
	protected JsTriggerDirectory jstrigger;
	
	protected AttachDirectoryURLNode resource;
	
	
	
	public ScriptpageUrlNode
	(
		String preloadDir,
		String jstriggerDir,
		String resourceDirectory
	)
	{
		if(null != preloadDir)
		{
			preload = new FilesInDirectoryConcaterationUrlNode("preload", new File(preloadDir), MIME.javascript.mime);
			this.addChild(preload);
		}
		
		if(null != jstriggerDir)
		{
			jstrigger = new JsTriggerDirectory("jstrigger", new File(jstriggerDir));
			this.addChild(jstrigger);
		}
		
		if(null != resourceDirectory)
		{
			resource = new AttachDirectoryURLNode("repository", resourceDirectory, true);
			this.addChild(resource);
		}
	}
	
	public void setReloadOnRequest(boolean reload)
	{
		if(null != preload)
		{
			preload.setCheckModifiedOnRequest(reload);
		}
		
		if(null != jstrigger)
		{
			jstrigger.setCheckModifiedOnRequest(reload);
		}
	}
	
	/***/
	protected abstract void renderPageWith
	(
		Context ctx,
		String[] id_triggers,
		String[] scripts
	);
	
	@Override
	protected boolean endpoint(Context ctx)
	{
		String[] scripts = ctx.getRequest().getParameterValues("script");
		
		if(null == scripts || 0 == scripts.length || (1 == scripts.length && 0 == scripts[0].length()))
		{
			scripts = null;
		}
		
		if(null == scripts || 0 == scripts.length)
		{
			scripts = new String[]{"js_trigger_opening_page"};
		}
		
		scripts = ArrayTools.arrayAppend("js_trigger_always", scripts);
		
		String path = getPath();
		
		renderPageWith
		(
			ctx,
			scripts,
			ArrayTools.withoutNulls(new String[]
			{
				null == preload?null:path+"/preload/"+preload.salt.getSalt(),
				null == jstrigger?null:path+"/jstrigger/"+jstrigger.salt.getSalt()
			})
		);
		
		return true;
	}

	@Override
	protected abstract boolean access(Context ctx);
}
