package eu.javaexperience.web.dispatch.url.spec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.generic.TimeAttrEntry;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.reflect.Mirror;
import eu.javaexperience.text.StringTools;
import eu.javaexperience.web.MIME;
import eu.javaexperience.web.dispatch.url.FilesInDirectoryConcaterationUrlNode;

public class JsTriggerDirectory extends FilesInDirectoryConcaterationUrlNode
{
	public JsTriggerDirectory(String nodeName, File dir)
	{
		super(nodeName, dir, MIME.javascript.mime);
	}
	
	public JsTriggerDirectory(String nodeName, AbstractFile dir)
	{
		super(nodeName, dir, MIME.javascript.mime);
	}
	
	public byte[] loadContent()
	{
		TimeAttrEntry<byte[]>[] fs = getContentList();
		
		StringBuilder sb = new StringBuilder();
		
		if(null != fs)
		{
			sb.append("document.addEventListener(\"DOMContentLoaded\", function() {\n\n");
			for(TimeAttrEntry<byte[]> sf:fs)
			{
				try
				{
					Object origin = sf.getOrigin();

					String filestr = null;
					String content = null;

					if(origin instanceof AbstractFile)
					{
						AbstractFile af = (AbstractFile) origin;
						filestr = af.getFileName();
						try(InputStream is = af.openRead())
						{
							content = new String(IOTools.loadAllFromInputStream(is));
						}
					}
					else
					{
						filestr = origin.toString();
						content = IOTools.getFileContents(filestr);
					}
					
					sb.append("if(null != document.querySelector(\"#js_trigger_"+StringTools.getSubstringAfterLastString(filestr, "/", filestr)+"\"))\n{\n");
					sb.append(content);
					sb.append("\n}\n\n");
				}
				catch (IOException e)
				{
					Mirror.propagateAnyway(e);
				}
			}
			sb.append("\n});\n");
		}
		
		return sb.toString().getBytes();
	}
}
