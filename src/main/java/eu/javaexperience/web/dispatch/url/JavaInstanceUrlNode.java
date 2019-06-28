package eu.javaexperience.web.dispatch.url;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import eu.javaexperience.collection.map.SmallMap;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.web.Context;

public abstract class JavaInstanceUrlNode extends URLNode
{
	protected final Map<String, Method> meths = new SmallMap<>();
	
	public boolean invoke(String name, Context arg) throws Throwable
	{
		try
		{
			Method m = meths.get(name);
			if(m != null)
			{
				m.invoke(this, arg);
				return true;
			}
			return false;
		}
		catch(InvocationTargetException tar)
		{
			throw tar.getCause();
		}
	}
	
	public Method getMethodByName(String name)
	{
		return meths.get(name);
	}
	
	public JavaInstanceUrlNode(String nodeName)
	{
		super(nodeName, true);
	}

	public JavaInstanceUrlNode()
	{
		super("");
		nodeName = getClass().getSimpleName();
		pattern = MultiUrlNodePattern.simpleCaseSensitiveString(this.getClass().getSimpleName());
		
		loadFunction();
	}
	
	protected void loadFunction()
	{
		for(Method m:Mirror.getClassData(this.getCanonicalURL()).getAllMethods())
		{
			if(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
			{
				Class<?>[] params = m.getParameterTypes();
				if(1 == params.length && Context.class.isAssignableFrom(params[0]))
				{
					meths.put(m.getName(), m);
				}
			}
		}
	}
	
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
				Method m = getMethodByName(name);
				if(m != null)
				{
					if(!beforeCall(ctx, m))
					{
						return false;
					}
					m.invoke(this, ctx);
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
			Mirror.propagateAnyway(e);
			return true;//have a nice day
		}
	}
}