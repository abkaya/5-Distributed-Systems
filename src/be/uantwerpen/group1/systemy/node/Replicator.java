package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import java.nio.file.*;

public class Replicator implements ReplicatorInterface, Runnable, java.util.Observer
{
	List<String> ownedFiles;
	List<String> localFiles;
	List<String> downloadedFiles;
	String nodeIP = null;
	String dnsIP = null;
	int dnsPort = 0;
	int tcpFileTranferPort = 0;
	String remoteNSName = "NameServerInterface";
	NameServerInterface nsi = null;
	
	/**
	 * Replicator observes the files in the localFiles folder. Delete or create actions are performed 
	 * on these files. Hence the filename and action variable below.
	 */
	private String observedFile;
    private int observedAction;
   

    @Override
    public void update(Observable o, Object args) {
        if(o instanceof ObservableWatchService)
        {
            ObservableWatchService watchService = (ObservableWatchService)o;
            this.observedAction = watchService.getAction();
            this.observedFile = watchService.getFileName();
            System.out.println("ConcreteObserver >>  action : "+observedAction+" , fileName : "+observedFile);
        }
    }

	@Override
	public String getOwnerLocation(String fileName) throws RemoteException
	{
		return null;
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
	 * The ownership of a file is set remotely by another node
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
	public String hash(String nameToConvert)
	{
		return Integer.toString(Hashing.hash(nameToConvert));
	}

	/**
	 * Returns a list of the local files within the relative directory localFiles/
	 * @return List<String> localFiles
	 */
	public static List<String> getLocalFiles()
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
	public Replicator(String nodeIP, int tcpFileTranferPort, String dnsIP, int dnsPort, Observable observable)
	{
		this.tcpFileTranferPort = tcpFileTranferPort;
		this.nodeIP = nodeIP;
		this.dnsIP = dnsIP;
		this.dnsPort = dnsPort;
		observable.addObserver(this);
	}

	@Override
	public void run()
	{
		localFiles = getLocalFiles();
		TCP fileClient = null;

		/*
		 * This block listens for incoming requests by other nodes who wish to receive files
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
		 * Get the file location for all current files. 
		 * Any further operation within this for loop block is for testing purposes only
		 * and must/will be adjusted to its proper functionality asap.
		 */
		for (String localFile : localFiles)
		{
			System.out.println("---Replication Thread----");
			System.out.println(localFile);
			try
			{
				fileClient = new TCP(tcpFileTranferPort, nsi.getFileLocation(hash(localFile)));
				fileClient.receiveFile(localFile);
				System.out.println(localFile + " owner request from name server returns: " + nsi.getFileLocation(hash(localFile)));
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// This line is where startup ends! From here on out, everything update related is handled.

		/*
		 * Update will check for events in the directory containing the local files. Rather than polling, we will use event 
		 * based checks called "file change notifications", Watch Service API in the java.nio.file package. 
		 * https://docs.oracle.com/javase/tutorial/essential/io/notification.html
		 */
		
	}
}
