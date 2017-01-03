package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	private NodeInterface nodeInterface;

	private HashMap<String, String> fileListAgent;
	private boolean agentFinished = false;

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

	/**
	 * 
	 */
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		this.fileListAgent = updateListAgent(nodeInterface, fileListAgent);
		
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
	public static HashMap<String, String> updateListAgent(NodeInterface nodeInterface, HashMap<String, String> fileListAgent)
	{
		ArrayList<String> currentNodeOwner = null;

		try
		{
			currentNodeOwner = nodeInterface.getCurrentNodeOwner();

		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	
	public 

	/**
	 * 
	 * @param fileListAgent
	 * @param updatedFileListNode
	 * @return
	 */
	public static HashMap<String, String> updateFileListOnNode(HashMap<String, String> fileListAgent,
			HashMap<String, String> updatedFileListNode)
	{
		Iterator<Map.Entry<String, String>> entries = fileListAgent.entrySet().iterator();
		while (entries.hasNext())
		{
			Map.Entry<String, String> entry = entries.next();
			updatedFileListNode.put(entry.getKey(), entry.getValue());

		}

		return updatedFileListNode;

	}

	/**
	 * A node can only download a file when the fileAgent is active on the node itself
	 */
	public static void downloadFile()
	{

	}

}