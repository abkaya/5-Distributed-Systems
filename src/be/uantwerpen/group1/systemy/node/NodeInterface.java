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
	String getNameFileToDownload() throws RemoteException;
	String getHostname() throws RemoteException;
	Boolean getFinishedDownload() throws RemoteException;

	void setDNSIP(String IP) throws RemoteException;
	void updateNextNode(NodeInfo newNode) throws RemoteException;
	void updatePreviousNode(NodeInfo newNode) throws RemoteException;
	void passFileAgent(FileAgent fileAgent) throws RemoteException;
	void updateFileListNode(ArrayList<String> fileList) throws RemoteException;
}

