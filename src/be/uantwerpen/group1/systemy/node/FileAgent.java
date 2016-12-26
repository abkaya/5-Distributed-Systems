package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	// The list of files from the agent
	private ArrayList<String> fileListAgent;
	// The list of files where the current node is owner from
	private static ArrayList<String> currentNodeOwner;
	// This is the list of files from the node
	private static ArrayList<String> fileListNode;
	// The files that are locked
	private ArrayList<String> lockedFiles;
	
	private ArrayList<String> requestToLock;
	// Interface for RMI
	private static NameServerInterface nsi;
	// Ip address of the current node
	private static String nodeIpCurrentNode;

	/**
	 * 
	 * @param fileListNode
	 * @param nodeIp
	 * @param nsi
	 */
	public FileAgent(ArrayList<String> fileListNode, String fileToLock, String nodeIp, NameServerInterface nsi)
	{
		// TODO Auto-generated constructor stub
		FileAgent.fileListNode = fileListNode;
		FileAgent.nodeIpCurrentNode = nodeIp;
		FileAgent.nsi = nsi;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		calculateOwnership();
		updateListAgent();
		updateListNode();

	}
	
	
	

	/**
	 * This method will update the list of files from the Node
	 * @return
	 */
	private ArrayList<String> updateListNode()
	{
		// TODO Auto-generated method stub
		for (int i = 0; i < fileListAgent.size(); i++)
		{
			if (!fileListNode.contains(fileListAgent.get(i)))
			{
				fileListNode.add(fileListAgent.get(i));
			}

		}
		return fileListNode;
	}

	/**
	 * This method will update the list of files from the agent based on the ownership
	 * from the file of the current node (point 2.b.ii)
	 */
	private void updateListAgent()
	{
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.contains(currentNodeOwner.get(i)))
			{
				fileListAgent.add(currentNodeOwner.get(i));
			}

		}

	}

	/**
	 * This method will look at the files of the current node and calculate if the
	 * current node is the owner of certain files (point 2.b.i)
	 */
	public static void calculateOwnership()
	{
		String tempIP = null;
		ArrayList<String> filesCurrentNode = getLocalFiles();

		for (int i = 0; i < filesCurrentNode.size(); i++)
		{
			try
			{
				tempIP = nsi.getIPAddress(Hashing.hash(filesCurrentNode.get(i)));
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tempIP == nodeIpCurrentNode)
			{
				currentNodeOwner.add(filesCurrentNode.get(i));
			}
		}
	}

	/**
	 * Returns a list of the local files within the relative directory localFiles/
	 * @return List<String> localFiles
	 */
	public static ArrayList<String> getLocalFiles()
	{
		ArrayList<String> localFiles = new ArrayList<String>();
		File[] files = new File("localFiles/").listFiles();
		for (File file : files)
		{
			if (file.isFile())
			{
				localFiles.add(file.getName());
			}
		}
		return localFiles;
	}

}
