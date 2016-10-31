package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node {

	public static void main(String args[]) {
		
		try {
			String myIP = InetAddress.getLocalHost().getHostAddress();
			MulticastSender.send("234.0.113.0", 1337, myIP);
			
			NameServerInterface nsi= null;
			String name = "NameServerInterface";
			RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
			Registry registry = rmi.getRegistry("localhost");
	        try
			{
				nsi = (NameServerInterface) registry.lookup(name);
			} catch (RemoteException | NotBoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        try
			{
	        	//test to see whether our RMI class does its job properly. Spoiler alert: it does.
				System.out.println(nsi.getIPAddress("filename"));
			} catch (RemoteException e)
			{
				e.printStackTrace();
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}  
		
	}
	
}

