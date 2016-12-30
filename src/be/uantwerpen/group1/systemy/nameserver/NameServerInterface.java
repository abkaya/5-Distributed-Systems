package be.uantwerpen.group1.systemy.nameserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Abdil Kaya
 */
public interface NameServerInterface extends Remote {
    String getFileLocation (String fileName) throws RemoteException;
    void removeNode(int hash) throws RemoteException;
	int getNextNode(int hash) throws RemoteException;
	public String getPreviousNode(String nodeHash) throws RemoteException;
}