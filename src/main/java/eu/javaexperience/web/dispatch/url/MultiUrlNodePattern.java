package eu.javaexperience.web.dispatch.url;

import java.util.regex.Pattern;

import eu.javaexperience.semantic.references.MayNotNull;
import eu.javaexperience.web.Context;


public class MultiUrlNodePattern implements URLNodePattern
{
	protected String pattern;
	protected int opts = 0x0;
	@MayNotNull protected PatternMode mode;
	protected String originName;
	
	protected Pattern pat;
	
	protected String[] strs;
	
	private MultiUrlNodePattern()
	{}
	
	public boolean match(Context ctx)
	{
		return match(ctx.getRequestUrl().getCurrentURLElement());
	}
	
	public boolean match(String str)
	{
		switch (mode)
		{
		case always:	return true;

		case regex:		return pat.matcher(str).find();
			
		case string: 	return isCaseInsensitive()?pattern.equalsIgnoreCase(str):pattern.equals(str);

		
		case stringArray:
			if(isCaseInsensitive())
				for(String s:strs)
					if(s.equalsIgnoreCase(str))
						return true;
					else
						continue;
			else
				for(String s:strs)
					if(s.equals(str))
						return true;
					else
						continue;
			
			return false;
		}

		throw new RuntimeException("URLNodePattern undefined mode case: "+mode);
	}
	
	public boolean isCaseInsensitive()
	{
		return Pattern.CASE_INSENSITIVE == (opts | Pattern.CASE_INSENSITIVE);
	}
	
	public static MultiUrlNodePattern simpleCaseSensitiveString(String str)
	{
		MultiUrlNodePattern pat = new MultiUrlNodePattern();
		pat.originName = str;
		pat.mode = PatternMode.string;
		
		pat.opts = 0x0;
		pat.pattern = str;
		return pat;
	}

	public static MultiUrlNodePattern simpleCaseInsensitiveString(String str)
	{
		MultiUrlNodePattern pat = new MultiUrlNodePattern();
		pat.mode = PatternMode.string;
		
		pat.opts = Pattern.CASE_INSENSITIVE;
		pat.pattern = str;
		return pat;
	}

	public void setOriginName(String name)
	{
		this.originName = name;
	}
	
	public String getOriginName()
	{
		return originName;
	}
	
	public static MultiUrlNodePattern fromRegex(Pattern p)
	{
		MultiUrlNodePattern ret = new MultiUrlNodePattern();
		ret.mode = PatternMode.regex;
		ret.pattern = p.pattern();
		ret.opts = p.flags();
		ret.pat = p;
		
		return ret;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof MultiUrlNodePattern))
			return false;
		
		return equals((MultiUrlNodePattern)o);
	}
	
	/**
	 * Tulterhelt verzió, ez került meghivásra ha (o instanceof URLNodePattern) && equals(((URLNodePattern)o));
	 * */
	public boolean equals(MultiUrlNodePattern pat)
	{
		if(mode != pat.mode)
			return false;
		
		switch (mode)
		{
		case always:
				return true;
		case regex:
				return pat.equals(pat.pat);

		case string:
				return isCaseInsensitive()?pattern.equalsIgnoreCase(pat.pattern):pattern.equals(pat.pattern);
		
		case stringArray:
			if(isCaseInsensitive())
			{
				kint:for(String s:strs)
				{
					for(String p:pat.strs)
						if(s.equalsIgnoreCase(p))
							continue kint;

					return false;
				}
				return true;
			
			}
			else
			{
				kint:for(String s:strs)
				{
					for(String p:pat.strs)
						if(s.equals(p))
							continue kint;

					return false;
				}
				return true;
			}			
		}
		
		return false;
	}
}
