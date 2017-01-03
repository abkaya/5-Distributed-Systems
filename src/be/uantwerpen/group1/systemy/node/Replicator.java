package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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
	String remoteNSName = "NameServerInterface";
	NameServerInterface nsi = null;
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
			 * If the deleted file is one this node is the owner of, then make sure it is also removed from the 
			 * ownedFiles list, as well as the fileRecords list
			 */
			if (observedAction == 0)
			{

			} else if (observedAction == 1)
			{
				String tempOwnerIP = getOwnerLocation(observedFile);

				// IF the owner of this new file is the local node :
				if (tempOwnerIP == nodeIP)
				{
					localOwnerReplicationProcess(observedFile, tempOwnerIP);
				}
				// ELSE if the owner of this file is a remote node :
				else if (tempOwnerIP != nodeIP)
				{
					remoteOwnerReplicationProcess(observedFile, tempOwnerIP);
				}
			}
		}
	}

	/**
	 * Replication process to go through when the new local file is owned by the local node:
	  * - replicate file to previous node 
	  * - create fileRecord : set fileName, set this node as the "localByNode"-node, add the previous node to the
	  * 		"downloadedByNode" list AND add this fileRecord to the local fileRecords list. 
	  * 
	  * - add fileName to the local localFiles list. 
	  * - add fileName to the local ownedFiles list.
	  * 
	  * - add fileName to the remote previous node's downloadedFiles list. 
	  * 
	 */
	private void localOwnerReplicationProcess(String fileName, String remoteNodeIP)
	{
		sendFile(fileName, getPreviousNode(nodeIP));
		fileRecords.add(new FileRecord(fileName, remoteNodeIP, nodeIP));
		this.localFiles.add(fileName);
		this.ownedFiles.add(fileName);
		
		ReplicatorInterface ri = null;
		RMI<ReplicatorInterface> rmi = new RMI<ReplicatorInterface>();
		ri = rmi.getStub(ri, "ReplicatorInterface", remoteNodeIP, 1099);

		try
		{
			ri.addDownloadedFile(fileName);
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "The remote node could not execute addDownloadedFile method.");
		}
	}

	/**
	 * Replication process to go through when the new local file is owned by a remote node:
	 * 	- replicate the file to the owner 
	 * 	- create fileRecord : set fileName, set this node as the "localByNode"-node, add the owner node to the 
	 * 		"downloadedByNode" list, BUT add this fileRecord to the owner's fileRecords list. 
	 * 
	 * 	- add fileName to the remote/owner's ownedFiles list. 
	 *  - add fileName to the remote/owner's downloadedFiles list.
	 *  
	 * 	- add fileName to the local localFiles list.
	 */
	private void remoteOwnerReplicationProcess(String fileName, String remoteNodeIP)
	{
		sendFile(fileName, getOwnerLocation(fileName));
		
		this.localFiles.add(fileName);
		
		ReplicatorInterface ri = null;
		RMI<ReplicatorInterface> rmi = new RMI<ReplicatorInterface>();
		ri = rmi.getStub(ri, "ReplicatorInterface", remoteNodeIP, 1099);

		try
		{
			ri.addDownloadedFile(fileName);
			ri.addOwnedFile(fileName);
			ri.addFileRecord(new FileRecord(fileName, getOwnerLocation(fileName), nodeIP));
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "The remote node could not execute the remote owner replication process methods.");
		}
	}
	
	/**
	 * Method to add a fileRecord to the fileRecords list. Used by a remote node once the owner is known to be a remote node.
	 */
	@Override
	public void addFileRecord(FileRecord fileRecord) throws RemoteException
	{
		this.fileRecords.add(fileRecord);
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
	 * RMI method used by other nodes to check whether or not this node already knows it owns a file
	 */
	@Override
	public boolean hasOwnedFile(String fileName) throws RemoteException
	{
		return isFileInList(fileName, ownedFiles);
	}

	/**
	 * RMI method used by other nodes to check whether or not this node has a file available locally, either
	 * downloaded or made locally available on its own.
	 * @param String: fileName
	 * @return boolean 
	 */
	@Override
	public boolean hasFile(String fileName) throws RemoteException
	{
		return (isFileInList(fileName, localFiles) || isFileInList(fileName, downloadedFiles));
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
	 * Method to add a filename to the downloadedFiles list. This is done remotely by another node.
	 */
	@Override
	public void addDownloadedFile(String fileName) throws RemoteException
	{
		downloadedFiles.add(fileName);
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
	 * SendFile method. Used to send files, using the the remote node's receiveFile RMI method, which uses the already
	 * existing TCP API to request a file receive.
	 */
	private void sendFile(String fileName, String targetNodeIP)
	{
		ReplicatorInterface ri = null;
		RMI<ReplicatorInterface> rmi = new RMI<ReplicatorInterface>();
		ri = rmi.getStub(ri, "ReplicatorInterface", targetNodeIP, 1099);

		try
		{
			ri.receiveFile(fileName, nodeIP);
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "The remote node could not execute receiveFile method.");
		}
	}

	/**
	 * A method used when a certain node replicator needs to request a file. The method is strictly called remotely,
	 * and becomes solely a tool for the node replicator which is trying to send a file.
	 * So in essence,the sendFile method is used locally by a replicator, which makes a remote call to the 
	 * remote replicator, telling it to make the TCP file request from the calling node.
	 * 
	 * @param String : fileName
	 * @param String : fileServerNodeIP : the IP address of the node to request a file from. So the remote node calling this method remotely, will
	 * always provide its own nodeIP, as seen in the private sendFile method.
	 */
	@Override
	public void receiveFile(String fileName, String fileServerNodeIP) throws RemoteException
	{
		TCP fileClient = null;
		fileClient = new TCP(tcpFileTranferPort, fileServerNodeIP);
		fileClient.receiveFile(fileName);
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
		SystemyLogger.log(Level.INFO, logName + "Trying to set up the nameserver stub for the first time in the replicator.");
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);
		SystemyLogger.log(Level.INFO, logName + "dnsip : " + dnsIP + ", port : " + dnsPort + "remotename : " + remoteNSName);

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

		SystemyLogger.log(Level.INFO, logName + "Attempt to start the observable");
		observable.addObserver(this);
		observable.processEvents();

	}

}
