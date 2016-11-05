package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Random;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node
{

	static String dnsIP = null;
	static int myHash = 0;
	static NodeInfo nextNode = null;
	static NodeInfo previousNode = null;
	
	/**
	 * @param args: first argument is the nodeName
	 * @throws RemoteException
	 * @throws UnknownHostException
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException
	{

		NameServerInterface nsi = null;
		String remoteNSName = "NameServerInterface";
		/* this is our IP, we now assume not to have the DNS IP, which we'll receive after retransmission
		 * by the DNS server over a TCP socket.
		 *
		 * Assessing one's IP address can become tricky when multiple network interfaces are involved. For instance, I'm getting the APIPA
		 * address 169.254.202.83, which is undesirable. We could work this out in the future, but let's use the manually determined IP
		 * address for now - abdil
		 * 
		 * I fixed this issue by disabling my virtual adapters from VMWare and VBox - Robin
		 * 
		 * String myIP = InetAddress.getLocalHost().getHostAddress();	// Automatic
		 * String myIP = "192.168.1.103";								// Manual
		 */
		
		String myIP = InetAddress.getLocalHost().getHostAddress();
		String nodeName = null;
		
		if (args.length != 0) {
			// if nodeName is provided
			nodeName = args[0];
		} else {
			// else generate one random
			Random random = new Random();
			int r = random.nextInt(1000);
			random = null;
			nodeName = "node" + r;
		}
		System.out.println("node '" + nodeName + "' is on " + myIP);
		
		/*Don't mind the awful port names. It's just to get everyone acquainted with them*/
		int dnsPort = 1099;
		int sendMulticastPort = 2000;
		int receiveMulticastPort = 2001;
		int tcpFileTranferPort = 2002;
		//int tcpDNSRetransmissionPort = 2003;
		String requestedFile = "HQImage.jpg";

		String multicastMessage = nodeName + "," + myIP;
		MulticastSender.send("234.0.113.0", sendMulticastPort, multicastMessage);

		MulticastListener multicastListener = new MulticastListener("234.0.113.0", receiveMulticastPort);
		
		
		new Thread(() -> {
			while (true) {
				String receivedMulticastMessage = multicastListener.receive().trim();
				System.out.println("Received multicast message: " + receivedMulticastMessage);
				String messageComponents[] = receivedMulticastMessage.split(",");
				dnsIP = messageComponents[0];
				String newNodeName = messageComponents[1];
				int newNodeHash = Integer.parseInt(messageComponents[2]);
				String newNodeIP = messageComponents[3];
				int nodeCount = Integer.parseInt(messageComponents[4]);
				System.out.println("New node! " + newNodeName + " (" + newNodeHash +") at " + newNodeIP + "  total nodes: " + nodeCount);
				
				if (nextNode == null) {
					// no nodes -> point to self
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					myHash = newNodeHash;
					System.out.println("setting myself as next and previous node");
				} else if (nextNode.getHash() == myHash) {
					// pointing to myself -> point in both ways to 2de known node
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					System.out.println("setting 2de as next and previous node");
				} else if ( newNodeHash < nextNode.getHash() && newNodeHash > myHash || newNodeHash < nextNode.getHash() && nextNode.getHash() < myHash || nextNode.getHash() < myHash && newNodeHash >= myHash ) {
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					System.out.println("New next node! " + nextNode.toString());
					// ##################################################
					// TCP to newNode to tell him that we are buddies now
					// ##################################################
				} else if ( newNodeHash > previousNode.getHash() && newNodeHash < myHash || newNodeHash > previousNode.getHash() && previousNode.getHash() > myHash || previousNode.getHash() > myHash && newNodeHash <= myHash ) {
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeName);
					System.out.println("New previous node! " + previousNode.toString());
					// ##################################################
					// TCP to newNode to tell him that we are buddies now
					// ##################################################
				}
				
			}
		}).start();
		
		/*
		 * Now we imagine we don't have a clue what the DNS IP is, and hope for TCP retransmission
		 * to get ahold of the DNS server's IP. We'll await for the DNS server to get back at us
		 * and continue with RMI once we get it.
		*/
		//TCP dnsIPReceiver = new TCP(myIP, tcpDNSRetransmissionPort);
		//dnsIP = dnsIPReceiver.receiveText();
		
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);
		
		/*
		 * TESTCODE get border node's
		 */
		//nextNode = nsi.getNextNode();
		//System.out.println("Found next node at " + nextNode);
		//previousNode = nsi.getPreviousNode();
		//System.out.println("Found previous node at " + previousNode);
		
		
		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		System.out.println("DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n " + "DNS Server RMI tree map return : "
				+ nsi.getIPAddress(requestedFile));

		//Temporarily using the same node as if it were some other node hosting files
		
		TCP fileServer = new TCP(myIP, tcpFileTranferPort);
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
