package be.uantwerpen.group1.systemy.node;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import be.uantwerpen.group1.systemy.logging.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node implements NodeInterface
{	
	private static String logName = Node.class.getName() + " >> ";

	public static void main(String args[]) throws RemoteException
	{
		
		/*
		 * Concerning the Files this node owns and the replication of these files: How to keep a list of files. Keep two lists? Files owned
		 * and filesHeld. Remove files?
		 */
		
		NameServerInterface nsi = null;
		String remoteNSName = "NameServerInterface";
		/*
		 * this is our IP, we now assume not to have the DNS IP, which we'll receive after retransmission by the DNS server over a TCP
		 * socket.
		 */
		String nodeIP = "192.168.1.101";
		String dnsIP = null;
		String hostnameIP = "Node1,192.168.1.101";

		/* Don't mind the awful port names. It's just to get everyone acquainted with them */
		int dnsPort = 1099;
		int multicastPort = 2000;
		int tcpFileTranferPort = 2001;
		int tcpDNSRetransmissionPort = 2002;
		String requestedFile = "HQImage.jpg";

		/*
		 * Assessing one's IP address can become tricky when multiple network interfaces are involved. For instance, I'm getting the APIPA
		 * address 169.254.202.83, which is undesirable. We could work this out in the future, but let's use the manually determined IP
		 * address for now
		 * String myIP = InetAddress.getLocalHost().getHostAddress();
		 */
		MulticastSender.send("234.0.113.0", multicastPort, hostnameIP);

		/*
		 * Now we imagine we don't have a clue what the DNS IP is, and hope for TCP retransmission to get ahold of the DNS server's IP.
		 * We'll await for the DNS server to get back at us and continue with RMI once we get it.
		 */
		TCP dnsIPReceiver = new TCP(nodeIP, tcpDNSRetransmissionPort);
		dnsIP = dnsIPReceiver.receiveText();

		/*
		 * Once the DNS IP address is known, RMI on the nameserver can commence
		 */
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);
		
		/*
		 * once the DNS IP address is known, the replicator can start and run autonomously.
		 */
		Replicator rep = new Replicator(nodeIP, tcpFileTranferPort, dnsIP, dnsPort);
		rep.run();

		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		SystemyLogger.log(Level.INFO, logName + "DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n "
				+ "DNS Server RMI tree map return : " + nsi.getFileLocation(rep.hash(requestedFile)));
		// System.out.println("DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n "
		// + "DNS Server RMI tree map return : " + nsi.getIPAddress(requestedFile));


		// request the file from the server hosting it, according to the dns server
		TCP fileClient = new TCP(tcpFileTranferPort, nsi.getFileLocation(rep.hash(requestedFile)));
		fileClient.receiveFile(requestedFile);
	}

}
