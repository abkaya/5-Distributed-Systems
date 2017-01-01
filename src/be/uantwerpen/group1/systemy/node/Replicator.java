package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import java.nio.file.*;

/**
 * Replicator Class which handles replication upon the change of local files in the localFiles folder.
 * The changes are detected using the observer pattern on a watchservice, which is event based.
 * 
 * @author Abdil Kaya
 */
public class Replicator implements ReplicatorInterface, Runnable, java.util.Observer
{
	private static String logName = Node.class.getName() + " >> ";

	List<String> ownedFiles;
	List<String> localFiles;
	List<String> downloadedFiles;
	String nodeIP = null;
	String dnsIP = null;
	int dnsPort = 0;
	int tcpFileTranferPort = 0;
	static String remoteNSName = "NameServerInterface";
	static NameServerInterface nsi = null;
	List<FileRecord> fileRecords = null;

	/**
	 * Get ownedFiles
	 * @return List<String> : ownedFiles 
	 */
	public List<String> getOwnedFiles()
	{
		return ownedFiles;
	}

	/**
	 * Set ownedFiles
	 * @param ownedFiles
	 */
	public void setOwnedFiles(List<String> ownedFiles)
	{
		this.ownedFiles = ownedFiles;
	}

	/**
	 * Get localFiles
	 * @return List<String> : localFiles
	 */
	public List<String> getLocalFiles()
	{
		return localFiles;
	}

	/**
	 * Set localFiles
	 * @param localFiles
	 */
	public void setLocalFiles(List<String> localFiles)
	{
		this.localFiles = localFiles;
	}

	/**
	 * Get downloadedFiles
	 * @return List<String> : downloadedFiles
	 */
	public List<String> getDownloadedFiles()
	{
		return downloadedFiles;
	}

	/**
	 * Set downloadedFiles
	 * @param downloadedFiles
	 */
	public void setDownloadedFiles(List<String> downloadedFiles)
	{
		this.downloadedFiles = downloadedFiles;
	}

	/**
	 * Get fileRecords
	 * @return List<FileRecord> : fileRecords
	 */
	public List<FileRecord> getFileRecords()
	{
		return fileRecords;
	}

	/**
	 * Set fileRecords
	 * @param fileRecords
	 */
	public void setFileRecords(List<FileRecord> fileRecords)
	{
		this.fileRecords = fileRecords;
	}

	/**
	 * Replicator observes the files in the localFiles folder. Delete or create actions are performed 
	 * on these files. Hence the filename and action variable below.
	 */
	private String observedFile;
	private int observedAction;

	/**
	 * This is the observer update method, called whenever the watchservice(observable) is on its turn triggered by a file change
	 * event in the folder it observes
	 * @param observable the observable is the WatchService, which is an event based folder change checker
	 */
	@Override
	public void update(Observable o, Object args)
	{
		if (o instanceof ObservableWatchService)
		{
			ObservableWatchService watchService = (ObservableWatchService) o;
			this.observedAction = watchService.getAction();
			this.observedFile = watchService.getFileName();
			SystemyLogger.log(Level.INFO, logName + "action : " + observedAction + " , fileName : " + observedFile);

			/*
			 * - handle the delete operation (observedAction == 0) by removing the file from the local lists. 
			 * - handle the create operation (observedAction == 1) by the replication process...
			 */
			/*
			 * If the deleted file is one we're the owner of, then 
			 */
			if (observedAction == 0)
			{

			} else if (observedAction == 1)
			{
				String tempOwnerIP = getOwnerLocation(observedFile);

				/*
				 * Once the owner is known, check whether or not it is the local node. 
				 */

				// IF it is the local node :
				if (tempOwnerIP == nodeIP)
				{

				}
				/*
				 * ELSE if the owner is not this node: 
				 * 	- replicate the file to the owner 
				 * 	- create fileRecord : set fileName, set this node as the "localByNode"-node, add the owner node to the 
				 * 		"downloadedByNode" list, BUT add this fileRecord to the owner's fileRecords list. 
				 * 	- add fileName to the remote/owner's ownedFiles list. 
				 * 	- add fileName to the local localFiles list.
				 * 	- add fileName to the remote/owner's localFiles list.
				 */
				else
				{

				}
			}
		}
	}

	/**
	 * Replication process to go through when the new local file is owned by the local node:
	  * - replicate file to previous node 
	  * - create fileRecord : set fileName, set this node as the "localByNode"-node, add the previous node to the
	  * 		"downloadedByNode" list AND add this fileRecord to the local fileRecords list. 
	  * - add fileName to the local localFiles list. 
	  * - add fileName to the remote previous node's localFiles list. 
	  * - add fileName to the local ownedFiles list.
	 */
	private void localOwnerReplicationProcess()
	{

	}

	private void remoteOwnerReplicationProcess()
	{

	}

