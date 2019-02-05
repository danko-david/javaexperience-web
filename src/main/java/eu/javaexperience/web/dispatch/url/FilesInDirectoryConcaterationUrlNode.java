package eu.javaexperience.web.dispatch.url;

import java.io.File;

import eu.javaexperience.asserts.AssertArgument;
import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.fs.os.OsFile;
import eu.javaexperience.io.file.FileTools;

public class FilesInDirectoryConcaterationUrlNode extends FileListConcaterationUrlNode
{
	protected AbstractFile dir;

	public FilesInDirectoryConcaterationUrlNode(String name, AbstractFile directory, String mime)
	{
		super(name, mime, FileTools.emptyFileArray);
		AssertArgument.assertNotNull(directory, "directory");
		if(!(this.dir = directory).isDirectory())
		{
			throw new RuntimeException("Given file is not directory: "+dir);
		}
		this.list = this.dir.listFiles();
	}
	
	public FilesInDirectoryConcaterationUrlNode(String name, File directory, String mime)
	{
		this(name, new OsFile(directory), mime);
	}
	
	@Override
	protected AbstractFile[] getFileList()
	{
		return dir.listFiles();
	}
}