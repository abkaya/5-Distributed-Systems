package be.uantwerpen.group1.systemy.node;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable
{
	private static NameServerInterface nameServerInterface;
	private static NodeInfo nodeInfoCurrentNode;

	private static ArrayList<String> fileListAgent;
	private static HashMap<String, Boolean> fileLockMap;
	private boolean fileAgentInNetwork;
	private boolean agentFinished;

	public FileAgent(NameServerInterface nameServerInterface, NodeInfo nodeInfoCurrentNode)
	{
		// TODO Auto-generated constructor stub
		this.nameServerInterface = nameServerInterface;
		this.nodeInfoCurrentNode = nodeInfoCurrentNode;

	}

	public boolean isFileAgentInNetwork()
	{
		return fileAgentInNetwork;
	}

	public void setFileAgentInNetwork(boolean fileAgentInNetwork)
	{
		this.fileAgentInNetwork = fileAgentInNetwork;
	}

	public boolean isAgentFinished()
	{
		return agentFinished;
	}

	public void setAgentFinished(boolean agentFinished)
	{
		this.agentFinished = agentFinished;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		ArrayList<String> fileListNode = nodeInfoCurrentNode.getFileList();
		String fileToLock = nodeInfoCurrentNode.getFileToLock();
		String fileToDelete = nodeInfoCurrentNode.getFileToDelete();
		String currentNodeIp = nodeInfoCurrentNode.getIP();

		updateListAgent(calculateOwnership(currentNodeIp), fileToDelete);
		updateFileListOnNode(fileListNode, fileToDelete);

		agentFinished = true;

	}

	/**
	 * This method will look at the files of the current node and calculate if the
	 * current node is the owner of certain files (point 2.b.i). Those files will be added
	 * to an arrayList
	 */
	public static ArrayList<String> calculateOwnership(String currentNodeIp)
	{
		String tempIP = null;
		ArrayList<String> filesCurrentNode = NodeInfo.getLocalFiles();
		ArrayList<String> currentNodeOwner = new ArrayList<>();

		for (int i = 0; i < filesCurrentNode.size(); i++)
		{
			try
			{
				tempIP = nameServerInterface.getIPAddress(Hashing.hash(filesCurrentNode.get(i)));
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tempIP == currentNodeIp)
			{
				currentNodeOwner.add(filesCurrentNode.get(i));
			}
		}

		return currentNodeOwner;
	}

	/**
	 * This method will update the list from the fileAgent. Files where the current node
	 * is owner off will be added to the list and also if a node deleted a certain file
	 * this wil also be deleted from the list of the fileAgent
	 */
	public static void updateListAgent(ArrayList<String> currentNodeOwner, String fileToDelete)
	{
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (fileListAgent.contains(currentNodeOwner.get(i)))
			{

			} else
			{
				fileListAgent.add(currentNodeOwner.get(i));

			}

		}

		if (fileListAgent.contains(fileToDelete))
		{
			fileListAgent.remove(fileToDelete);
		}
	}

	/**
	 * This method will update the list on the node as mentioned above
	 */
	public static void updateFileListOnNode(ArrayList<String> fileListNode, String fileToDelete)
	{
		for (int i = 0; i < fileListAgent.size(); i++)
		{
			if (fileListNode.contains(fileListAgent.get(i)))
			{

			} else
			{
				nodeInfoCurrentNode.addFileToFileList(fileListAgent.get(i));

			}

		}

		if (fileListNode.contains(fileToDelete))
		{
			nodeInfoCurrentNode.deleteFileFromFileList(fileToDelete);
		}

	}

}
