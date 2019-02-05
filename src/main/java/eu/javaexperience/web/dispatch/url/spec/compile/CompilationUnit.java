package eu.javaexperience.web.dispatch.url.spec.compile;

import java.io.File;

import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.text.StringTools;

public class CompilationUnit
{
	public static final  CompilationUnit[] emptyCompilationUnitArray = new CompilationUnit[0];
	public String dir;
	public String cls;
	
	public CompilationUnit(String src, String cls)
	{
		this.dir = src;
		this.cls = cls;
	}
	
	public CompilationUnit(String src, Class<?> cls)
	{
		this.dir = src;
		this.cls = cls.getName();
	}

	public File getFile()
	{
		return getUnitFile(dir, ".java");
	}

	public File getUnitFile(@MayNull String base, @MayNull String extension)
	{
		StringBuilder sb = new StringBuilder();
		if(null != base)
		{
			sb.append(base);
			sb.append("/");
		}
		
		sb.append(StringTools.replaceAllStrings(cls, ".", "/"));
		
		if(null != extension)
		{
			sb.append(extension);
		}
		
		return new File(sb.toString());
	}

	public void updateTargetModificationDate
	(
		String outputDir
	)
	{
		getUnitFile(outputDir, ".js").setLastModified(getFile().lastModified());
	}

	public boolean checkNeedRecompile(String outputDir)
	{
		File dst = getUnitFile(outputDir, ".js");
		if(!dst.exists())
		{
			return true;
		}
		
		File src = getFile();
		return src.lastModified() > dst.lastModified();
	}
}