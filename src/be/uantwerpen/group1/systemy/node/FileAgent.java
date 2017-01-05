package be.uantwerpen.group1.systemy.node;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.management.MBeanParameterInfo;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;

	private static String logName = Node.class.getName() + " >> ";

	private NodeInterface nodeInterface;

	private HashMap<String, String> fileListAgent;
	private boolean agentFinishedWithUpdate = false;
	private Boolean permissionToDownload = false;
	private Boolean agentFinished = false;

	/**
	 * Constructor of the FileAgent
	 * @param nodeInterface
	 */
	public FileAgent()
	{

	}

	/**
	 * Getters and setters
	 */

	public HashMap<String, String> getUpdatedFileListNode()
	{
		return fileListAgent;
	}

	public boolean IsAgentFinishedWithUpdate()
	{
		return agentFinishedWithUpdate;
	}

	public Boolean getPermissionToDownload()
	{
		return permissionToDownload;
	}

	public Boolean isAgentFinished()
	{
		return agentFinished;
	}

	public void setNodeInterface(NodeInterface nodeInterface)
	{
		this.nodeInterface = nodeInterface;
	}

	public void setAgentFinishedWithUpdate(boolean agentFinished)
	{
		this.agentFinishedWithUpdate = agentFinished;
	}

	public void setAgentFinished(Boolean agentFinished)
	{
		this.agentFinished = agentFinished;
	}

	public void setPermissionToDownload(Boolean permissionToDownload)
	{
		this.permissionToDownload = permissionToDownload;
	}

	/**
	 * 
	 */
	@Override
	public void run()
	{
		try
		{
			SystemyLogger.log(Level.INFO, logName + "Agent starts on node " + nodeInterface.getHostname());
			TimeUnit.SECONDS.sleep(1);
			SystemyLogger.log(Level.INFO, logName + "Agent finished on node " + nodeInterface.getHostname());
		} catch (RemoteException | InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// setAgentFinished(false);
		// setAgentFinished(false);
		// setPermissionToDownload(false);
		//
		// ArrayList<String> currentNodeOwner = null;
		// String hostName = null;
		// String fileToLock = null;
		//
		// try
		// {
		// currentNodeOwner = nodeInterface.getCurrentNodeOwner();
		// hostName = nodeInterface.getHostname();
		// fileToLock = nodeInterface.getNameFileToDownload();
		//
		// } catch (RemoteException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// // TODO Auto-generated method stub
		// this.fileListAgent = updateListAgent(this.fileListAgent, currentNodeOwner);
		// this.fileListAgent = processLock(this.fileListAgent, fileToLock, hostName);
		// updateFileListNode(this.nodeInterface, this.fileListAgent);
		//
		// setAgentFinishedWithUpdate(true);
		//
		// if (this.fileListAgent.get(fileToLock).equals(hostName))
		// {
		// setPermissionToDownload(true);
		//
		// try
		// {
		// do
		// {
		// TimeUnit.MILLISECONDS.sleep(100);
		// } while (!nodeInterface.getFinishedDownload());
		// } catch (RemoteException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InterruptedException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// // after download complete, release the lock
		// this.fileListAgent.replace(fileToLock, hostName, "notLocked");
		// }
		//
		// setAgentFinished(true);
		//
		// SystemyLogger.log(Level.INFO, "Agent finished on node " + hostName);
	}

	/**
	 * This will update the list of the fileAgent based on the ownership (point 2.b.i)
	 * @param currentNodeOwner: the list of files where the current node is owner off
	 * @param fileListNode: the fileList of the node (non updated)
	 * @param fileListAgent: the fileList of the agent (non updated)
	 * @return: the updated fileList of the agent
	 */
	public static HashMap<String, String> updateListAgent(HashMap<String, String> fileListAgent, ArrayList<String> currentNodeOwner)
	{

		// update the fileList of the agent based on the ownership of the currentNode
		for (int i = 0; i < currentNodeOwner.size(); i++)
		{
			if (!fileListAgent.containsKey(currentNodeOwner.get(i)))
			{
				fileListAgent.put(currentNodeOwner.get(i), "notLocked");
			}

		}

		return fileListAgent;
	}

	/**
	 * This method will process locks for files of the currentNode
	 * @param fileListAgent: the list of the FileAgent
	 * @param hostname: the hostname of the currentNode
	 * @return: the updated fileList of the agent
	 */
	public static HashMap<String, String> processLock(HashMap<String, String> fileListAgent, String fileToLock, String hostname)
	{
		if (fileListAgent.containsKey(fileToLock) && fileListAgent.get(fileToLock).equals("not locked"))
		{
			fileListAgent.replace(fileToLock, "notLocked", hostname);
		} else
		{
			SystemyLogger.log(Level.INFO, "file already locked, try again later");
		}

		return fileListAgent;
	}

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
			SystemyLogger.log(Level.SEVERE, e.getMessage());
		}
	}

}