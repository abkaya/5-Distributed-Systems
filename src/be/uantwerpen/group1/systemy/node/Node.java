package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node
{

	public static void main(String args[]) throws RemoteException
	{

		NameServerInterface nsi = null;
		String remoteNSName = "NameServerInterface";
		String dnsIP = "192.168.1.103";
		String hostnameIP = "Node1,192.168.1.103";
		int dnsPort = 1099;
		int multicastPort = 2000;
		int tcpFileTranferPort = 2001;
		String requestedFile = "HQImage.jpg";

		/*
		 * Assessing one's IP address can become tricky when multiple network interfaces are involved. For instance, I'm getting the APIPA
		 * address 169.254.202.83, which is undesirable. We could work this out in the future, but let's use the manually determined IP
		 * address for now - abdil
		 * 
		 * String myIP = InetAddress.getLocalHost().getHostAddress();
		 */
		MulticastSender.send("234.0.113.0", multicastPort, hostnameIP);

		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);

		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		System.out.println("DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n " + "DNS Server RMI tree map return : "
				+ nsi.getIPAddress(requestedFile));

		//Temporarily using the same node as if it were some other node hosting files
		String host = "192.168.1.103";
		TCP fileServer = new TCP(host, tcpFileTranferPort);
		new Thread(() ->
		{
			fileServer.listenToSendFile();
		}).start();
		
		
		//request the file from the server hosting it, according to the dns server
		TCP fileClient = new TCP(tcpFileTranferPort, nsi.getIPAddress(requestedFile));
		fileClient.receiveFile(requestedFile);
		//As simple as that!
		
	}

}
