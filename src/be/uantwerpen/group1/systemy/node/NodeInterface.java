package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for inter node communication 
 * 
 * @author Robin
 */
public interface NodeInterface extends Remote {
	boolean ping() throws RemoteException;
}
