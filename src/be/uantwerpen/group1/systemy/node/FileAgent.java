package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable
{
	private NameServerInterface nameServerInterface;

	private ArrayList<String> fileListAgent;
	private boolean agentFinished;

	private ArrayList<String> fileListNode;
	private ArrayList<String> updatedFileListNode;
	private String fileToLock;
	private String fileToDelete;
	private String currentNodeIp;
	private String locationFiles;

	public FileAgent(NameServerInterface nameServerInterface)
	{
		// TODO Auto-generated constructor stub
		this.nameServerInterface = nameServerInterface;

	}

	public void setFileListNode(ArrayList<String> fileListNode)
	{
		this.fileListNode = fileListNode;
	}

	public void setFileToLock(String fileToLock)
	{
		this.fileToLock = fileToLock;
	}

	public void setFileToDelete(String fileToDelete)
	{
		this.fileToDelete = fileToDelete;
	}

	public void setCurrentNodeIp(String currentNodeIp)
	{
		this.currentNodeIp = currentNodeIp;
	}

	public void setLocationFiles(String locationFiles)
	{
		this.locationFiles = locationFiles;
	}

	public void setAgentFinished(boolean agentFinished)
	{
		this.agentFinished = agentFinished;
	}

	public ArrayList<String> getUpdatedFileListNode()
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

		ArrayList<String> currentNodeOwner = calculateOwnership(currentNodeIp, locationFiles, nameServerInterface);
		fileListAgent = updateListAgent(currentNodeOwner, fileListAgent, fileToDelete);
		updatedFileListNode = updateFileListOnNode(fileListNode, fileListAgent, updatedFileListNode, fileToDelete);
		agentFinished = true;

	}

	/**
	 * This method will look at the files of the current node and calculate if the
	 * current node is the owner of certain files (point 2.b.i). Those files will be added
	 * to an arrayList
	 */
	public static ArrayList<String> calculateOwnership(String currentNodeIp, String locationFiles, NameServerInterface nameServerInterface)
	{
		ArrayList<String> currentNodeOwner = new ArrayList<>();
		ArrayList<String> localFiles = new ArrayList<>();
		String tempIP = null;

		File folder = new File(locationFiles);
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

		return currentNodeOwner;
	}

	/**
	 * This method will update the list from the fileAgent. Files where the current node
	 * is owner off will be added to the list and also if a node deleted a certain file
	 * this will also be deleted from the list of the fileAgent
	 */
	public static ArrayList<String> updateListAgent(ArrayList<String> currentNodeOwner, ArrayList<String> fileListAgent,
			String fileToDelete)
	{
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.contains(currentNodeOwner.get(i)))
			{
				fileListAgent.add(currentNodeOwner.get(i));
			}

		}

		if (fileListAgent.contains(fileToDelete))
		{
			fileListAgent.remove(fileToDelete);
		}

		return fileListAgent;
	}

	/**
	 * This method will update the list on the node as mentioned above
	 */
	public static ArrayList<String> updateFileListOnNode(ArrayList<String> fileListNode, ArrayList<String> fileListAgent,
			ArrayList<String> updatedFileListNode, String fileToDelete)
	{
		for (int i = 0; i < fileListAgent.size(); i++)
		{
			if (!fileListNode.contains(fileListAgent.get(i)))
			{
				updatedFileListNode.add(fileListAgent.get(i));

			}

			if (fileListNode.contains(fileToDelete))
			{
				updatedFileListNode.remove(fileToDelete);
			}

		}

		return updatedFileListNode;

	}

}