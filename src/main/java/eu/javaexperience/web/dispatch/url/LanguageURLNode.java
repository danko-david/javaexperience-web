package eu.javaexperience.web.dispatch.url;

import eu.javaexperience.asserts.AssertArgument;
import eu.javaexperience.semantic.references.MayNotNull;
import eu.javaexperience.semantic.status.NeedToTest;
import eu.javaexperience.web.Context;

/**
 * After request goes trough this node, proper language set in ThreadLocal
 * clientLanguage.
 * */
@NeedToTest
public class LanguageURLNode extends URLNode
{
	protected final AvailableLanguage default_language;
	protected final AvailableLanguage[] other_languages;
	
	public LanguageURLNode(AvailableLanguage default_, AvailableLanguage... otherLanguages)
	{
		super("");
		AssertArgument.assertNotNull(default_language = default_, "default language");
		AssertArgument.assertNotNull(other_languages = otherLanguages, "other languages");
		
		pattern  = new URLNodePattern()
		{
			public boolean match(String str)
			{
				if(default_language.isoName.equals(str))
					return true;
				
				for(AvailableLanguage lang:other_languages)
					if(lang.isoName.equals(str))
						return true;
				
				return false;
			}
			
			@Override
			public boolean match(Context ctx)
			{
				return match(ctx.getRequestUrl().getCurrentURLElement());
			}
		};
	}

	public String getNodeName()
	{
		return clientLanguage.get().getISOName();
	}
	
	public static enum AvailableLanguage
	{
		en("en"),
		hu("hu"),
		
		;
		
		protected final String isoName;
		
		public static @MayNotNull AvailableLanguage getByISO(String iso)
		{
			for(AvailableLanguage l:values())
				if(l.isoName.equals(iso))
					return l;
		
			return en;
		}
		
		public final String getISOName()
		{
			return isoName;
		}
		
		private AvailableLanguage(String isoName)
		{
			this.isoName = isoName;
		}
		
	}
	
	public AvailableLanguage getRequestCurrentLanguage()
	{
		return clientLanguage.get();
	}
	
	public ThreadLocal<AvailableLanguage> clientLanguage = new ThreadLocal<AvailableLanguage>()
	{
		@Override
		protected AvailableLanguage initialValue()
		{
			return AvailableLanguage.en;
		}
	};

	@Override
	public boolean dispatch(Context ctx)
	{
		clientLanguage.set(AvailableLanguage.getByISO(ctx.getRequestUrl().getCurrentURLElement()));
		//TODO redirect canonical, or read client's language then redirect 
		return tryDispatchSubNodes(ctx);
	}
}
