package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable
{
	private static ArrayList<String> fileListNode;
	private static String fileToLock;
	private static String currentNodeIp;
	private static NameServerInterface nameServerInterface;
	private static NodeInfo nodeInfoCurrentNode;

	// This is the file list of the agent with all the files in the distributed filesystem
	private static ArrayList<String> fileListAgent;

	public FileAgent(ArrayList<String> fileListNode, String fileToLock, String nodeIp, NameServerInterface nameServerInterface,
			NodeInfo nodeInfoCurrentNode)
	{
		// TODO Auto-generated constructor stub
		FileAgent.fileListNode = fileListNode;
		FileAgent.fileToLock = fileToLock;
		FileAgent.currentNodeIp = nodeIp;
		FileAgent.nameServerInterface = nameServerInterface;
		FileAgent.nodeInfoCurrentNode = nodeInfoCurrentNode;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		calculateOwnershipAndUpdateFileAgentList();
		updateFileListOnNode();

	}

	/**
	 * This method will look at the files of the current node and calculate if the
	 * current node is the owner of certain files (point 2.b.i and 2.b.ii). When this is true
	 * the file will be added to the filelist of the agent.
	 */
	public static void calculateOwnershipAndUpdateFileAgentList()
	{
		String tempIP = null;
		ArrayList<String> filesCurrentNode = getLocalFiles();

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
				fileListAgent.add(filesCurrentNode.get(i));
			}
		}
	}

	public static void updateFileListOnNode()
	{
		for (int i = 0; i < fileListAgent.size(); i++)
		{

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
