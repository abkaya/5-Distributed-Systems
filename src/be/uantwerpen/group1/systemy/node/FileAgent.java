package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	private NameServerInterface nameServerInterface;

	private HashMap<String, String> fileListAgent;
	private boolean agentFinished;

	private HashMap<String, String> fileListNode;
	private HashMap<String, String> updatedFileListNode;

	private String currentNodeIp;
	private String locationLocalFiles;
	private String locationDownloadedFiles;

	public FileAgent(NameServerInterface nameServerInterface)
	{
		// TODO Auto-generated constructor stub
		this.nameServerInterface = nameServerInterface;

	}

	public void setFileListNode(HashMap<String, String> fileListNode)
	{
		this.fileListNode = fileListNode;
	}

	public void setCurrentNodeIp(String currentNodeIp)
	{
		this.currentNodeIp = currentNodeIp;
	}

	public void setLocationFiles(String locationFiles)
	{
		this.locationLocalFiles = locationFiles;
	}

	public void setAgentFinished(boolean agentFinished)
	{
		this.agentFinished = agentFinished;
	}

	public HashMap<String, String> getUpdatedFileListNode()
	{
		return updatedFileListNode;
	}

	public boolean isAgentFinished()
	{
		return agentFinished;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

		ArrayList<String> currentNodeOwner = calculateOwnership(currentNodeIp, locationLocalFiles, nameServerInterface);
		fileListAgent = updateListAgent(currentNodeOwner, fileToLock, fileListAgent);
		updatedFileListNode = updateFileListOnNode(fileListNode, fileListAgent, updatedFileListNode);
		agentFinished = true;

	}
	
	
	
	public HashMap<String, Boolean> lockFile(String fileToLock, HashMap<String, Boolean> fileListNode) {
		if(fileListNode.containsKey(fileToLock)) {
			fileListNode.put(fileToLock, true);
		}
		return fileListNode;
	}
	
	

	/**
	 * This method will look at the files of the current node and calculate if the
	 * current node is the owner of certain files (point 2.b.i). Those files will be added
	 * to an arrayList
	 */
	public static ArrayList<String> calculateOwnership(String currentNodeIp, String locationLocalFiles, String locationDownloadedFiles, NameServerInterface nameServerInterface)
	{
		ArrayList<String> currentNodeOwner = new ArrayList<>();
		ArrayList<String> localFiles = new ArrayList<>();
		String tempIP = null;

		File folder = new File(locationLocalFiles);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++)
		{
			localFiles.add(listOfFiles[i].getName());
		}

		for (int i = 0; i < localFiles.size(); i++)
		{
			try
			{
				tempIP = nameServerInterface.getIPAddress(Hashing.hash(localFiles.get(i)));
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tempIP == currentNodeIp)
			{
				currentNodeOwner.add(localFiles.get(i));
			}
		}
		
		folder = new File(locationDownloadedFiles);
		for (int i = 0; i < listOfFiles.length; i++)
		{
			localFiles.
		} 

		return currentNodeOwner;
	}

	/**
	 * This method will update the list from the fileAgent. Files where the current node
	 * is owner off will be added to the list and also if a node deleted a certain file
	 * this will also be deleted from the list of the fileAgent.
	 */
	public static HashMap<String, Boolean> updateListAgent(ArrayList<String> currentNodeOwner, HashMap<String, String> fileListAgent)
	{
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.containsKey(currentNodeOwner.get(i))) {
				fileListAgent.put(currentNodeOwner.get(i), "notLocked");
			}
			
		}
		if (fileListAgent.containsKey(fileToLock))
		{
			fileListAgent.replace(fileToLock, true);
		}

		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.containsKey(currentNodeOwner.get(i)))
			{
				fileListAgent.put(currentNodeOwner.get(i), false);
			}

		}

		return fileListAgent;
	}

	/**
	 * This method will update the list on the node as mentioned above
	 */
	public static ArrayList<String> updateFileListOnNode(ArrayList<String> fileListNode, HashMap<String, Boolean> fileListAgent,
			ArrayList<String> updatedFileListNode)
	{
		// Transform the keys of the hashmap to an ArrayList
		Set<String> kSet = fileListAgent.keySet();
		ArrayList<String> fileListAgentArray = new ArrayList<String>(kSet);

		// If the name of the file is not present in the fileList of the node, then add it
		for (int i = 0; i < fileListAgentArray.size(); i++)
		{
			if (!fileListNode.contains(fileListAgent.get(i)))
			{
				updatedFileListNode.add(fileListAgentArray.get(i));

			}
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