package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for inter node communication
 *
 * @author Robin
 */
public interface NodeInterface extends Remote {
	void setDNSIP(String IP) throws RemoteException;
	void updateNextNode(NodeInfo newNode) throws RemoteException;
	void updatePreviousNode(NodeInfo newNode) throws RemoteException;
	void passFileAgent(FileAgent fileAgent) throws RemoteException;
}
