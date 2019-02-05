package eu.javaexperience.web.dispatch.url;

import java.io.File;
import java.util.ArrayList;

import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.FileSystemTools;
import eu.javaexperience.generic.TimeAttrEntry;
import eu.javaexperience.generic.TimeAttrEntryTools;
import eu.javaexperience.io.IOFunctions;

public class FileListConcaterationUrlNode extends ContentConcaterationURLNode
{
	protected AbstractFile[] list;
	
	public FileListConcaterationUrlNode(String name, String mime)
	{
		super(name, mime);
		this.list = FileSystemTools.emptyAbstractFileArray;
	}
	
	public FileListConcaterationUrlNode(String name, String mime, AbstractFile... files)
	{
		super(name, mime);
		this.list = files;
	}
	
	public FileListConcaterationUrlNode(String name, String mime, File... files)
	{
		super(name, mime);
		this.list = FileSystemTools.warpFiles(files);
	}
	
	public void setNewFileList(File... files)
	{
		list = FileSystemTools.warpFiles(files);
		lastModified = 0;
		refresh();
	}
	
	public void setNewFileList(AbstractFile... files)
	{
		list = files;
		lastModified = 0;
		refresh();
	}
	
	protected AbstractFile[] getFileList()
	{
		return list;
	}
	
	@Override
	protected TimeAttrEntry<byte[]>[] getContentList()
	{
		ArrayList<TimeAttrEntry<byte[]>> ret = new ArrayList<>();
		for(AbstractFile f:getFileList())
		{
			if(!f.isDirectory())
			{
				ret.add(TimeAttrEntryTools.fromFileWithExaminer(f, IOFunctions.LOAD_AFILE_CONTENT));
			}
		}
		
		return ret.toArray(TimeAttrEntryTools.emptyTimeAttrEntryArray);
	}
}
