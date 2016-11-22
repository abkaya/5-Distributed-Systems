package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;

public class Replicator implements ReplicatorInterface, Runnable
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
		for(String files : ownedFiles)
		{
			if(files == fileName)
				return true;
		}
		return false;
	}

	/**
	 * RMI method used by other nodes to check whether or not this node has a file available locally
	 */
	@Override
	public boolean hasLocalFile(String fileName) throws RemoteException
	{
		for(String files : localFiles)
		{
			if(files == fileName)
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
	public String hash(String nameToConvert) {
		return Integer.toString((Math.abs((nameToConvert.hashCode())) % 32768));
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
     * @param nodeIP
     * @param tcpFileTranferPort
     * @param dnsIP
     * @param dnsPort
     */
    public Replicator(String nodeIP, int tcpFileTranferPort, String dnsIP, int dnsPort)
	{
    	this.tcpFileTranferPort = tcpFileTranferPort;
    	this.nodeIP = nodeIP;
		this.dnsIP = dnsIP;
		this.dnsPort = dnsPort;
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
		 * Get the file location for all current files
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
		
	}
}
