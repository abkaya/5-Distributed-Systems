package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node
{

	public static void main(String args[])
	{

		NameServerInterface nsi = null;
		String remoteNSName = "NameServerInterface";
		String dnsIP = "192.168.1.103";
		String myIP = "192.168.1.103";
		int dnsPort = 1099;

		/*
		 * Assessing one's IP address can become tricky when multiple network
		 * interfaces are involved. For instance, I'm getting the APIPA address 169.254.202.83,
		 * which is undesirable. We could work this out in the future, but let's
		 * use the manually determined IP address for now - abdil
		 * 
		 * String myIP = InetAddress.getLocalHost().getHostAddress();
		 */
		MulticastSender.send("234.0.113.0", 1337, myIP);

		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);

		try
		{
			// test to see whether our RMI class does its job properly. Spoiler alert: it does.
			System.out.println(nsi.getIPAddress("filename"));
		} catch (RemoteException e)
		{
			e.printStackTrace();
		}
	}

}
