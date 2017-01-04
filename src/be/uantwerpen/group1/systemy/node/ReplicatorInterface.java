package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the replicator class
 *
 * @author Abdil Kaya
 */
public interface ReplicatorInterface extends Remote
{
	//String getOwnerLocation(String fileName);
	//String getPreviousNode(String nodeIP);
	public boolean hasOwnedFile(String fileName) throws RemoteException;
	public boolean hasFile(String fileName) throws RemoteException;
	public void addOwnedFile(String fileName) throws RemoteException;
	public void addLocalFile(String fileName) throws RemoteException;
	public void addDownloadedFile(String fileName) throws RemoteException;
	public void addFileRecord(FileRecord fileRecord) throws RemoteException;
	public void receiveFile(String fileName, String nodeIP) throws RemoteException;
}