package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.sound.sampled.Port;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import java.nio.file.*;

/**
 * Replicator Class which handles replication upon the change of local files in the localFiles folder, as well as on startup 
 * and network member node updates.<br>
 * The localFiles folder changes are detected using the observer pattern on a watchservice, which  in its turn is event based.<br>
 * <br>
 * Keeps a records of localFiles, ownedFiles, downloadedFiles and a FileRecord for every file this node is an owner of.
 * 
 * @author Abdil Kaya
 */
public class Replicator implements ReplicatorInterface, Runnable, java.util.Observer
{
	private static String logName = Node.class.getName() + " >> ";

	List<String> ownedFiles = new ArrayList<String>();
	List<String> localFiles = new ArrayList<String>();
	List<String> downloadedFiles = new ArrayList<String>();
	String nodeIP = null;
	String dnsIP = null;
	String hostName = null;
	int dnsPort = 0;
	int tcpFileTranferPort = 0;
	String remoteNSName = "NameServerInterface";
	NameServerInterface nsi = null;
	List<FileRecord> fileRecords = new ArrayList<FileRecord>();
	RMI<NameServerInterface> nameServerRMI = null;
	// init skeleton
	ReplicatorInterface ri = null;
	// RMI object does not require the constructor with hostname and whatnot. The registry is already running.
	RMI<ReplicatorInterface> replicatorRMI = new RMI<ReplicatorInterface>();

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
				if (localFiles.contains(observedFile))
					localFiles.remove(observedFile);
			} else if (observedAction == 1)
			{
				maintainFileRecords();
				replicate(observedFile);
			}
		}
		printFileRecords();
	}

	/**
	 * Replicate method. This method is used to handle the replication process, from which it branches off
	 * depending on which node owns the file which is being replicated.
	 * @param String fileName
	 */
	@Override
	public void replicate(String fileName)
	{
		String tempOwnerIP = getOwnerLocation(fileName);

		// IF the owner of this new file is the local node :
		if (tempOwnerIP.equalsIgnoreCase(nodeIP))
		{
			SystemyLogger.log(Level.INFO, logName + fileName + " is owned by this node, (" + tempOwnerIP + "==" + nodeIP
					+ "). Replicating to the previous node if it exists.");
			localOwnerReplicationProcess(fileName, getPreviousNode(hostName));
		}
		// ELSE if the owner of this file is a remote node :
		else if (!tempOwnerIP.equalsIgnoreCase(nodeIP) && !tempOwnerIP.isEmpty())
		{
			// Every node has to play fair and check for itself whether or not it's wrongfully assigned as an owner.
			if (ownedFiles.contains(observedFile))
				ownedFiles.remove(observedFile);
			deleteFileRecordByFileName(observedFile);
			SystemyLogger.log(Level.INFO, logName + fileName + " is owned by another node, (" + tempOwnerIP + "!=" + nodeIP
					+ "). Replicating to the owner node.");
			remoteOwnerReplicationProcess(fileName, tempOwnerIP);
		}
	}

	/**
	 * Method to replicate all local files. Generally used once at startup.
	 */
	public void replicateLocalFiles()
	{
		for (String localFile : findLocalFiles())
		{
			replicate(localFile);
		}
	}

	/**
	 * Replication process to go through when the new local file is owned by the local node:<br>
	  * - replicate file to previous node <br>
	  * - create fileRecord : set fileName, set this node as the "localByNode"-node, add the previous node to the<br>
	  * 		"downloadedByNode" list AND add this fileRecord to the local fileRecords list. <br>
	  * <br>
	  * - add fileName to the local localFiles list. <br>
	  * - add fileName to the local ownedFiles list.<br>
	  * <br>
	  * - add fileName to the remote previous node's downloadedFiles list. <br>
	  * 
	 */
	private void localOwnerReplicationProcess(String fileName, String remoteNodeIP)
	{
		if (!nodeIP.equalsIgnoreCase(remoteNodeIP))
			sendFile(fileName, getPreviousNode(hostName));
		if (!fileRecordsContainFileName(fileName))
			fileRecords.add(new FileRecord(fileName, getPreviousNode(hostName), nodeIP));
		else
		{
			if (!getFileRecordByFileName(fileName).getDownloadedByNodes().contains(getPreviousNode(hostName)))
				getFileRecordByFileName(fileName).addDownloadedBy(getPreviousNode(hostName));
		}
		if (!localFiles.contains(fileName))
			this.localFiles.add(fileName);
		if (!ownedFiles.contains(fileName))
			this.ownedFiles.add(fileName);

		ReplicatorInterface tempRi = null;
		tempRi = replicatorRMI.getStub(tempRi, "ReplicatorInterface", remoteNodeIP, 1099);

		try
		{
			if (!tempRi.hasDownloadedFile(fileName))
				tempRi.addDownloadedFile(fileName);
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "The remote node could not execute addDownloadedFile method.");
		}
		SystemyLogger.log(Level.INFO, logName + "Added file name and record to the appropriate lists, both locally and remotely.");
	}

	/**
	 * Replication process to go through when the new local file is owned by a remote node:
	 * <br>
	 * replicate the file to the owner <br>
	 * 	- create fileRecord : set fileName, set this node as the "localByNode"-node, add the owner node to the <br>
	 * 		"downloadedByNode" list, BUT add this fileRecord to the owner's fileRecords list. <br>
	 * 	- IF the fileRecord is already available on this node, then make sure to adjust and pass that fileRecord, and delete it from this node afterwards.<br>
	 * <br>
	 * 	- add fileName to the remote/owner's ownedFiles list. <br>
	 *  - add fileName to the remote/owner's downloadedFiles list.<br>
	 *  <br>
	 * 	- add fileName to the local localFiles list.<br>
	 */
	private void remoteOwnerReplicationProcess(String fileName, String remoteNodeIP)
	{
		sendFile(fileName, remoteNodeIP);
		SystemyLogger.log(Level.INFO, logName + "Owner of " + fileName + " is " + remoteNodeIP);
		if (!localFiles.contains(fileName))
			this.localFiles.add(fileName);

		ReplicatorInterface tempRi = null;
		while (tempRi == null)
		{
			tempRi = replicatorRMI.getStub(tempRi, "ReplicatorInterface", remoteNodeIP, 1099);
			if (tempRi == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try
		{
			if (!tempRi.hasDownloadedFile(fileName))
				tempRi.addDownloadedFile(fileName);
			if (!tempRi.hasOwnedFile(fileName))
				tempRi.addOwnedFile(fileName);
			if (!tempRi.fileRecordsContainFileName(fileName))
			{
				if (!fileRecordsContainFileName(fileName))
					tempRi.addFileRecord(new FileRecord(fileName, remoteNodeIP, nodeIP));
				else
				{
					getFileRecordByFileName(fileName).addDownloadedBy(remoteNodeIP);
					tempRi.addFileRecord(getFileRecordByFileName(fileName));
					deleteFileRecordByFileName(fileName);
				}

			}
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName
					+ "The remote node could not execute the remote owner replication process methods. Is object serializable?");
		}
		SystemyLogger.log(Level.INFO, logName + "Added file name and record to the appropriate lists, both locally and remotely.");
	}

	/**
	 * SendFile method. Used to send files, using the the remote node's receiveFile RMI method, which uses the already
	 * existing TCP API to request a file receive.
	 */
	private void sendFile(String fileName, String targetNodeIP)
	{
		ReplicatorInterface tempRi = null;
		while (tempRi == null)
		{
			tempRi = replicatorRMI.getStub(tempRi, "ReplicatorInterface", targetNodeIP, 1099);
			if (tempRi == null)
			{
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try
		{
			tempRi.receiveFile(fileName, nodeIP);
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "The remote node could not execute receiveFile method.");
		}
	}

	/**
	 * a method to print the current fileRecords and their data
	 */
	@Override
	public void printFileRecords()
	{
		System.out.println("[________________________________FileRecords________________________________]");
		System.out.println("Files owned by this node " + hostName + "(" + hash(hostName) + ")[" + nodeIP + "] \n");
		for (FileRecord fr : fileRecords)
		{
			System.out.println("\t Filename: ");
			System.out.println("\t\t - " + fr.getFileName() + "(" + hash(fr.getFileName()) + ")");

			System.out.println("\t Local By: ");
			System.out.println("\t\t - " + fr.getLocalByNode());

			System.out.println("\t Replicated to: ");
			for (String db : fr.getDownloadedByNodes())
			{
				System.out.println("\t\t - " + db);
			}
			System.out.println("\t \n----------------------- ");

		}
		System.out.println("[___________________________________________________________________________]");
	}

	/**
	 * A method used when a certain node replicator needs to request a file. The method is strictly called remotely,
	 * and becomes solely a tool for the node replicator which is trying to send a file.<br>
	 * So in essence,the sendFile method is used locally by a replicator, which makes a remote call to the 
	 * remote replicator, telling it to make the TCP file request from the calling node.<br>
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
	 * Method delete a fileRecord by its fileName attribute
	 */
	public void deleteFileRecordByFileName(String fileName)
	{
		List<FileRecord> toRemove = new ArrayList<FileRecord>();
		for (FileRecord fr : fileRecords)
		{
			if (fr.getFileName().equals(fileName))
				toRemove.add(fr);
		}
		for (FileRecord fr : toRemove)
			fileRecords.remove(fr);
	}

	/**
	 * Method to get fileRecord by filename
	 * Used when sending the fileRecord to a new owner.
	 */
	public FileRecord getFileRecordByFileName(String fileName)
	{
		FileRecord toReturn = null;
		for (FileRecord fr : fileRecords)
		{
			if (fr.getFileName().equals(fileName))
				toReturn = fr;
		}
		return toReturn;
	}

	/**
	 * Method to check whether or not the fileRecords contains a fileRecord with a certain fileName
	 */
	@Override
	public boolean fileRecordsContainFileName(String fileName)
	{
		for (FileRecord fr : fileRecords)
		{
			if (fr.getFileName().equals(fileName))
				return true;
		}
		return false;
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
	 * Method to maintain the fileRecords. If the node isn't an owner anymore,
	 * necessary steps should be taken.
	 */
	public void maintainFileRecords()
	{
		/*
		 * make sure the fileRecords are maintained properly. Check for new owners!
		 * If this node isn't the owner any longer, let the node who owns the file locally,
		 * replicate it to the new owner. Make sure this node is listed as one that has a replica of the file
		 */
		List<FileRecord> toRemove = new ArrayList<FileRecord>();
		for (FileRecord fr : fileRecords)
		{
			if (!getOwnerLocation(fr.getFileName()).equals(nodeIP))
			{
				toRemove.add(fr);
				ReplicatorInterface tempRi = null;
				tempRi = replicatorRMI.getStub(tempRi, "ReplicatorInterface", getOwnerLocation(fr.getFileName()), 1099);
				try
				{
					tempRi.replicate(fr.getFileName());
				} catch (RemoteException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for (FileRecord fr : toRemove)
			fileRecords.remove(fr);
	}

	/**
	 * Request the file's owner IP address from the nameserver, using RMI
	 */
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
	public String getPreviousNode(String hostName)
	{
		try
		{
			String prevNode = nsi.getNodeIP(nsi.getPreviousNode(hash(hostName)));
			if (!prevNode.isEmpty())
				return prevNode;
			else
				return nodeIP;
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.WARNING, logName + "Owner of file: " + observedFile + ". could not be found!");
			return null;
		}
	}

	/**
	 * Method to check whether or not a certain file name is listed in  the downloadedFiles list
	 */
	@Override
	public boolean hasDownloadedFile(String fileName) throws RemoteException
	{
		if (downloadedFiles.contains(fileName))
			return true;
		else
			return false;
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
	 * Replicator constructor 
	 * @param nodeIP
	 * @param tcpFileTranferPort
	 * @param dnsIP
	 * @param dnsPort
	 */
	public Replicator(String hostName, String nodeIP, int tcpFileTranferPort, String dnsIP, NameServerInterface nameServerInterface)
			throws IOException
	{
		this.tcpFileTranferPort = tcpFileTranferPort;
		this.nodeIP = nodeIP;
		this.dnsIP = dnsIP;
		this.nsi = nameServerInterface;
		this.hostName = hostName;
	}

	/**
	 * Replicator constructor for RMI
	 */
	public Replicator() throws IOException
	{
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

		// Init replicator rmi skeleton, by binding the object to the already running registry
		ri = this;
		replicatorRMI.bindObject("ReplicatorInterface", ri, nodeIP, dnsPort);
		// Replicate all local files first
		replicateLocalFiles();

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
			SystemyLogger.log(Level.SEVERE, logName + "Could not start the observable watch service.");
		}

		SystemyLogger.log(Level.INFO, logName + "Starting the observable watchService now");
		observable.addObserver(this);
		observable.processEvents();

	}

}
