package be.uantwerpen.group1.systemy.nameserver;

import java.rmi.RemoteException;

import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.MulticastListener;;

public class NameServer implements NameServerInterface
{

	public static void main(String args[])
	{
		NameServerRegister nsr = new NameServerRegister(false);
		
		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>("192.168.1.103","NameServerInterface",nsi);
		
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", 1337);
		while(true) {
			String multicastMessage = multicastListener.receive();
			System.out.println("Received: " + multicastMessage);
			// TODO: add incoming (new) IP to hashtable
			System.out.println("IP needs to be added to hashtable (if new)");
		}
		
	}

	@Override
	public String getIPAddress(String fileName) throws RemoteException
	{
		return "Name Server response: You are now supposed to get the ip address of machine holding the file you were looking for. \n"
				+ "This string will be replaced by the output of a nethod provided by the NameServerRegistry class.";
	}

}