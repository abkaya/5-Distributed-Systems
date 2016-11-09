package be.uantwerpen.group1.systemy.node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
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
	static String dnsIP = null;
	
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
		
		// define some important stuff
		int dnsPort = 1099;
//		int sendMulticastPort = 2000;
//		int receiveMulticastPort = 2001;
		int tcpFileTranferPort = 2002;
//		int tcpDNSRetransmissionPort = 2003;
		String requestedFile = "HQImage.jpg";

		// init RMI
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);
		

		initShutdownHook();
		discover();
		listenToNewNodes();
		listenToNeighborRequests();

		
		
		
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
	
	
	
	
	
	/**
	 * Method creates and starts the shutdown hook to notify neighbors and the nameserver
	 */
	private static void initShutdownHook() {
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
	}
	
	/**
	 * Send discover request with node data to nameserver
	 */
	private static void discover() {
		// Request
		int sendMulticastPort = 2000;
		String Message = me.toData();
		MulticastSender.send("234.0.113.0", sendMulticastPort, Message);
		// Response
		int tcpDNSRetransmissionPort = 2003;
		TCP dnsIPReceiver = new TCP(me.getIP(), tcpDNSRetransmissionPort);
		dnsIP = dnsIPReceiver.receiveText();
		System.out.println("NameServer is on IP: " + dnsIP);
	}
	
	/**
	 * Listen to multicast responses on discover requests
	 * This method creates and starts a thread
	 */
	private static void listenToNewNodes() {
		/*
		 * Listen for new nodes
		 */
		int receiveMulticastPort = 2001;
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", receiveMulticastPort);
		new Thread(() -> {
			while (true) {
				String receivedMulticastMessage = multicastListener.receive().trim();
				System.out.println("Received multicast message: " + receivedMulticastMessage);
				String messageComponents[] = receivedMulticastMessage.split(",");
				NodeInfo newNode = new NodeInfo(messageComponents[0], Integer.parseInt(messageComponents[1]), messageComponents[2]);
				int nodeCount = Integer.parseInt(messageComponents[3]);
				System.out.println("New node! " + newNode.toString() + " at " + newNode.getIP() + "  total nodes: " + nodeCount);
				if (nextNode == null) {
					// no nodes -> point to self
					nextNode = newNode;
					previousNode = newNode;
					me.setHash(newNode.getHash());
					System.out.println("setting myself as next and previous node");
				} else if ( nextNode.getHash() == me.getHash() ) {
					// pointing to myself -> point in both ways to 2de known node
					nextNode = newNode;
					previousNode = newNode;
					System.out.println("setting 2de as next and previous node");
				} else if ( newNode.isNewNext(me,nextNode) ) {
					// New next node
					nextNode = newNode;
					System.out.println("New next node! " + nextNode.toString());
					TCP neighborSender = new TCP(nextNode.getPort(), nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
				} else if ( newNode.isNewPrevious(me,previousNode) ) {
					// New previous node
					previousNode = newNode;
					System.out.println("New previous node! " + previousNode.toString());
					TCP neighborSender = new TCP(previousNode.getPort(), previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				}
				
			}
		}).start();
	}
	
	/**
	 * Listen to incoming TCP requests from neighbor
	 */
	private static void listenToNeighborRequests() {
		new Thread(() -> {
			while( me.getHash() == 0 ) {
				
			}
			TCP neighborReceiver = new TCP(me.getIP(), me.getPort());
			System.out.println("Listening for neighbors on port " + me.getPort());
			while(true) {
				// packet layout "next,name,hash,ip"
				String neighborMessage = neighborReceiver.receiveText();
				System.out.println("Received neighbor packet: " + neighborMessage);
				String[] neighborMessageComponents = neighborMessage.split(",");
				if (neighborMessageComponents[0].equals("next")) {
					nextNode = new NodeInfo(neighborMessageComponents[1],Integer.parseInt(neighborMessageComponents[2]),neighborMessageComponents[3]);
					System.out.println("New next node! " + nextNode.toString());
				} else if (neighborMessageComponents[0].equals("previous")) {					
					previousNode = new NodeInfo(neighborMessageComponents[1],Integer.parseInt(neighborMessageComponents[2]),neighborMessageComponents[3]);
					System.out.println("New previous node! " + previousNode.toString());
				} else {
					System.err.println("Neighbour package identifier not recognized! " + neighborMessageComponents[0]);
				}
			}
		}).start();
	}

	
}
