package be.uantwerpen.group1.systemy.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServer;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node implements NodeInterface {
	private static String logName = Node.class.getName() + " >> ";

	static NodeInfo me = null;
	static NodeInfo nextNode = null;
	static NodeInfo previousNode = null;
	static NameServerInterface nsi = null;
	static ParserXML parserXML = new ParserXML(logName);
	static String dnsIP = parserXML.getDnsIPN();

	static final String HOSTNAME = parserXML.getHostNameN();
	static final int NEIGHBORPORT = parserXML.getNeighborPortN();
	static final int MULTICASTPORT = parserXML.getMulticastPortN();
	static final String REMOTENSNAME = parserXML.getRemoteNsNameN();
	static final int DNSPORT = parserXML.getDnsPortN();
	static final int RMIPORT = 1099;	// TODO: same as DNSPORT
	static final int TCPDNSRETRANSMISSIONPORT = parserXML.getTcpDnsRetransmissionPortN();

	/**
	 * @param args: first argument is the nodeName (optional)
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException, SocketException {

		String IP = null;
		ArrayList<String> IPs = Interface.getIP();
		if (IPs.size() == 1) {
			IP = IPs.get(0);
		} else if (IPs.size() > 1) {
			System.out.println("Choose one of the following IP addresses:");
			for (int i = 0; i < IPs.size(); i++) {
				System.out.println("  (" + i + ") " + IPs.get(i));
			}
			int n = -1;
			Scanner reader = new Scanner(System.in);
			while ( n < 0 || n > IPs.size()-1 ) {
				System.out.print("Enter preferred number: ");
				n = reader.nextInt();
			}
			reader.close();
			IP = IPs.get(n);
		} else {
			SystemyLogger.log(Level.SEVERE, logName + "No usable IP address detected");
			System.exit(-1);
		}
		me = new NodeInfo(HOSTNAME, IP);

		SystemyLogger.log(Level.INFO, logName + "node '" + me.toString() + "' is on " + me.getIP());

		// init skeleton
		NodeInterface ni = new Node();
		RMI<NodeInterface> rmiNode = new RMI<NodeInterface>(me.getIP(), me.getName(), ni);
		
		initShutdownHook();
		listenToNewNodes();
		listenToNeighborRequests();
		startHeartbeat();
		discover();
		
		// init nameserver stub
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nsi = rmi.getStub(nsi, REMOTENSNAME, dnsIP, DNSPORT);

		/*
		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		SystemyLogger.log(Level.INFO, logName + "DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n " + "DNS Server RMI tree map return : "
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
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			SystemyLogger.log(Level.INFO, logName + "shutdown procedure started");
			TCP neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
			neighborSender.sendText("next," + nextNode.toData());
			neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
			neighborSender.sendText("previous," + previousNode.toData());
			try {
				nsi.removeNode(me.getHash());
			} catch (Exception e) {
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			}
			SystemyLogger.log(Level.INFO, logName + "shutdown procedure ended");
		}));
	}

	/**
	 * Send discover request with node data to nameserver
	 */
	private static void discover() {
		// Request
		String Message = me.toData();
		MulticastSender.send("234.0.113.0", MULTICASTPORT, Message);
		SystemyLogger.log(Level.INFO, logName + "Send multicast message: " + Message);
		// Response
		TCP dnsIPReceiver = new TCP(me.getIP(), TCPDNSRETRANSMISSIONPORT);
		dnsIP = dnsIPReceiver.receiveText();
		SystemyLogger.log(Level.INFO, logName + "NameServer is on IP: " + dnsIP);
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
				SystemyLogger.log(Level.INFO, logName + "Received multicast message: " + receivedMulticastMessage);
				String messageComponents[] = receivedMulticastMessage.split(",");
				NodeInfo newNode = new NodeInfo(messageComponents[0], messageComponents[2]);
				//int nodeCount = Integer.parseInt(messageComponents[3]);
				SystemyLogger.log(Level.INFO, logName + "New node! " + newNode.toString() + " at " + newNode.getIP());
				if (nextNode == null || previousNode == null) {
					// no nodes -> point to self
					nextNode = newNode;
					previousNode = newNode;
					SystemyLogger.log(Level.INFO,
							logName + "setting new node (probably myself) as next and previous node");
				} else if (newNode.getHash() == me.getHash()) {
					SystemyLogger.log(Level.INFO, logName + "New node is myself while already having neigbors");
				} else if (nextNode.getHash() == me.getHash()) {
					// pointing to myself -> point in both ways to 2de known node
					nextNode = newNode;
					previousNode = newNode;
					SystemyLogger.log(Level.INFO, logName + "setting 2de as next and previous node");
					TCP neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
					neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				} else if (newNode.isNewNext(me, nextNode)) {
					// New next node
					nextNode = newNode;
					SystemyLogger.log(Level.INFO, logName + "New next node! " + nextNode.toString());
					TCP neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
					neighborSender.sendText("previous," + me.toData());
				} else if (newNode.isNewPrevious(me, previousNode)) {
					// New previous node
					previousNode = newNode;
					SystemyLogger.log(Level.INFO, logName + "New previous node! " + previousNode.toString());
					TCP neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
					neighborSender.sendText("next," + me.toData());
				} else {
					SystemyLogger.log(Level.INFO, logName + "Node is not a (new) neighbor");
				}
				SystemyLogger.log(Level.INFO, logName + "Current situation: " + previousNode.toString() + " | "
						+ me.toString() + " | " + nextNode.toString());
			}
		}).start();
	}

	/**
	 * Listen to incoming TCP requests from neighbor
	 */
	private static void listenToNeighborRequests() {
		new Thread(() -> {
			TCP neighborReceiver = new TCP(me.getIP(), NEIGHBORPORT);
			SystemyLogger.log(Level.INFO, logName + "Listening for neighbors on port " + NEIGHBORPORT);
			while (true) {
				// packet layout "next,name,hash,ip"
				String neighborMessage = neighborReceiver.receiveText();
				SystemyLogger.log(Level.INFO, logName + "Received neighbor packet: " + neighborMessage);
				String[] neighborMessageComponents = neighborMessage.split(",");
				if (neighborMessageComponents[0].equals("next")) {
					nextNode = new NodeInfo(neighborMessageComponents[1], neighborMessageComponents[3]);
					SystemyLogger.log(Level.INFO, logName + "New next node! " + nextNode.toString());
				} else if (neighborMessageComponents[0].equals("previous")) {
					previousNode = new NodeInfo(neighborMessageComponents[1], neighborMessageComponents[3]);
					SystemyLogger.log(Level.INFO, logName + "New previous node! " + previousNode.toString());
				} else {
					SystemyLogger.log(Level.SEVERE,
							logName + "Neighbour package identifier not recognized! " + neighborMessageComponents[0]);
				}
				SystemyLogger.log(Level.INFO, logName + "Current situation: " + previousNode.toString() + " | "
						+ me.toString() + " | " + nextNode.toString());
			}
		}).start();
	}
	
	/**
	 * Thread that checks if neighbors are still alive
	 */
	private static void startHeartbeat() {
		new Thread(() -> {
			while(true) {
				RMI<NodeInterface> rmiNode = new RMI<NodeInterface>();
				if (nextNode != null) {
					// init next node stub
					NodeInterface nextNodeInterface = null;
					nextNodeInterface = rmiNode.getStub(nextNodeInterface, nextNode.getName(), nextNode.getIP(), RMIPORT);
					// ping next node
					try {
						if ( nextNodeInterface.ping() ) {
							// everything ok
						}
					} catch (Exception e) {
						// node not reachable
						int trys = 5;
						boolean response = false;
						while(response == false && trys > 0) {
							try {
								TimeUnit.SECONDS.sleep(1);
							} catch (Exception e2) {
								SystemyLogger.log(Level.SEVERE, logName + "Unable to perform sleep");
							}
							try {
								response = nextNodeInterface.ping();
							} catch (Exception e1) {
								trys--;
							}
						}
						if (response == false) {
							SystemyLogger.log(Level.SEVERE, logName + "Next node lost. Starting recovery.");
							nextFailed();
						}
					}
				}
				if (previousNode != null) {
					// init previous node stub
					NodeInterface previousNodeInterface = null;
					previousNodeInterface = rmiNode.getStub(previousNodeInterface, previousNode.getName(), previousNode.getIP(), RMIPORT);
					// ping previous node 
					try {
						if ( previousNodeInterface.ping() ) {
							// everything ok
						}
					} catch (Exception e) {
						e.printStackTrace();
						// node not reachable
						int trys = 5;
						boolean response = false;
						while(response == false && trys > 0) {
							try {
								TimeUnit.SECONDS.sleep(1);
							} catch (Exception e2) {
								SystemyLogger.log(Level.SEVERE, logName + "Unable to perform sleep");
							}
							try {
								response = previousNodeInterface.ping();
							} catch (Exception e1) {
								trys--;
							}
						}
						if (response == false) {
							SystemyLogger.log(Level.SEVERE, logName + "Previous node lost. Starting recovery.");
							previousFailed();
						}
					}
				}
				// wait for 3 seconds
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (Exception e) {
					SystemyLogger.log(Level.SEVERE, logName + "Unable to perform sleep");
				}
			}
		}).start();
	}
	

	/**
	 * If previous node is failed, replace it in myself by the previous of the failed node and remove the failed node from register
	 */
	public static void previousFailed() {
		try {
			// get new previous node from nameserver
			NodeInfo failedNode = previousNode;
			int newPreviousHash = nsi.getPreviousNode(failedNode.getHash());
			String newPreviousIP = nsi.getNodeIP(newPreviousHash);
			previousNode = new NodeInfo(newPreviousHash, newPreviousIP);
			// send my data to new previous node
			TCP neighborSender = new TCP(NEIGHBORPORT, previousNode.getIP());
			neighborSender.sendText("next," + me.toData());
			// remove failed node from register on nameserver
			nsi.removeNode(failedNode.getHash());
		} catch (RemoteException e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * If next node is failed, replace it in myself by the next of the failed node and remove the failed node from register
	 */
	public static void nextFailed() {
		try {
			// get new next node from nameserver
			NodeInfo failedNode = nextNode;
			int newNextHash = nsi.getNextNode(failedNode.getHash());
			String newNextIP = nsi.getNodeIP(newNextHash);
			nextNode = new NodeInfo(newNextHash, newNextIP);
			// send my data to new next node
			TCP neighborSender = new TCP(NEIGHBORPORT, nextNode.getIP());
			neighborSender.sendText("previous," + me.toData());
			// remove failed node from register on nameserver
			nsi.removeNode(failedNode.getHash());
		} catch (RemoteException e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}
	
	/**
	 * method that returns true over RMI to check if node is still online
	 * @return boolean: true
	 */
	@Override
	public boolean ping() {
		return true;
	}

}
