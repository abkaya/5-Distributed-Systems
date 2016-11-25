package be.uantwerpen.group1.systemy.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.logging.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node
{
	private static String logName = Node.class.getName() + " >> ";

	static NodeInfo me = null;
	static NodeInfo nextNode = null;
	static NodeInfo previousNode = null;
	static NameServerInterface nsi = null;
	static String dnsIP = null;
	
	static final int NEIGHBORPORT = 2003;
	static final int MULTICASTPORT = 2000;
	
	/**
	 * @param args: first argument is the nodeName (optional)
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException, SocketException
	{
		String remoteNSName = "NameServerInterface";

		me = new NodeInfo();
		me.setIP(Interface.getIP());

		if (args.length != 0) {
			// if nodeName is provided
			me.setName(args[0]);
		} else {
			// else generate one random
			Random random = new Random();
			int number = random.nextInt(1000);
			random = null;
			me.setName("node" + String.format("%03d", number));
		}
		System.out.println("node '" + me.toString() + "' is on " + me.getIP());

		int dnsPort = 1099;
		int tcpFileTranferPort = 2001;
		int tcpDNSRetransmissionPort = 2002;
		String requestedFile = "HQImage.jpg";


		initShutdownHook();
		listenToNewNodes();
		listenToNeighborRequests();
		discover();

		// init RMI
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, remoteNSName, dnsIP, dnsPort);


		/*
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
		*/

		/*
		 * once the DNS IP address is known, the replicator can start and run autonomously.
		 */
		//Replicator rep = new Replicator(me.getIP(), tcpFileTranferPort, dnsIP, dnsPort);
		//rep.run();
	}





	/**
	 * Method creates and starts the shutdown hook to notify neighbors and the nameserver
	 */
	private static void initShutdownHook() {
		Runtime.getRuntime().addShutdownHook( new Thread(() -> {
			System.out.println("shutdown procedure started");
			TCP neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
			neighborSender.sendText("next," + nextNode.toData());
			neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
			neighborSender.sendText("previous," + previousNode.toData());
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
		String Message = me.toData();
		MulticastSender.send("234.0.113.0", MULTICASTPORT, Message);
		System.out.println("Send multicast message: " + Message);
		// Response
		int tcpDNSRetransmissionPort = 2002;
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
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", MULTICASTPORT);
		new Thread(() -> {
			while (true) {
				String receivedMulticastMessage = multicastListener.receive().trim();
				System.out.println("Received multicast message: " + receivedMulticastMessage);
				String messageComponents[] = receivedMulticastMessage.split(",");
				NodeInfo newNode = new NodeInfo(messageComponents[0], messageComponents[2]);
				//int nodeCount = Integer.parseInt(messageComponents[3]);
				System.out.println("New node! " + newNode.toString() + " at " + newNode.getIP());
				if ( nextNode == null || previousNode == null ) {
					// no nodes -> point to self
					nextNode = newNode;
					previousNode = newNode;
					System.out.println("setting new node (probably myself) as next and previous node");
				} else if ( newNode.getHash() == me.getHash() ) {
					System.out.println("New node is myself while already having neigbors");
				} else if ( nextNode.getHash() == me.getHash() ) {
					// pointing to myself -> point in both ways to 2de known node
					nextNode = newNode;
					previousNode = newNode;
					System.out.println("setting 2de as next and previous node");
					TCP neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
					neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				} else if ( newNode.isNewNext(me,nextNode) ) {
					// New next node
					nextNode = newNode;
					System.out.println("New next node! " + nextNode.toString());
					TCP neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
				} else if ( newNode.isNewPrevious(me,previousNode) ) {
					// New previous node
					previousNode = newNode;
					System.out.println("New previous node! " + previousNode.toString());
					TCP neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				} else {
					System.out.println("Node is not a (new) neighbor");
				}
				System.out.println("Current situation: " + previousNode.toString() + " | " + me.toString() + " | " + nextNode.toString() );
			}
		}).start();
	}

	/**
	 * Listen to incoming TCP requests from neighbor
	 */
	private static void listenToNeighborRequests() {
		new Thread(() -> {
			TCP neighborReceiver = new TCP(me.getIP(), NEIGHBORPORT);
			System.out.println("Listening for neighbors on port " + NEIGHBORPORT);
			while(true) {
				// packet layout "next,name,hash,ip"
				String neighborMessage = neighborReceiver.receiveText();
				System.out.println("Received neighbor packet: " + neighborMessage);
				String[] neighborMessageComponents = neighborMessage.split(",");
				if (neighborMessageComponents[0].equals("next")) {
					nextNode = new NodeInfo(neighborMessageComponents[1],neighborMessageComponents[3]);
					System.out.println("New next node! " + nextNode.toString());
				} else if (neighborMessageComponents[0].equals("previous")) {
					previousNode = new NodeInfo(neighborMessageComponents[1],neighborMessageComponents[3]);
					System.out.println("New previous node! " + previousNode.toString());
				} else {
					System.err.println("Neighbour package identifier not recognized! " + neighborMessageComponents[0]);
				}
				System.out.println("Current situation: " + previousNode.toString() + " | " + me.toString() + " | " + nextNode.toString() );
			}
		}).start();
	}


}
