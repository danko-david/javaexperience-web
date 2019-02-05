package eu.javaexperience.web;

import java.util.regex.Pattern;

import eu.javaexperience.regex.RegexTools;
import eu.javaexperience.text.StringTools;

public class WebTools
{
	public static final String EMAIL_REGEX = 
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
	
	public static boolean isValidEmail(String email)
	{
		return EMAIL_PATTERN.matcher(email).find();
	}
	
	protected static Pattern SELECT_NON_SEO_CHARS = Pattern.compile("[^a-z0-9-]");
	protected static Pattern SELECT_SEO_CHARS_FOR_DASH = Pattern.compile("[- _]");
	
	public static String asSeoName(String str)
	{
		str = str.toLowerCase();
		str = StringTools.deAccent(str);
		str = RegexTools.MATCH_WHITESPACES.matcher(str).replaceAll(" ");
		str = SELECT_SEO_CHARS_FOR_DASH.matcher(str).replaceAll("-");
		str = SELECT_NON_SEO_CHARS.matcher(str).replaceAll("");
		
		return str;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println(asSeoName("16 db ärvíztűrő_tüköR-FŰRŐGép"));
	}
}
