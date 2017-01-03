package be.uantwerpen.group1.systemy.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.net.InetAddress;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node implements NodeInterface {
	private static String logName = Node.class.getName() + " >> ";

	private static NodeInfo me = null;
	private static NodeInfo nextNode = null;
	private static NodeInfo previousNode = null;
	private static FileAgent fileAgent = null;
	private static NameServerInterface nameServerInterface = null;
	private static String dnsIP = ParserXML.parseXML("DnsIp");
	private static String HOSTNAME = ParserXML.parseXML("Hostname");

	private static String nodeIp;
	private static boolean debugMode = false;

	private static final int MULTICASTPORT = Integer.parseInt(ParserXML.parseXML("MulticastPort"));
	private static final String REMOTENSNAME = ParserXML.parseXML("RemoteNsName");
	private static final int RMIPORT = Integer.parseInt(ParserXML.parseXML("RMIPort"));
	private static final Boolean GUI = Boolean.parseBoolean(ParserXML.parseXML("GUI"));
	private static final String LOCALFILESLOCATION = ParserXML.parseXML("localFilesLocation");
	private static final String DOWNLOADEDFILESLOCATION = ParserXML.parseXML("downloadedFilesLocation");
	
	private static HashMap<String, String> fileList;
	private static String fileToDownload = null;
	private static boolean downloadCompleted = false;

	// node RMI interfaces
	private static RMI<NodeInterface> rmiNodeClient = new RMI<NodeInterface>();
	private static NodeInterface myNodeInterface = null;
	private static NodeInterface nextNodeInterface = null;
	private static NodeInterface previousNodeInterface = null;

	/**
	 * Constructor only for debug purposes
	 * @param ipAddress_debug: if where in debug mode, the ipaddress is the ipaddress for the nameserver, otherwise it's zero
	 */
	public Node(String nodeIP, boolean debugMode) {
		Node.nodeIp = nodeIP;
		Node.debugMode = debugMode;
	}

	/**
	 * Constructor for RMI
	 */
	public Node() {
		// empty
	}

	/**
	 * @param args: first argument is the nodeName (optional)
	 * @throws RemoteException
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public static void main(String args[]) throws RemoteException, UnknownHostException, SocketException {

		if (!debugMode) {
			nodeIp = Interface.getIP();
		}

		me = new NodeInfo(HOSTNAME, nodeIp);

		SystemyLogger.log(Level.INFO, logName + "node '" + me.toString() + "' is on " + me.getIP());

		// init skeleton
		NodeInterface ni = new Node();
		RMI<NodeInterface> rmiNode = new RMI<NodeInterface>(me.getIP(), "node", ni);

		// init loopback interface
		myNodeInterface = rmiNodeClient.getStub(myNodeInterface, "node", me.getIP(), RMIPORT);
		SystemyLogger.log(Level.INFO, logName + "Created own loopback RMI interface");

		listenToNewNodes();
		discover();
		initShutdownHook();
		startHeartbeat();



		/*
		// test to see whether our RMI class does its job properly. Spoiler alert: it does.
		SystemyLogger.log(Level.INFO, logName + "DNS RMI IP address request for machine hosting file: 'HQImage.jpg' \n " + "DNS Server RMI tree map return : "
				+ nameServerInterface.getIPAddress(requestedFile));

		//Temporarily using the same node as if it were some other node hosting files

		TCP fileServer = new TCP(me.getIP(), tcpFileTranferPort);
		new Thread(() ->
		{

			fileServer.listenToSendFile();
		}).start();


		//request the file from the server hosting it, according to the dns server
		TCP fileClient = new TCP(tcpFileTranferPort, nameServerInterface.getIPAddress(requestedFile));
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
			SystemyLogger.log(Level.INFO, logName + "Shutdown procedure started");
			try {
				nextNodeInterface.updatePreviousNode(nextNode);
				previousNodeInterface.updateNextNode(previousNode);
				nameServerInterface.removeNode(me.getHash());
			} catch (Exception e) {
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			}
			SystemyLogger.log(Level.INFO, logName + "Shutdown procedure ended");
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
				try {
					String receivedMulticastMessage = multicastListener.receive().trim();
					SystemyLogger.log(Level.INFO, logName + "Received multicast message: " + receivedMulticastMessage);
					String messageComponents[] = receivedMulticastMessage.split(",");
					NodeInfo newNode = new NodeInfo(messageComponents[0], messageComponents[2]);
					SystemyLogger.log(Level.INFO, logName + "New node! " + newNode.toString() + " at " + newNode.getIP());
					if (nextNode == null || previousNode == null) {
						// no nodes -> point to self
						myNodeInterface.updateNextNode(newNode);
						myNodeInterface.updatePreviousNode(newNode);
						SystemyLogger.log(Level.INFO, logName + "setting new node (probably myself) as next and previous node");
					} else if (newNode.getHash() == me.getHash()) {
						SystemyLogger.log(Level.INFO, logName + "New node is myself while already having neigbors");
					} else if (nextNode.getHash() == me.getHash() && previousNode.getHash() == me.getHash()) {
						// pointing to myself -> point in both ways to 2de known node
						myNodeInterface.updateNextNode(newNode);
						myNodeInterface.updatePreviousNode(newNode);
						nextNodeInterface.updatePreviousNode(me);
						previousNodeInterface.updateNextNode(me);
					} else if (newNode.isNewNext(me, nextNode)) {
						// New next node
						myNodeInterface.updateNextNode(newNode);
						nextNodeInterface.updatePreviousNode(me);
					} else if (newNode.isNewPrevious(me, previousNode)) {
						// New previous node
						myNodeInterface.updatePreviousNode(newNode);
						previousNodeInterface.updateNextNode(me);
					} else {
						SystemyLogger.log(Level.INFO, logName + "Node is not a (new) neighbor");
					}
					SystemyLogger.log(Level.INFO, logName + networkStatus());
				} catch(RemoteException e) {
					SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Thread that checks if neighbors are still alive
	 */
	private static void startHeartbeat() {
		new Thread(() -> {
			while(true) {
				if (nextNode != null) {
					// ping next node
					try {
						if( !InetAddress.getByName(nextNode.getIP()).isReachable(5) ) {
							SystemyLogger.log(Level.SEVERE, logName + "Next node lost. Starting recovery.");
							nextFailed();
						}
					} catch (Exception e) {
						SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
					}
				}
				if (previousNode != null) {
					// ping previous node
					try {
						if( !InetAddress.getByName(nextNode.getIP()).isReachable(5) ) {
							SystemyLogger.log(Level.SEVERE, logName + "Next node lost. Starting recovery.");
							nextFailed();
						}
					} catch (Exception e) {
						SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
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
	 * This method will start a thread for passing the fileAgent to the other nodes. 
	 * When the fileAgent is finished with his job on the currentNode a boolean will be high and
	 * fileList on the node can be updated and the 
	 */
	private static void passFileAgentInNetwork()
	{
		new Thread(() ->
		{
			while (true)
			{
				// if fileAgent is finished
				if (fileAgent.isAgentFinished())
				{
					// update the filelist of the node when the fileAgent finished
					fileList = fileAgent.getUpdatedFileListNode();

					// pass the FileAgent to the nextNode && uodate the interface 
					/**
					 * ROBIN, ABDIL?
					 */

					// update the fileAgent with information from the next node


				}

			}

		}).start();
	}


	/**
	 * If previous node is failed, replace it in myself by the previous of the failed node and remove the failed node from register
	 */
	public static void previousFailed() {
		try {
			NodeInfo failedNode = previousNode;
			// get new previous node from nameserver
			int newPreviousHash = nameServerInterface.getPreviousNode(failedNode.getHash());
			String newPreviousIP = nameServerInterface.getNodeIP(newPreviousHash);
			NodeInfo newPreviousNode = new NodeInfo(newPreviousHash, newPreviousIP);
			myNodeInterface.updatePreviousNode(newPreviousNode);
			// send my data to new previous node
			previousNodeInterface.updateNextNode(me);
			// remove failed node from register on nameserver
			nameServerInterface.removeNode(failedNode.getHash());
		} catch (RemoteException e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * If next node is failed, replace it in myself by the next of the failed node and remove the failed node from register
	 */
	public static void nextFailed() {
		try {
			NodeInfo failedNode = nextNode;
			// get new next node from nameserver
			int newNextHash = nameServerInterface.getNextNode(failedNode.getHash());
			String newNextIP = nameServerInterface.getNodeIP(newNextHash);
			NodeInfo newNextNode = new NodeInfo(newNextHash, newNextIP);
			myNodeInterface.updateNextNode(newNextNode);
			// send my data to new next node
			nextNodeInterface.updatePreviousNode(me);
			// remove failed node from register on nameserver
			nameServerInterface.removeNode(failedNode.getHash());
		} catch (RemoteException e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * Method to set the DNS IP after sending a discovery multicast
	 * + creating nameserver stub
	 *
	 * @param IP: new NS IP
	 */
	@Override
	public void setDNSIP(String IP) {
		dnsIP = IP;
		SystemyLogger.log(Level.INFO, logName + "NameServer is on IP: " + dnsIP);
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>();
		nameServerInterface = rmi.getStub(nameServerInterface, REMOTENSNAME, dnsIP, RMIPORT);
		SystemyLogger.log(Level.INFO, logName + "Created nameserver stub");
	}

	/**
	* Update next node + stub
	*
	* @param newNode: NodeInfo of new next node
	*/
	@Override
	public void updateNextNode(NodeInfo newNode) {
		nextNode = newNode;
		nextNodeInterface = rmiNodeClient.getStub(nextNodeInterface, "node", nextNode.getIP(), RMIPORT);
		SystemyLogger.log(Level.INFO, logName + "New next node " + nextNode.toString());
		SystemyLogger.log(Level.INFO, logName + networkStatus());
	}

	/**
	* Update previous node + stub
	*
	* @param newNode: NodeInfo of new previous node
	*/
	@Override
	public void updatePreviousNode(NodeInfo newNode) {
		previousNode = newNode;
		previousNodeInterface = rmiNodeClient.getStub(previousNodeInterface, "node", previousNode.getIP(), RMIPORT);
		SystemyLogger.log(Level.INFO, logName + "New previous node " + previousNode.toString());
		SystemyLogger.log(Level.INFO, logName + networkStatus());
	}

	/**
	 * Method that returns status message of local network topology
	 * 
	 * @return String: status message
	 */
	private static String networkStatus() {
		String status =  "Current situation: ";
		if (previousNode != null)
			status += previousNode.toString();
		else
			status += "none";
		status += " | ";
		if (me != null)
			status += me.toString();
		else
			status += "none";
		status += " | ";
		if (nextNode != null)
			status += nextNode.toString();
		else
			status += "none";
		return status;
	}
	
	/**
	 * This method will lock the requested file
	 * @param fileToLock: the name of the file for locking
	 */
	public static void fileToLock(String fileToLock)
	{
		
		if (fileList.containsKey(fileToLock) && fileList.get(fileToLock).equals("notLocked")) {
			fileList.replace(fileToLock, "notLocked", "lockRequest");
		} else {
			SystemyLogger.log(Level.INFO, "file doesn't exist in list or there is already a lock by the current Node");
		}
		
	}
	
	/**
	 * This method will download the file and 
	 */
	public static void downloadFile()
	{
		
		
		
	}

	@Override
	public ArrayList<String> getCurrentNodeOwner() throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, String> getFileListNode() throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getFileToDownload() throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void passFileAgent(FileAgent fileAgent) throws RemoteException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateFileListNode(HashMap<String, String> fileList) throws RemoteException
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getHostname() throws RemoteException
	{
		// TODO Auto-generated method stub
		return me.getName();
	}

	@Override
	public boolean downloadCompleted() throws RemoteException
	{
		// TODO Auto-generated method stub
		return downloadCompleted;
	}
}