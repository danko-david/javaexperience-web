package eu.javaexperience.web.dispatch.url;

import java.util.Iterator;

import eu.javaexperience.semantic.references.MayNotModified;

public interface URLLink
{
	/**
	 * Visszadja azokat a Node-okat amelyek ezen kereszül érhetőek el.
	 * */
	public Iterator<URLLink> iterateOwnedElements();
	
	/**
	 * Vissszaadja ennek a láncszemnek a nevét.
	 * */
	public String getNodeName();
	
	/**
	 * URL elvét követve: visszaadja azt az URL-t amelyen az adott tartalom elérhető
	 * SEO szempontból a kérelemnek csak az ezzel megegyező URL-ekről szabad kiszolgálódnia
	 * az ezt elérő de ezzel karakter pontosan nem egyező URL-eket át kell iránítani ide.
	 * */
	public String getCanonicalURL();
	
	public @MayNotModified URLNode[] childs();
	
	public @MayNotModified URLNode[] parents();
	
	public boolean isDomainNode();
}
