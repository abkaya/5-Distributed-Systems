package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Replicator implements ReplicatorInterface, Runnable
{
	List<String> ownedFiles;
	List<String> localFiles;
	List<String> downloadedFiles;
	String dnsIP = null;
	
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
	public int hashing(String nameToConvert) {
		return (Math.abs((nameToConvert.hashCode())) % 32768);
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

	
	@Override
	public void run()
	{
		/*
		 * This method will iteratively gain its functionality.
		 * It currently lists its own local files.
		 */
		localFiles = getLocalFiles();
		for (String localFile : localFiles)
		{
			System.out.println(localFile);
		}
	}
}
