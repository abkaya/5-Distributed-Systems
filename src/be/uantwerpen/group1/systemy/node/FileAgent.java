package be.uantwerpen.group1.systemy.node;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	private NodeInterface nodeInterface;

	private HashMap<String, String> fileListAgent;
	private boolean agentFinished = false;
	private boolean permissionToDownload = false;

	/**
	 * Constructor of the FileAgent
	 * @param nodeInterface
	 */
	public FileAgent(NodeInterface nodeInterface)
	{
		// TODO Auto-generated constructor stub
		this.nodeInterface = nodeInterface;

	}

	/**
	 * Getters and setters
	 */
	public void setNodeInterface(NodeInterface nodeInterface)
	{
		this.nodeInterface = nodeInterface;
	}

	public void setAgentFinished(boolean agentFinished)
	{
		this.agentFinished = agentFinished;
	}

	public HashMap<String, String> getUpdatedFileListNode()
	{
		return fileListAgent;
	}

	public boolean isAgentFinished()
	{
		return agentFinished;
	}

	public boolean isPermissionToDownload()
	{
		return permissionToDownload;
	}

	public void setPermissionToDownload(boolean permissionToDownload)
	{
		this.permissionToDownload = permissionToDownload;
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{

		HashMap<String, String> fileListNode = null;
		ArrayList<String> currentNodeOwner = null;
		String hostName = null;
		String fileToLock = null;

		try
		{
			fileListNode = nodeInterface.getFileListNode();
			currentNodeOwner = nodeInterface.getCurrentNodeOwner();
			hostName = nodeInterface.getHostname();

		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		this.fileListAgent = updateListAgent(fileListAgent, currentNodeOwner);
		this.fileListAgent = processLock(fileListAgent, fileListNode, hostName);
		updateFileListNode(nodeInterface, fileListAgent);

		Iterator<Map.Entry<String, String>> entries = fileListAgent.entrySet().iterator();
		while (entries.hasNext())
		{
			Map.Entry<String, String> entry = entries.next();
			if (entry.getValue().equals(hostName))
			{
				this.fileListAgent = downloadFile(entry.getKey(), fileListAgent);
			}

		}

		agentFinished = true;

	}

	// This method will be replaced via an method in the interface of the node to get the list via RMI

	// /**
	// * This method will look at the files of the current node and calculate if the
	// * current node is the owner of certain files (point 2.b.i). Those files will be added
	// * to an arrayList
	// */
	// public static ArrayList<String> calculateOwnership(String currentNodeIp, String locationLocalFiles, String locationDownloadedFiles,
	// NameServerInterface nameServerInterface)
	// {
	// ArrayList<String> currentNodeOwner = new ArrayList<>();
	// ArrayList<String> localFiles = new ArrayList<>();
	// String tempIP = null;
	//
	// File folder = new File(locationLocalFiles);
	// File[] listOfFiles = folder.listFiles();
	// for (int i = 0; i < listOfFiles.length; i++)
	// {
	// localFiles.add(listOfFiles[i].getName());
	// }
	//
	// for (int i = 0; i < localFiles.size(); i++)
	// {
	// try
	// {
	// tempIP = nameServerInterface.getIPAddress(Hashing.hash(localFiles.get(i)));
	// } catch (RemoteException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// if (tempIP == currentNodeIp)
	// {
	// currentNodeOwner.add(localFiles.get(i));
	// }
	// }
	//
	// folder = new File(locationDownloadedFiles);
	// for (int i = 0; i < listOfFiles.length; i++)
	// {
	// localFiles.
	// }
	//
	// return currentNodeOwner;
	// }

	/**
	 * This will update the list of the fileAgent based on the ownership (point 2.b.i)
	 * @param currentNodeOwner: the list of files where the current node is owner off
	 * @param fileListNode: the fileList of the node (non updated)
	 * @param fileListAgent: the fileList of the agent (non updated)
	 * @return: the updated fileList of the agent
	 */
	public static HashMap<String, String> updateListAgent(HashMap<String, String> fileListAgent, ArrayList<String> currentNodeOwner)
	{

		// update the fileList of the agent based on the ownership of the currentNode
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.containsKey(currentNodeOwner.get(i)))
			{
				fileListAgent.put(currentNodeOwner.get(i), "notLocked");
			}

		}

		return fileListAgent;
	}

	/**
	 * This method will process locks for files of the currentNode
	 * @param nodeInterface
	 * @param fileListAgent
	 * @param fileListNode
	 * @param hostname
	 * @return
	 */
	public static HashMap<String, String> processLock(HashMap<String, String> fileListAgent, HashMap<String, String> fileListNode,
			String hostname)
	{
		Iterator<Map.Entry<String, String>> entries = fileListNode.entrySet().iterator();
		while (entries.hasNext())
		{
			Map.Entry<String, String> entry = entries.next();
			if (entry.getValue().equals("lockRequest"))
			{
				if (fileListAgent.get(entry.getKey()).equals("notLocked"))
				{
					fileListAgent.replace(entry.getKey(), "notLocked", hostname);
				}
			}

		}

		return fileListAgent;
	}

	public static void updateFileListNode(NodeInterface nodeInterface, HashMap<String, String> fileListAgent)
	{
		try
		{
			nodeInterface.updateFileListNode(fileListAgent);
		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method will wait till the download is successful and then put the value back to notLocked
	 * @param fileName: the name of the file for downloading
	 * @param fileListAgent: the fileList of the fileAgent
	 * @return: the updated fileList of the fileAgent
	 */
	public static HashMap<String, String> downloadFile(String fileName, HashMap<String, String> fileListAgent)
	{

		// Some logic for waiting till download is successful
		// (if download == successful)

		fileListAgent.replace(fileName, "notLocked");

		return fileListAgent;

	}

}