package be.uantwerpen.group1.systemy.nameserver;

import java.net.SocketException;
import java.net.UnknownHostException;

import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastSender;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastListener;

public class NameServer implements NameServerInterface
{
	static NameServerRegister nsr = new NameServerRegister(false);
	static String nameServerIP = null;
	
	public static void main(String args[]) throws UnknownHostException, SocketException
	{
		
		nameServerIP = Interface.getIP();
		
		int receiveMulticastPort = 2000;
		int sendMulticastPort = 2001;
		int tcpDNSRetransmissionPort = 2003;
		
		System.out.println("NameServer started on " + nameServerIP);

		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>(nameServerIP, "NameServerInterface", nsi);
		
		
		/*
		 * New nodes will apply to join the multicast group, be added to the DNS registry,
		 * and will receive the DNS IP through TCP retransmission
		 */
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", receiveMulticastPort);
		new Thread(() ->
		{
		while (true)
		{
			String[] hostnameIP;
			String multicastMessage = multicastListener.receive().trim();
			System.out.println("Received multicast message: " + multicastMessage);
			hostnameIP = multicastMessage.split(",");
			String hostName = hostnameIP[0];
			String hostIP = hostnameIP[2];
			System.out.println("Received hostname " + hostName + ", IP : " + hostIP);
			nsr.addNode(hostName, hostIP);
			/*After having received the IP address of a new node, we need to send it a reply,
			 * letting it know what our DNS server IP is. He got to us, but it doesn't know
			 * our IP yet. That what this retransmission is about.
			 */
			TCP dnsIPRetransmission = new TCP(tcpDNSRetransmissionPort, hostIP);
			dnsIPRetransmission.sendText(nameServerIP);
			// Multicast data about new node
			multicastMessage = hostName + "," + nsr.hashing(hostName) + "," + hostIP + "," + nsr.getSize();
			MulticastSender.send("234.0.113.0", sendMulticastPort, multicastMessage);
		}
		}).start();
		
	}

	@Override
	public String getIPAddress(String fileName) {
		return nsr.getFileLocation(fileName);
	}
	
	@Override
	public void removeNode(int hash) {
		nsr.removeNodeFromRegister(hash);
	}

}