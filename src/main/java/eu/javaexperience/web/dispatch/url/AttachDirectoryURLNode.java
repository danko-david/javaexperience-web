package eu.javaexperience.web.dispatch.url;

import java.util.regex.Pattern;

import eu.javaexperience.file.AbstractFile;
import eu.javaexperience.file.FileSystemTools;

public class AttachDirectoryURLNode extends AttachFileUrlNode
{
	public AttachDirectoryURLNode(String nodeName, String localDir, boolean listDir)
	{
		super(nodeName, FileSystemTools.DEFAULT_FILESYSTEM.fromUri(localDir), listDir);
	}
	
	public AttachDirectoryURLNode(String nodeName,boolean caseSenitive,String localDir,boolean listDir)
	{
		super(nodeName, caseSenitive, FileSystemTools.DEFAULT_FILESYSTEM.fromUri(localDir), listDir);
	}
	
	public AttachDirectoryURLNode(String nodeName,Pattern regex,String localDir,boolean listDir)
	{
		super(nodeName, regex, FileSystemTools.DEFAULT_FILESYSTEM.fromUri(localDir), listDir);
	}
	
	public AttachDirectoryURLNode(String nodeName, AbstractFile localDir, boolean listDir)
	{
		super(nodeName, localDir, listDir);
	}
	
	public AttachDirectoryURLNode(String nodeName,boolean caseSenitive,AbstractFile localDir,boolean listDir)
	{
		super(nodeName, caseSenitive, localDir, listDir);
	}
	
	public AttachDirectoryURLNode(String nodeName,Pattern regex,AbstractFile localDir,boolean listDir)
	{
		super(nodeName, regex, localDir, listDir);
	}
}