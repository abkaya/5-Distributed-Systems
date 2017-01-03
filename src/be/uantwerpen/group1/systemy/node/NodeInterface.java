package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * RMI interface for inter node communication
 *
 * @author Robin
 */
public interface NodeInterface extends Remote
{
	ArrayList<String> getCurrentNodeOwner() throws RemoteException;
	HashMap<String, String> getFileListNode() throws RemoteException;
	String getHostname() throws RemoteException;
	String getFileToDownload() throws RemoteException;
	boolean downloadCompleted() throws RemoteException;

	void setDNSIP(String IP) throws RemoteException;
	void updateNextNode(NodeInfo newNode) throws RemoteException;
	void updatePreviousNode(NodeInfo newNode) throws RemoteException;
	void passFileAgent(FileAgent fileAgent) throws RemoteException;
	void updateFileListNode(HashMap<String, String> fileList) throws RemoteException;
}
