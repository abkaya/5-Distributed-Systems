package be.uantwerpen.group1.systemy.nameserver;

import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastListener;;

public class NameServer implements NameServerInterface
{
	static NameServerRegister nsr = new NameServerRegister(false);

	public static void main(String args[])
	{
		String nameServerIP =  "192.168.1.103";
		int multicastPort = 2000;
		int tcpDNSRetransmissionPort = 2002;
		

		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>(nameServerIP, "NameServerInterface", nsi);
		
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", multicastPort);
		new Thread(() ->
		{
		while (true)
		{
			String[] hostnameIP;
			String multicastMessage = multicastListener.receive().trim();
			hostnameIP = multicastMessage.split(",");
			System.out.println("Received hostname " + hostnameIP[0] + ", IP : " + hostnameIP[1]);
			nsr.addNode(hostnameIP[0], hostnameIP[1]);
			/*After having received the IP address of a new node, we need to send it a reply,
			 * letting it know what our DNS server IP is. He got to us, but it doesn't know
			 * our IP yet. That what this retransmission is about.
			 */
			TCP dnsIPRetransmission = new TCP(tcpDNSRetransmissionPort, hostnameIP[1]);
			dnsIPRetransmission.sendText(nameServerIP);
		}
		}).start();
		
	}

	@Override
	public String getIPAddress(String fileName)
	{
		return nsr.getFileLocation(fileName);
	}

}