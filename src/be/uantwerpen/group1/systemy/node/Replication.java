package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Replication implements ReplicationInterface, Runnable
{
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
	 * This functions sends the files to their respective owner.
	 * Should this node be the only 
	 * @param args
	 */
	public static void main(String args[])
	{
		List<String> localFiles = getLocalFiles();
		for (String localFile : localFiles)
		{
			System.out.println(localFile);
		}

	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getIPAddress(String fileName) throws RemoteException
	{

		return null;
	}
}
