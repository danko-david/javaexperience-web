package eu.javaexperience.web.sitemap;

import java.util.Date;
import java.util.List;

import eu.javaexperience.semantic.references.MayNotNull;
import eu.javaexperience.semantic.references.MayNull;

/**
	<url>
		<loc>http://www.example.com/catalog?item=73&amp;desc=vacation_new_zealand</loc>
		<lastmod>2004-12-23</lastmod>
		<changefreq>weekly</changefreq>
		<priority>0.3</priority>
	</url>
 * */
public class SitemapEntry
{
	public @MayNotNull String url;
	public @MayNull Date lastModify;
	
	public List<SitemapEntry> images;
}
