package eu.javaexperience.web.sitemap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import eu.javaexperience.text.Format.DateFormatParseUnit;

public class SitemapGeneratorTools
{
	private static final byte[] C_SITEMAP = "\n\t<sitemap>\n\t".getBytes();
	private static final byte[] C_SITEMAP_END = "\n\t</sitemap>\n".getBytes();
	
	public static void generateSiteMapIndex(File dst_file, Collection<SitemapEntry> entries, boolean gz) throws IOException
	{
		OutputStream os = new FileOutputStream(dst_file);
		
		try
		{
			if(gz)
			{
				os = new GZIPOutputStream(os);
			}
			
			os.write(SITEMAP_INDEX_HEADER);
			
			for(SitemapEntry ent:entries)
			{
				os.write(C_SITEMAP);
				
					os.write(C_LOC);
					os.write(ent.url.getBytes());
					os.write(C_LOC_END);
					
					if(null != ent.lastModify)
					{
						os.write(C_LASTMOD);
						os.write(SITEMAP_DATE_FORMAT.format(ent.lastModify).getBytes());
						os.write(C_LASTMOD_END);
					}
					
				os.write(C_SITEMAP_END);
			}
			
			os.write(SITEMAP_INDEX_FOOTER);
			
			//loc és last mod
			
		}
		finally
		{
			os.flush();
			os.close();
		}
	}
	
	
	private static final byte[] SITEMAP_INDEX_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n".getBytes();
	
	private static final byte[] SITEMAP_INDEX_FOOTER = "</sitemapindex>".getBytes();
	
	private static final byte[] C_URL = "\t<url>\n".getBytes();
	private static final byte[] C_URL_END = "\t</url>\n".getBytes();

	private static final byte[] C_LOC = "\t\t<loc>".getBytes();
	private static final byte[] C_LOC_END = "</loc>\n".getBytes();

	private static final byte[] C_LASTMOD = "\t\t<lastmod>".getBytes();
	private static final byte[] C_LASTMOD_END = "</lastmod>\n".getBytes();
	private static final byte[] C_IMAGE = "\t\t<image:image>\n".getBytes();
	private static final byte[] C_IMAGE_END = "\t\t</image:image>\n".getBytes();
	
	private static final byte[] C_IMAGE_LOC = "\t\t\t<image:loc>".getBytes();
	private static final byte[] C_IMAGE_LOC_END = "</image:loc>\n".getBytes();
	
	/*
	 * TODO google dismiss however others accetps this, try this:
	 * https://github.com/dfabulich/sitemapgen4j/blob/master/src/main/java/com/redfin/sitemapgenerator/W3CDateFormat.java
	 *	MILLISECOND("yyyy-MM-dd'T'HH:mm:ss.SSSZ", true),
		SECOND("yyyy-MM-dd'T'HH:mm:ssZ", true),
		MINUTE("yyyy-MM-dd'T'HH:mmZ", true),
		DAY("yyyy-MM-dd", false),
		MONTH("yyyy-MM", false),
		YEAR("yyyy", false),
	 */
	public static final DateFormatParseUnit SITEMAP_DATE_FORMAT = new DateFormatParseUnit("yyyy-MM-dd'T'HH:mm:ssZ");  
	
	protected static void writeEntry(OutputStream os, SitemapEntry entry) throws IOException
	{
		os.write(C_URL);
			os.write(C_LOC);
			os.write(entry.url.getBytes());
			os.write(C_LOC_END);
		
			if(null != entry.lastModify)
			{
				os.write(C_LASTMOD);
				os.write(SITEMAP_DATE_FORMAT.format(entry.lastModify).getBytes());
				os.write(C_LASTMOD_END);
			}

			if(null != entry.images && !entry.images.isEmpty())
			{
				os.write(C_IMAGE);
				for(SitemapEntry img:entry.images)
				{
					os.write(C_IMAGE_LOC);
					os.write((img.url).getBytes());
					os.write(C_IMAGE_LOC_END);
				}
				os.write(C_IMAGE_END);
			}
		
		os.write(C_URL_END);
	}
	
	protected static void writeHeader(OutputStream os) throws IOException
	{
		os.write
		((
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<urlset\n" +
				"\txmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
				"\txmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\"\n" +
				"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"\txsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n"
		).getBytes());
	}
	
	protected static void writeFooter(OutputStream os) throws IOException
	{
		os.write("</urlset>".getBytes());
	}
	
	private static final Set<PosixFilePermission> permission = new HashSet<PosixFilePermission>()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add(PosixFilePermission.OWNER_READ);
			add(PosixFilePermission.OWNER_WRITE);
			
			add(PosixFilePermission.GROUP_READ);
			
			add(PosixFilePermission.OTHERS_READ);
		}
	};
/*	TODO generic regenarator (always regenerate the last)
	public static void main(String[] args) throws Throwable
	{
		//van kategória map?
		//van az újragenerálását kérő állomány?
		
		{
			File catMap = new File(SITEMAP_PATH+CATEGORY_MAP_NAME);
			File reqRegenCatmap = new File(SITEMAP_PATH+"req_regen_sitemap_categories");
			
			if(!catMap.exists())
			{
				generateCategorySiteMap(catMap, true);
			}
			else if(reqRegenCatmap.exists())
			{
				File target = tmpFile();
				generateCategorySiteMap(target, true);
				Files.move(target.toPath(), catMap.toPath(), StandardCopyOption.REPLACE_EXISTING);
				reqRegenCatmap.delete();
			}
		}
		
		//végigmegyünk, ami nem létezik legeneráljuk, különben megnézzük melyik a legrégebbi és 
		//azt generáljuk újra

		int min_index = -1;
		long min_date = Long.MAX_VALUE;
		
		for(int i=0;i < SITEMAP_SET_PART_COUNT;++i)
		{
			File f = new File(SITEMAP_PATH+ARTICLE_MAP_PREFIX+i+ARTICLE_MAP_POSTFIX);
			if(!f.exists())
			{
				generateArticleSiteMapPart(i, f, true);
			}
			else
			{
				long t = f.lastModified();
				if(t < min_date)
				{
					min_index = i;
					min_date = t;
				}
			}
		}

		if(min_index > -1)
		{
			File arts = new File(SITEMAP_PATH+ARTICLE_MAP_PREFIX+min_index+ARTICLE_MAP_POSTFIX);
			//újrageneráljuk a legrégebbi részt.
			File target = tmpFile();
			generateArticleSiteMapPart(min_index, target, true);
			Files.move(target.toPath(), arts.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		
		//a végén a sitemapindexet újrageneráljuk
		{
			File index = new File(SITEMAP_PATH+SITEMAP_INDEX_NAME);
			//újrageneráljuk a legrégebbi részt.
			File target = tmpFile();
			generateSiteMapIndex(target, true, true);
			Files.move(target.toPath(), index.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	*/

	public static void generateSiteMapUrlSet(File dst_file, Collection<SitemapEntry> pages, boolean gz) throws IOException
	{
		OutputStream os = new FileOutputStream(dst_file);

		try
		{
			if(gz)
			{
				os = new GZIPOutputStream(os);
			}
			
			writeHeader(os);
			
			for(SitemapEntry page:pages)
			{
				writeEntry(os, page);
			}
			
			writeFooter(os);
		}
		finally
		{
			os.flush();
			os.close();
		}
	}
}