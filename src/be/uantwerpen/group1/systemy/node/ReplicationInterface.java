package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicationInterface extends Remote {
    String getIPAddress (String fileName) throws RemoteException;
}