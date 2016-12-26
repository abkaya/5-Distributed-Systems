package be.uantwerpen.group1.systemy.node;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class FailureAgent implements Runnable, Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String failedHostIp;
	private static String currentHostIp;
	
	private static List<String> fileList = null;
	private static NameServerInterface nsi = null;

	public FailureAgent(String failedHostIp, String currentHostip, HashMap<String, Boolean> fileList, NameServerInterface nsi)
	{
		// TODO Auto-generated constructor stub
		FailureAgent.failedHostIp = failedHostIp;
		FailureAgent.currentHostIp = currentHostip;
		FailureAgent.nsi = nsi;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		String tempIp = null;
		fileList = getLocalFiles();
		for (int i = 0; i < fileList.size(); i++)
		{
			try
			{
				if (failedHostIp == nsi.getIPAddress(Hashing.hash(fileList.get(i))))
				{
					

				}
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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

}
