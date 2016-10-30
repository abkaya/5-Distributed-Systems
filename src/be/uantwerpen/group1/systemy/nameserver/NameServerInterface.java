/**
 * 
 */
package be.uantwerpen.group1.systemy.nameserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Abdil Kaya
 *
 */
public interface NameServerInterface extends Remote {
    String getIPAddress (String fileName) throws RemoteException;
}
