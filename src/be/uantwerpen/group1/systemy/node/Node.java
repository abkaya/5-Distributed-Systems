package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Random;

import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node
{

	static NodeInfo me = null;
	static NodeInfo nextNode = null;
	static NodeInfo previousNode = null;
	static NameServerInterface nsi = null;
	
	
	/**
	 * @param args: first argument is the nodeName
	 * @throws RemoteException
	 * @throws UnknownHostException
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException
	{
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
		 * me.setIP(InetAddress.getLocalHost().getHostAddress());	// Automatic
		 * me.setIP("192.168.1.103");								// Manual
		 */
		
		me = new NodeInfo();
		me.setIP(InetAddress.getLocalHost().getHostAddress());
		String dnsIP = null;
		
		if (args.length != 0) {
			// if nodeName is provided
			me.setName(args[0]);
		} else {
			// else generate one random
			Random random = new Random();
			int r = random.nextInt(1000);
			random = null;
			me.setName("node" + r);
		}
		System.out.println("node '" + me.getName() + "' is on " + me.getIP());
		
		/*Don't mind the awful port names. It's just to get everyone acquainted with them*/
		int dnsPort = 1099;
		int sendMulticastPort = 2000;
		int receiveMulticastPort = 2001;
		int tcpFileTranferPort = 2002;
		int tcpDNSRetransmissionPort = 2003;
		String requestedFile = "HQImage.jpg";

		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);
		
		String multicastMessage = me.toData();
		MulticastSender.send("234.0.113.0", sendMulticastPort, multicastMessage);

		MulticastListener multicastListener = new MulticastListener("234.0.113.0", receiveMulticastPort);
		
		/*
		 * Listen for new nodes
		 */
		new Thread(() -> {
			while (true) {
				String receivedMulticastMessage = multicastListener.receive().trim();
				System.out.println("Received multicast message: " + receivedMulticastMessage);
				String messageComponents[] = receivedMulticastMessage.split(",");
				String newNodeName = messageComponents[0];
				int newNodeHash = Integer.parseInt(messageComponents[1]);
				String newNodeIP = messageComponents[2];
				int nodeCount = Integer.parseInt(messageComponents[3]);
				System.out.println("New node! " + newNodeName + " (" + newNodeHash +") at " + newNodeIP + "  total nodes: " + nodeCount);
				
				if (nextNode == null) {
					// no nodes -> point to self
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					me.setHash(newNodeHash);
					System.out.println("setting myself as next and previous node");
				} else if (nextNode.getHash() == me.getHash()) {
					// pointing to myself -> point in both ways to 2de known node
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					System.out.println("setting 2de as next and previous node");
				} else if ( newNodeHash < nextNode.getHash() && newNodeHash > me.getHash() || newNodeHash < nextNode.getHash() && nextNode.getHash() < me.getHash() || nextNode.getHash() < me.getHash() && newNodeHash >= me.getHash() ) {
					// New next node
					nextNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					System.out.println("New next node! " + nextNode.toString());
					TCP neighborSender = new TCP(nextNode.getPort(), nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
				} else if ( newNodeHash > previousNode.getHash() && newNodeHash < me.getHash() || newNodeHash > previousNode.getHash() && previousNode.getHash() > me.getHash() || previousNode.getHash() > me.getHash() && newNodeHash <= me.getHash() ) {
					// New previous node
					previousNode = new NodeInfo(newNodeName, newNodeHash, newNodeIP);
					System.out.println("New previous node! " + previousNode.toString());
					TCP neighborSender = new TCP(previousNode.getPort(), previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				}
			}
		}).start();
		
		/*
		 * Listen for new neigbour request
		 */
		new Thread(() -> {
			TCP neighborReceiver = new TCP(me.getIP(), me.getPort());
			System.out.println("Listening for neighbours on port " + me.getPort());
			while(true) {
				// packet layout "next,name,hash,ip"
				String neighbourMessage = neighborReceiver.receiveText();
				System.out.println("Received neighbour packet: " + neighbourMessage);
				String[] messageComponents = neighbourMessage.split(",");
				if (messageComponents[0].equals("next")) {
					nextNode = new NodeInfo(messageComponents[1],Integer.parseInt(messageComponents[2]),messageComponents[3]);
					System.out.println("New next node! " + nextNode.toString());
				} else if (messageComponents[0].equals("previous")) {					
					previousNode = new NodeInfo(messageComponents[1],Integer.parseInt(messageComponents[2]),messageComponents[3]);
					System.out.println("New previous node! " + previousNode.toString());
				} else {
					System.err.println("Neighbour package identifier not recognized! " + messageComponents[0]);
				}
			}
		}).start();
		
		/*
		 * Shutdown hook
		 */
		Runtime.getRuntime().addShutdownHook( new Thread(() -> {
			System.out.println("shutdown procedure started");
			TCP neighborSender = new TCP(previousNode.getPort(), previousNode.getIP());
			neighborSender.sendText("next," + nextNode.toData());
			neighborSender = new TCP(nextNode.getPort(), nextNode.getIP());
			neighborSender.sendText("next," + previousNode.toData());
			try {
				nsi.removeNode(me.getHash());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("shutdown procedure ended");
		}) );
		
		/*
		 * Now we imagine we don't have a clue what the DNS IP is, and hope for TCP retransmission
		 * to get ahold of the DNS server's IP. We'll await for the DNS server to get back at us
		 * and continue with RMI once we get it.
		*/
		TCP dnsIPReceiver = new TCP(me.getIP(), tcpDNSRetransmissionPort);
		dnsIP = dnsIPReceiver.receiveText();
		System.out.println("NameServer is on IP: " + dnsIP);
		
		
		
		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		System.out.println("DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n " + "DNS Server RMI tree map return : "
				+ nsi.getIPAddress(requestedFile));

		//Temporarily using the same node as if it were some other node hosting files
		
		TCP fileServer = new TCP(me.getIP(), tcpFileTranferPort);
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
