package eu.javaexperience.web.dom.build;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CPathExpression
{
	/*

	<> {} [] () '' ||


	doctype >
	{

			head
		|
			body#test.cls[data-id=sabz]'szekcio'
				>
				{
						div#tartalom.atlatszo[data-swid=asdasfadf]
					|
						div#foot.atlatszo
				}
	}

	*/

	//http://stackoverflow.com/questions/11859442/how-to-match-string-in-quotes-using-regex

	private static String txt = "\\\"(?<txt>[^\"]*)\\\"";

	//static String txt = "(?<txt>(?<=\\))[^)]*(?=\\)))";
	
	static String escapedString(String groupName)
	{
		return "[^\"'\\]]+";
	}

	public static final String example = "html > { head > meta.a.b | body > {div#tartalom.fullwidth[data-swgui='valami'][data-sw=\"v\"] > h1\"Helló \"  > h2\"világ.\"| input[disabled] | a[href]\"Valami\" | div#foot.transparent[data_swgui=v mi]}}";

	static final Pattern classesInTag = Pattern.compile("(\\.(?<cls>[\\w-_]+))");
	
	//static final Pattern attrInTag = Pattern.compile("(\\[\\s*(?<quok>['\"]?)(?<k>[\\w-]+)(\\k<quok>['\"]?)\\s*(=\\s*(?<quo>['\"]?)(?<v>[\\w-]+)(\\k<quo>)\\])?)");
	static final Pattern attrInTag = Pattern.compile("(\\[\\s*(?<quok>['\"]?)(?<k>"+escapedString("quok")+")(\\k<quok>)\\s*(=\\s*(?<quo>['\"]?)(?<v>"+escapedString("quo")+")(\\k<quo>))?\\])");
	
	static final Pattern tag = Pattern.compile
	(
		//"\\s*(?<name>\\w+)(#(?<id>\\w+))?(('(?<szekc>\\w+)')*("+txt+")*(\\.\\w+)*(\\[\\s*[\\w-]+\\s*=\\s*(?<quo>['\"])?[\\w(\\[)(\\])]+(\\k<quo>)\\])*)*\\s*"
		"\\s*(?<name>\\w+)(#(?<id>\\w+))?(("+txt+")|("+classesInTag.toString()+")|("+attrInTag.toString()+"))*\\s*"
	);
	
	public static <T> T parseSingleElement(CPathBuilder<T> builder, String element)
	{
		//System.out.println("Elem to parse: "+element);
		T elem = null;
		Matcher t = tag.matcher(element);

		T ref = null;

		String disz = null;

		if(t.find())
		{
			disz = t.group();
			String constr = t.group("name");
			if(constr == null)
				return null;
			
			if(elem == null)
			{
				ref = elem = builder.createTag(constr);
			}
			else
			{
				builder.addChild(elem, ref = builder.createTag(constr));
			}
		}

		if(ref == null)
			return null;

		{
			String id = t.group("id");
			if(null != id)
			{
				builder.setId(ref, id);
			}
		}
		
		{
			String txt = t.group("txt");
			if(null != txt)
			{
				builder.setText(ref, txt);
			}
		}
		
		Matcher dat = classesInTag.matcher(disz);
		while(dat.find())
		{
			String cls = dat.group("cls");
			if(null != cls)
			{
				builder.addClass(ref, cls);
			}
		}

		dat = attrInTag.matcher(disz);
		while(dat.find())
		{
			String k = dat.group("k");
			String v = dat.group("v");
			if(null != k)
			{
				builder.addAttribute(ref, k, v);
			}
		}

		return elem;
	}

	protected static final Pattern FULLTAG = Pattern.compile("(?<tag>"+tag.toString()+")|(\\s*(?<chr>[{}|>])\\s*)");
	
	public static <T> T parse(CPathBuilder<T> builder, String str)
	{
		Matcher m = FULLTAG.matcher(str);

		T first = null;

		Stack<T> stack = new Stack<T>();

		boolean multi = false;
		boolean chld = false;
		boolean shib = false;
		boolean pending = false;

		String tag = null;
		String chr = null;
		try
		{
			while(m.find())
			{
				if((tag = m.group("tag")) != null)
				{
					if(chld && !multi)
						pending = true;
					else if(!chld && pending)
					{
						stack.pop();
						pending = false;
					}
					else
						pending = false;

					if(first == null)
						stack.add(first = parseSingleElement(builder, tag));

					if(shib)
					{
						stack.pop();
						stack.add(parseSingleElement(builder, tag));
						builder.addChild(stack.get(stack.size()-2), stack.lastElement());
					}
					else if(chld)
					{
						stack.add(parseSingleElement(builder, tag));
						builder.addChild(stack.get(stack.size()-2), stack.lastElement());
					}
				}
				else if((chr = m.group("chr")) != null)
				{
					if(chr.equals("{"))
					{
						multi = true;
					}
					else if(chr.equals("}"))
					{
						if(!multi)
							stack.pop();
						multi = false;
						chld = false;
						shib = false;
					}
					else if(chr.equals(">"))
					{
						multi = false;
						chld = true;
						shib = false;
					}
					else if(chr.equals("|"))
					{
						chld = false;
						shib = true;
					}
				}
			}
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Illegal in range: "+m.start()+"-"+m.end()+".", e);
		}

		return first;
	}


	public static void main(String args[])
	{
		/*
		System.out.println(example);
		System.out.println(parse(JsoupEditTools.CPATH_BUILDER, example));
		System.out.println(parse(JsoupEditTools.CPATH_BUILDER, "a[href=\"/webedit?file=.GET\"]\"Föoldal szerkesztése\""));
		System.out.println(parse(JsoupEditTools.CPATH_BUILDER, "a[href=\"/webedit?file=.GET\"]\"Szerk\""));
		System.out.println(parse(JsoupEditTools.CPATH_BUILDER, "a[href=\"webedit\"]\"Szerk\""));
		/**/
		//System.out.println(parse(JsoupEditTools.CPATH_BUILDER, "a.btn.btn-primary[href=\"/webedit?file=.GET\"][target=\"_blank\"]\"Föoldal szerkesztése\""));
	}
}
