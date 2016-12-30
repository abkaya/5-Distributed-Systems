package be.uantwerpen.group1.systemy.node;

import java.util.List;

public class FileRecord
{
	private String fileName;
	private List<String> downloadedByNodes;
	private String localByNode;
	
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public List<String> getDownloadedByNodes()
	{
		return downloadedByNodes;
	}
	public void setDownloadedByNodes(List<String> downloadedByNodes)
	{
		this.downloadedByNodes = downloadedByNodes;
	}
	public String getLocalByNode()
	{
		return localByNode;
	}
	public void setLocalByNode(String localByNode)
	{
		this.localByNode = localByNode;
	}	
}
