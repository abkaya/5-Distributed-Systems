package be.uantwerpen.group1.systemy.nameserver;

import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.MulticastListener;;

public class NameServer implements NameServerInterface
{
	static NameServerRegister nsr = new NameServerRegister(false);

	public static void main(String args[])
	{
		int multicastPort = 2000;
		String[] hostnameIP;

		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>("192.168.1.103", "NameServerInterface", nsi);
		
		
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", multicastPort);
		while (true)
		{
			String multicastMessage = multicastListener.receive().trim();
			hostnameIP = multicastMessage.split(",");
			System.out.println("Received hostname " + hostnameIP[0] + ", IP : " + hostnameIP[1]);
			nsr.addNode(hostnameIP[0], hostnameIP[1]);
		}

	}

	@Override
	public String getIPAddress(String fileName)
	{
		return nsr.getFileLocation(fileName);
	}

}