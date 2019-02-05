package eu.javaexperience.web.dispatch.url;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.javaexperience.reflect.CallPublic1ArgStaticOrGivenMethod;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;

public abstract class JavaClassURLNode extends URLNode
{
	protected final CallPublic1ArgStaticOrGivenMethod<? extends Context> handle = new CallPublic1ArgStaticOrGivenMethod<>(this.getClass(), null);
	
	public JavaClassURLNode(String nodeName)
	{
		super(nodeName, true);
	}

	public JavaClassURLNode()
	{
		super("");
		nodeName = getClass().getSimpleName();
		pattern = MultiUrlNodePattern.simpleCaseSensitiveString(this.getClass().getSimpleName());
	}
	
	//TODO berforeAccessNode
	
	/**
	 * Method called before the actual dispatcher invoked and it pass also
	 * the method which will be called, so it's come with the possibility to
	 * annotate your methods and do some extra operation on the context.
	 * For example in some code i use this to set the view, or 
	 * pass/deny logged in users.
	 * 
	 * This method not called before endpoint dispatch. 
	 * 
	 * It can reject the dispatching if returning false.
	 * if returns to dispatching to the founded method will be applied
	 * */
	protected abstract boolean beforeCall(Context ctx, Method m);

	protected abstract void afterCall(Context ctx, Method m);
	
	protected abstract void backward(Context ctx);
	
	protected abstract boolean endpoint(Context ctx);
	
	protected abstract boolean access(Context ctx);

	@Override
	public boolean dispatch(Context ctx)
	{
		if(!access(ctx))
		{
			return false;
		}
		
		PreparedURL url = ctx.getRequestUrl();
		//at this time the current path pointer is points on the class name
		try
		{
			if(!url.isEndOfPath())
			{
				String name = url.getCurrentURLElement();
				Method m = handle.getMethodByName(name);
				if(m != null)
				{
					if(!beforeCall(ctx, m))
					{
						return false;
					}
					m.invoke(null, ctx);
					afterCall(ctx, m);
				}
				
				boolean sub = tryDispatchSubNodes(ctx);
				backward(ctx);
				return sub;
			}
			else
			{
				return endpoint(ctx);
			}
		}
		catch (Throwable e)
		{
			if(e instanceof InvocationTargetException)
			{
				Mirror.throwSoftOrHardButAnyway(e.getCause());
			}
			else
			{
				Mirror.throwSoftOrHardButAnyway(e);
			}
			return true;//have a nice day
		}
	}
}