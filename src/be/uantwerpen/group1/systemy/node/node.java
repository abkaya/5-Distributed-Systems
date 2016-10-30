package be.uantwerpen.group1.systemy.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;

public class Node {

	public static void main(String args[]) {
		
		
		/*
		MulticastSocket s = null;
		
		try {
			// needs to be a multicast address
			InetAddress group = InetAddress.getByName("234.0.113.0");	
			s = new MulticastSocket(1337);
			s.joinGroup(group);
			String message = InetAddress.getLocalHost().getHostAddress();
			byte [] m = message.getBytes();
			DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 1337);
			s.send(messageOut);
			s.leaveGroup(group);
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(s != null)
				s.close(); 
		}*/
		
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
        
        
		
	}
	
}

