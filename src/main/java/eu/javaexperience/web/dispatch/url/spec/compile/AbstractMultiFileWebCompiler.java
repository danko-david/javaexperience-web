package eu.javaexperience.web.dispatch.url.spec.compile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import eu.javaexperience.collection.CollectionTools;
import eu.javaexperience.io.IOTools;
import eu.javaexperience.io.file.FileTools;
import eu.javaexperience.struct.BeforeAfterFixture;
import eu.javaexperience.text.StringTools;

public abstract class AbstractMultiFileWebCompiler implements WebCompiler
{
	public AbstractMultiFileWebCompiler(String dstDir)
	{
		this.dstDir = dstDir;
	}
	
	public String dstDir;
	
	public Set<String> sourceDirs = new HashSet<>();
	
	public ArrayList<CompilationUnit> clsToCompile = new ArrayList<>();
	
	public void addSourceDirs(String... dir)
	{
		CollectionTools.copyInto(dir, sourceDirs);
	}
	
	public void addClassToCompile(Class<?> cls)
	{
		for(String src:sourceDirs)
		{
			File f = toSourceFile(src, cls);
			if(f.exists())
			{
				clsToCompile.add(new CompilationUnit(src, cls));
				return;
			}
		}
		
		throw new RuntimeException("Class ("+cls+") not found in paths: "+CollectionTools.toString(sourceDirs));
	}
	
	public void addClassToCompile(String cls)
	{
		for(String src:sourceDirs)
		{
			File f = toSourceFile(src, cls);
			if(f.exists())
			{
				clsToCompile.add(new CompilationUnit(src, cls));
				return;
			}
		}
		
		throw new RuntimeException("Class ("+cls+") not found in paths: "+CollectionTools.toString(sourceDirs));
	}
	
	public void addClassesToCompile(Class<?>... cls)
	{
		for(Class<?> c:cls)
		{
			addClassToCompile(c);
		}
	}
	
	public void addClassesToCompile(String... cls)
	{
		for(String c:cls)
		{
			addClassToCompile(c);
		}
	}
	
	public void compile(boolean incremental)
	{
		new File(dstDir).mkdirs();
		ArrayList<File> f = new ArrayList<File>();
		for(String s:sourceDirs)
		{
			f.add(new File(s));
		}
		
		generateAll
		(
			getCompilationUnits(),
			helperClasses,
			f,
			dstDir,
			incremental
		);
	}
	
	protected abstract void generateAll
	(
		CompilationUnit[] compilationUnits,
		Set<String> helperClasses2,
		ArrayList<File> f,
		String dstDir,
		boolean incremental
	);

	public static File toSourceFile(String root, Class<?> c)
	{
		return new File(root+"/"+StringTools.replaceAllStrings(c.getName(), ".", "/")+".java");
	}
	
	public static File toSourceFile(String root, String c)
	{
		return new File(root+"/"+StringTools.replaceAllStrings(c, ".", "/")+".java");
	}
	
	protected BeforeAfterFixture<File[]> getExtraFileConcatList()
	{
		return null;
	};
	
	public void concatOneFile(String dst) throws IOException
	{
		try(final OutputStream os = new FileOutputStream(dst))
		{
			BeforeAfterFixture<File[]> ba = getExtraFileConcatList();
			if(null != ba && null != ba.before)
			{
				for(File f:ba.before)
				{
					if(null != f)
					{
						IOTools.copyFileContentToStream(f, os);
					}
				}
			}
			
			for(CompilationUnit cu:clsToCompile)
			{
				IOTools.copyFileContentToStream(cu.getUnitFile(dstDir, ".js"), os);
			}
			
			if(null != ba && null != ba.after)
			{
				for(File f:ba.after)
				{
					if(null != f)
					{
						IOTools.copyFileContentToStream(f, os);
					}
				}
			}
			
			os.flush();
		};
	}

	public CompilationUnit[] getCompilationUnits()
	{
		return clsToCompile.toArray(CompilationUnit.emptyCompilationUnitArray);
	}

	public void cleanupDir()
	{
		File f = new File(dstDir);
		FileTools.deleteDirectory(f, false);
		f.mkdirs();
	}

	public String getDestinationDir()
	{
		return dstDir;
	}
	
	public Set<String> getSourceDirs()
	{
		return new HashSet<String>(sourceDirs);
	}

	protected Set<String> helperClasses = new HashSet<>(); 
	
	public void addHelperClassesToCompile(String... helperClasses)
	{
		CollectionTools.copyInto(helperClasses, this.helperClasses);
	}
	
	public void addHelperClassesToCompile(Class<?>... helperClasses)
	{
		for(Class<?> c: helperClasses)
		{
			this.helperClasses.add(c.getName());
		}
	}

	public Set<String> getHelperClasses()
	{
		return helperClasses;
	}

	public long getSourcesLastModification()
	{
		long t0 = 0;
		for(CompilationUnit cu:getCompilationUnits())
		{
			long t = cu.getFile().lastModified();
			if(t > t0)
			{
				t0 = t;
			}
		}
		
		return t0;
	}
}
