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
	String getFileToDownload() throws RemoteException;
	String getHostname() throws RemoteException;
	String getIPAddress() throws RemoteException;
	String getFileToDeleteInNetwork() throws RemoteException;

	//for testing purpose
	ArrayList<String>getFileListNode() throws RemoteException;


	void setDNSIP(String IP) throws RemoteException;

	void updateNextNode(NodeInfo newNode) throws RemoteException;

	void updatePreviousNode(NodeInfo newNode) throws RemoteException;
	void passFileAgent(FileAgent fileAgent) throws RemoteException;
	void updateFileListNode(ArrayList<String> fileList) throws RemoteException;
	void setFileToDownload(String fileToDownload) throws RemoteException;
	Boolean downloadFile(String fileToDownload, String ipOwner) throws RemoteException;

	// used for replicator methods
	void replicateLocalFiles() throws RemoteException;

	void printFileRecordsStatus() throws RemoteException;
}
