package be.uantwerpen.group1.systemy.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicatorInterface extends Remote
{
	String getOwnerLocation(String fileName) throws RemoteException;
	public boolean hasOwnedFile(String fileName) throws RemoteException;
	public boolean hasLocalFile(String fileName) throws RemoteException;
	public void addOwnedFile(String fileName) throws RemoteException;
	public void addLocalFile(String fileName) throws RemoteException;
}