	/**
	 * Request the file's owner IP address from the nameserver, using RMI
	 */
	@Override
	public String getOwnerLocation(String fileName)
	{
		try
		{
			return nsi.getIPAddress(hash(fileName));
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "Owner of file: " + observedFile + ". could not be found!");
			return null;
		}
	}

	/**
	 * Get the previous node of a given node, using the nameserver's remote method
	 */
	@Override
	public String getPreviousNode(String nodeIP)
	{
		try
		{
			return nsi.getNodeIP(nsi.getPreviousNode(hash(nodeIP)));
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "Owner of file: " + observedFile + ". could not be found!");
			return null;
		}
	}

	/**
	 * Method dealing with the replication process, triggered once the observer pattern
	 * update method is called
	 */
	private void replicate()
	{

	}

	/**
	 * RMI method used by other nodes to check whether or not this node already knows it owns a file
	 */
	@Override
	public boolean hasOwnedFile(String fileName) throws RemoteException
	{
		return isFileInList(fileName, ownedFiles);
	}

	/**
	 * RMI method used by other nodes to check whether or not this node has a file available locally
	 * @param String: fileName
	 * @return boolean : whether or not the file is available on this machine locally
	 */
	@Override
	public boolean hasLocalFile(String fileName) throws RemoteException
	{
		return isFileInList(fileName, localFiles);
	}

	/**
	 * Private method used to check whether or not a list of strings contains a String
	 * @param str
	 * @param list
	 * @return boolean : true if list contains string
	 */
	private boolean isFileInList(String str, List<String> list)
	{
		for (String files : list)
		{
			if (files == str)
				return true;
		}
		return false;
	}

	/**
	 * Method to set the ownership of a file. This is done remotely by another node.
	 */
	@Override
	public void addOwnedFile(String fileName) throws RemoteException
	{
		ownedFiles.add(fileName);
	}

	/**
	 * Possession of a local file is/can be set by a remote node
	 */
	@Override
	public void addLocalFile(String fileName) throws RemoteException
	{
		localFiles.add(fileName);
	}

	/** 
	 * Hashes the passed string
	 * The has is bound to range : 0 - 32768
	 * @param nameToConvert : the string of which a hash will be returned
	 * @return the hash of nameToConvert
	 */
	public int hash(String nameToConvert)
	{
		return Hashing.hash(nameToConvert);
	}

	/**
	 * Returns a list of the local files within the relative directory localFiles/
	 * @return List<String> localFiles
	 */
	public static List<String> findLocalFiles()
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
	 * Replicator constructor 
	 * @param nodeIP
	 * @param tcpFileTranferPort
	 * @param dnsIP
	 * @param dnsPort
	 */
	public Replicator(String nodeIP, int tcpFileTranferPort, String dnsIP, int dnsPort) throws IOException
	{
		this.tcpFileTranferPort = tcpFileTranferPort;
		this.nodeIP = nodeIP;
		this.dnsIP = dnsIP;
		this.dnsPort = dnsPort;

	}

	@Override
	public void run()
	{
		localFiles = findLocalFiles();
		TCP fileClient = null;

		/*
		 * This block listens in another thread for incoming requests by other nodes who wish to receive files That's all there is to it for
		 * sending files.
		 */
		TCP fileServer = new TCP(nodeIP, tcpFileTranferPort);
		new Thread(() ->
		{
			fileServer.listenToSendFile();
		}).start();

		/*
		 * This block creates the name server stub to use the NS its remote methods
		 */
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);

		/*
		 * 
		 */

		/*
		 * Get the file location for all current files. Any further operation within this for loop block is for testing purposes only and
		 * must/will be adjusted to its proper functionality asap.
		 */
		for (String localFile : localFiles)
		{
			SystemyLogger.log(Level.INFO, logName + "---Replication Thread----");
			SystemyLogger.log(Level.INFO, logName + localFile);
			System.out.println(localFile);
			try
			{
				fileClient = new TCP(tcpFileTranferPort, nsi.getIPAddress(hash(localFile)));
				fileClient.receiveFile(localFile);
				SystemyLogger.log(Level.INFO, logName + localFile + " owner request from name server returns: " + nsi.getIPAddress(hash(
						localFile)));
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// This line is where startup ends! From here on out, everything update related is handled.

		/*
		 * Update will check for events in the directory containing the local files. Rather than polling, we will use event based checks
		 * called "file change notifications", Watch Service API in the java.nio.file package.
		 * https://docs.oracle.com/javase/tutorial/essential/io/notification.html
		 */

		// register directory and process its events
		Path dir = Paths.get("localFiles");
		// pass the relative directory and whether or not to watch recursively. We don't
		// check for files entered recursively.
		ObservableWatchService observable = null;
		try
		{
			observable = new ObservableWatchService(dir, false);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		observable.addObserver(this);
		observable.processEvents();

	}
}
