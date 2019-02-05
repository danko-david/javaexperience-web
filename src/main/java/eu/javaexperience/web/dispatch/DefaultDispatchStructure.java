package eu.javaexperience.web.dispatch;

import eu.javaexperience.patterns.behavioral.cor.CorChain;
import eu.javaexperience.patterns.behavioral.cor.CorDispatcher;
import eu.javaexperience.patterns.behavioral.cor.link.CorChainLink;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.dispatch.url.PreparedURL;

public class DefaultDispatchStructure
{
	/**
	 * Chains:
	 * root:
	 * 
	 * 	rewrite
	 * 	redirect
	 * 	static
	 * 	system
	 * 	ignore_domain
	 * 	custom
	 * 
	 * 
	 * 
	 * 
	 * */
	
	protected static final String[] CHAIN_NAMES = new String[]
	{
		"modify",
		"pre",
		
		"static",
		"system",
		"app",
		"last"
	};
	
	protected final CorDispatcher<Context> CHAINS = new CorDispatcher<>();
	{
		CorChain<Context> chain = null;
		for(String s:CHAIN_NAMES)
		{
			CorChain<Context> add = new CorChain<>(s);
			CHAINS.addChain(add);
			if(null != chain)
			{
				chain.setDefaultAction(add);
			}
			else
			{
				CHAINS.setRootChain(add);
			}
			
			chain = add;
		}
	}
	
	protected boolean pathDispatchAdded = false;
	protected boolean usePathDispatch = false;
	
	public void setUsePathDispatch(boolean use)
	{
		if(use && !pathDispatchAdded)
		{
			pathDispatchAdded = true;
			CHAINS.getChainByName("modify").addLinkAsFirst(new CorChainLink<Context>()
			{
				@Override
				public boolean dispatch(Context ctx)
				{
					if(usePathDispatch)
					{
						PreparedURL url = ctx.getRequestUrl();
						url.setPathPointer(url.getDomainElements());
					}
					return false;
				}
			});
		}
		
		this.usePathDispatch = use;
	}

	public boolean isUsePathDispatch()
	{
		return usePathDispatch;
	}
	
	public CorDispatcher<Context> getChains()
	{
		return CHAINS;
	}
}
