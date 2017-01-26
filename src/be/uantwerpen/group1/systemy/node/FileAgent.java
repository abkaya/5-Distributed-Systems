package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	private static String logName = Node.class.getName() + " >> ";

	private NodeInterface nodeInterface;
	private NodeInterface nextNodeInterface;
	private NameServerInterface nameServerInterface;

	private HashMap<String, String> fileListAgent = new HashMap<>();

	/**
	 * Constructor of the FileAgent
	 * @param nodeInterface
	 */
	public FileAgent(NameServerInterface nameServerInterface)
	{
		this.nameServerInterface = nameServerInterface;
	}

	/**
	 * Getters and setters
	 */

	public void setNextNodeInterface(NodeInterface nextNodeInterface)
	{
		this.nextNodeInterface = nextNodeInterface;
	}

	public String getActiveNode()
	{

		String hostName = null;
		try
		{
			hostName = nodeInterface.getHostname();
		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hostName;
	}

	public void setNodeInterface(NodeInterface nodeInterface)
	{
		this.nodeInterface = nodeInterface;
	}

	/**
	 * Run method of the FileAgent
	 */
	public void run()
	{

		try
		{
			//SystemyLogger.log(Level.INFO, logName + "Agent starts on node " + nodeInterface.getHostname());

			//TimeUnit.SECONDS.sleep(1);

			// testCode();

			String hostname = nodeInterface.getHostname();
			String ipAddress = nodeInterface.getIPAddress();
			String fileToLock = nodeInterface.getFileToDownload();
			String fileToDelete = nodeInterface.getFileToDeleteInNetwork();

			// First, if there is a lock the file download
			if (fileListAgent.containsValue(nodeInterface.getHostname()))
			{
				String fileToDownload = null;

				Iterator<Map.Entry<String, String>> entries = fileListAgent.entrySet().iterator();
				while (entries.hasNext())
				{
					Map.Entry<String, String> entry = entries.next();
					if (entry.getValue().equals(nodeInterface.getHostname()))
					{
						fileToDownload = entry.getKey();
					}
				}

				this.fileListAgent = downloadFile(fileListAgent, fileToDownload);
			}

			ArrayList<String> files = new ArrayList<>();
			files.addAll((ArrayList<String>) loadLocalFiles());
			files.addAll((ArrayList<String>) loadDownloadedFiles());
			ArrayList<String> currentNodeOwner = calculateOwner(nameServerInterface, files, ipAddress);
			this.fileListAgent = updateListAgent(this.fileListAgent, currentNodeOwner, fileToDelete);

			// if there is no file to lock, just update the lists
			if (!(fileToLock == null))
			{
				processLock(this.fileListAgent, fileToLock, hostname);

			}

			//SystemyLogger.log(Level.INFO, logName + fileToLock);

			updateFileListNode(this.nodeInterface, this.fileListAgent);

			// // First, if there is a lock of the current node, download the file
			// if (fileListAgent.containsValue(nodeInterface.getHostname()))
			// {
			// String fileToDownload = null;
			//
			// Iterator<Map.Entry<String, String>> entries = fileListAgent.entrySet().iterator();
			// while (entries.hasNext())
			// {
			// Map.Entry<String, String> entry = entries.next();
			// if (entry.getValue().equals(nodeInterface.getHostname()))
			// {
			// fileToDownload = entry.getKey();
			// }
			// }
			//
			// String ipOwner = nameServerInterface.getIPAddress(Hashing.hash(fileToDownload));
			// SystemyLogger.log(Level.INFO, logName + "The owner of this file is: " + ipOwner);
			//
			// TimeUnit.MILLISECONDS.sleep(1);
			//
			// fileListAgent.replace(fileToDownload, nodeInterface.getHostname(), "notLocked");
			//
			// nodeInterface.setFileToDownload(null);
			// SystemyLogger.log(Level.INFO, logName + "fileToDownload: " + nodeInterface.getFileToDownload());
			// }

			//SystemyLogger.log(Level.INFO, logName + "Agent finished on node " + nodeInterface.getHostname());
			//SystemyLogger.log(Level.INFO, logName + "Sending the fileAgent to " + nextNodeInterface.getHostname() + " in 5 seconds");

			TimeUnit.MILLISECONDS.sleep(100);

			// when the fileAgent is ready with its tasks, move it along to the next node
			nextNodeInterface.passFileAgent(this);
		} catch (InterruptedException | IOException e)
		{
			// TODO Auto-generated catch block
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}

	}

	/**
	 * Returns a list of the local files within the relative directory localFiles/
	 * @return List<String> localFiles
	 */
	public static List<String> loadLocalFiles()
	{
		List<String> localFiles = new ArrayList<String>();
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

	/**
	 * Returns a list of the downloaded files within the relative directory downloadedFiles/
	 * @return List<String> localFiles
	 */
	public static List<String> loadDownloadedFiles()
	{
		List<String> downloadedFiles = new ArrayList<String>();
		File[] files = new File("downloadedFiles/").listFiles();
		for (File file : files)
		{
			if (file.isFile())
			{
				downloadedFiles.add(file.getName());
			}
		}
		return downloadedFiles;
	}

	/**
	 * This method will calculate the ownership of the files in the folders (point 2.b.i)
	 * @param nameServerInterface: interface of the nameserver
	 * @param files: the files found on the current node
	 * @param ipAddress: the ip address of the current node
	 * @return: an Arraylist with the name of the files where this node is owner of
	 */
	public ArrayList<String> calculateOwner(NameServerInterface nameServerInterface, ArrayList<String> files, String ipAddress)
	{
		ArrayList<String> currentNodeOwner = new ArrayList<>();

		try
		{

			for (int i = 0; i < files.size(); i++)
			{

				if (ipAddress.equals(nameServerInterface.getIPAddress(Hashing.hash(files.get(i)))))
				{
					currentNodeOwner.add(files.get(i));
				}

			}
		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return currentNodeOwner;
	}

	/**
	 * This will update the list of the fileAgent based on the ownership (point 2.b.i)
	 * @param currentNodeOwner: the list of files where the current node is owner off
	 * @param fileListNode: the fileList of the node (non updated)
	 * @param fileListAgent: the fileList of the agent (non updated)
	 * @return: the updated fileList of the agent
	 */
	public HashMap<String, String> updateListAgent(HashMap<String, String> fileListAgent, ArrayList<String> currentNodeOwner,
			String fileToDelete)
	{
		// add files to the file list that are new
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.containsKey(currentNodeOwner.get(i)))
			{
				fileListAgent.put(currentNodeOwner.get(i), "notLocked");
			}

		}

		if (fileListAgent.containsKey(fileToDelete))
		{
			fileListAgent.remove(fileToDelete);
		}

		return fileListAgent;
	}

	/**
	 * This method will update the file list of the node via RMI
	 * @param nodeInterface: the interface of the node
	 * @param fileListAgent: the filelist of the agent
	 */
	public static void updateFileListNode(NodeInterface nodeInterface, HashMap<String, String> fileListAgent)
	{
		ArrayList<String> fileListUpdated = new ArrayList<>();
		try
		{
			Iterator<Map.Entry<String, String>> entries = fileListAgent.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry<String, String> entry = entries.next();
				fileListUpdated.add(entry.getKey());
			}

			nodeInterface.updateFileListNode(fileListUpdated);

		} catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * This method will process locks for files of the currentNode
	 * @param fileListAgent: the list of the FileAgent
	 * @param hostname: the hostname of the currentNode
	 * @return: the updated fileList of the agent
	 */
	private HashMap<String, String> processLock(HashMap<String, String> fileListAgent, String fileToLock, String hostname)
	{
		if (fileListAgent.get(fileToLock).equals("notLocked"))
		{
			fileListAgent.replace(fileToLock, hostname);

			SystemyLogger.log(Level.INFO, logName + fileToLock + " is locked by " + hostname);

		} else
		{
			SystemyLogger.log(Level.INFO, logName + fileToLock + " already locked, try again later");
		}

		return fileListAgent;
	}

	/**
	 * This method will let the specified node download a certain file
	 * @param fileListAgent
	 * @param fileToDownload
	 * @return
	 */
	private HashMap<String, String> downloadFile(HashMap<String, String> fileListAgent, String fileToDownload)
	{

		try
		{
			String ipOwner = nameServerInterface.getIPAddress(Hashing.hash(fileToDownload));
			do
			{
				TimeUnit.MILLISECONDS.sleep(1);

			} while (!nodeInterface.downloadFile(fileToDownload, ipOwner));

			fileListAgent.replace(fileToDownload, nodeInterface.getHostname(), "notLocked");
			nodeInterface.setFileToDownload(null);

		} catch (RemoteException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}

		return fileListAgent;

	}

	/**
	 * This is a test method
	 */
	public void testCode()
	{
		try
		{
			String hostname = nodeInterface.getHostname();
			String ipAddress = nodeInterface.getIPAddress();
			String fileToLock = nodeInterface.getFileToDownload();
			ArrayList<String> fileListNode = nodeInterface.getFileListNode();
			String fileToDelete = nodeInterface.getFileToDeleteInNetwork();

			System.out.println("current fileList of node");
			System.out.println("_____________________________________");
			for (int i = 0; i < fileListNode.size(); i++)
			{
				System.out.println(fileListNode.get(i));
			}

			System.out.println();
			System.out.println();

			ArrayList<String> files = new ArrayList<>();
			files.addAll((ArrayList<String>) loadLocalFiles());
			files.addAll((ArrayList<String>) loadDownloadedFiles());

			System.out.println("files in the folders");
			System.out.println("_____________________________________");
			for (int i = 0; i < files.size(); i++)
			{
				System.out.println(files.get(i));

			}

			ArrayList<String> currentNodeOwner = calculateOwner(nameServerInterface, files, ipAddress);

			System.out.println();
			System.out.println("files where the current node is owner of");
			System.out.println("_____________________________________");

			for (int i = 0; i < currentNodeOwner.size(); i++)
			{
				System.out.println(currentNodeOwner.get(i));

			}

			this.fileListAgent = updateListAgent(this.fileListAgent, currentNodeOwner, fileToDelete);

			System.out.println();
			System.out.println();

			System.out.println("file list from the agent");
			System.out.println("_____________________________________");

			Iterator<Map.Entry<String, String>> entries = this.fileListAgent.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry<String, String> entry = entries.next();
				System.out.println(entry.getKey());
				System.out.println(entry.getValue());
			}

			System.out.println();
			System.out.println();

			// if there is no file to lock, just update the lists
			if (!(fileToLock == null))
			{
				processLock(this.fileListAgent, fileToLock, hostname);

			}

			SystemyLogger.log(Level.INFO, logName + fileToLock);

			System.out.println();
			System.out.println();

			System.out.println("file list from the agent");
			System.out.println("_____________________________________");

			entries = this.fileListAgent.entrySet().iterator();
			while (entries.hasNext())
			{
				Map.Entry<String, String> entry = entries.next();
				System.out.println(entry.getKey());
				System.out.println(entry.getValue());
			}

			System.out.println();
			System.out.println();

			TimeUnit.SECONDS.sleep(5);

			updateFileListNode(this.nodeInterface, this.fileListAgent);

			fileListNode = nodeInterface.getFileListNode();

			System.out.println("updated fileList of the node");
			System.out.println("_____________________________________");
			for (int i = 0; i < fileListNode.size(); i++)
			{
				System.out.println(fileListNode.get(i));
			}

			System.out.println();
			System.out.println();

			// First, if there is a lock of the current node, download the file
			if (fileListAgent.containsValue(nodeInterface.getHostname()))
			{
				String fileToDownload = null;

				entries = fileListAgent.entrySet().iterator();
				while (entries.hasNext())
				{
					Map.Entry<String, String> entry = entries.next();
					if (entry.getValue().equals(nodeInterface.getHostname()))
					{
						fileToDownload = entry.getKey();
					}
				}

				String ipOwner = nameServerInterface.getIPAddress(Hashing.hash(fileToDownload));
				SystemyLogger.log(Level.INFO, logName + "The owner of this file is: " + ipOwner);

				TimeUnit.MILLISECONDS.sleep(1);

				fileListAgent.replace(fileToDownload, nodeInterface.getHostname(), "notLocked");
				nodeInterface.setFileToDownload(null);
				SystemyLogger.log(Level.INFO, logName + "fileToDownload: " + nodeInterface.getFileToDownload());
			}

			TimeUnit.SECONDS.sleep(5);

		} catch (RemoteException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			SystemyLogger.log(Level.SEVERE, e.getMessage());
		}

	}

}