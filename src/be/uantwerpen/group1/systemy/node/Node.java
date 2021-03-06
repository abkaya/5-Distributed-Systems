package be.uantwerpen.group1.systemy.node;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.io.IOException;
import java.net.InetAddress;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node implements NodeInterface
{
	private static String logName = Node.class.getName().replace("be.uantwerpen.group1.systemy.", "") + " >> ";

	private static NodeInfo me = null;
	private static NodeInfo nextNode = null;
	private static NodeInfo previousNode = null;
	private static NameServerInterface nameServerInterface = null;
	private static String dnsIP = null;
	private static String HOSTNAME = ParserXML.parseXML("Hostname");

	private static String nodeIp;
	private static boolean debugMode = false;

	private static final int MULTICASTPORT = Integer.parseInt(ParserXML.parseXML("MulticastPort"));
	private static final String REMOTENSNAME = ParserXML.parseXML("RemoteNsName");
	private static final int RMIPORT = Integer.parseInt(ParserXML.parseXML("RMIPort"));
	private static final Boolean GUI = Boolean.parseBoolean(ParserXML.parseXML("GUI"));
	private static final String LOCALFILESLOCATION = ParserXML.parseXML("localFilesLocation");
	private static final String DOWNLOADEDFILESLOCATION = ParserXML.parseXML("downloadedFilesLocation");
	private static final int TCPFILETRANSFERPORT = Integer.parseInt(ParserXML.parseXML("TcpFileTranferPort"));

	// node RMI interfaces
	private static RMI<NodeInterface> rmiNodeClient = new RMI<NodeInterface>();
	private static NodeInterface myNodeInterface = null;
	private static NodeInterface nextNodeInterface = null;
	private static NodeInterface previousNodeInterface = null;

	private static Replicator rep = null;

	static RMI<NameServerInterface> rmiNameServerInterface = null;

	/**
	 * Constructor for debug purposes only
	 * @param ipAddress_debug: When in debug mode, the ip address is the ip address of the nameserver, and zero otherwise
	 */
	public Node(String nodeIP, boolean debugMode)
	{
		Node.nodeIp = nodeIP;
		Node.debugMode = debugMode;
	}

	/**
	 * Constructor for RMI
	 */
	public Node()
	{
		// empty
	}

	/**
	 * @param args: first argument is the nodeName (optional)
	 * @throws IOException 
	 */
	public static void main(String args[]) throws IOException
	{

		if (!debugMode)
		{
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
		 * Once the nameserver interface stub is retrieved, the replicator can run autonomously.
		 */
		while (dnsIP == null)
		{
			try
			{
				Thread.sleep(1000);
				SystemyLogger.log(Level.INFO, logName + "No response from nameserver: ");
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		SystemyLogger.log(Level.INFO, logName + "REPLICATOR STARTED: ");
		rep = new Replicator(HOSTNAME, me.getIP(), TCPFILETRANSFERPORT, dnsIP, nameServerInterface);
		rep.run();
	}

	/**
	 * Method creates and starts the shutdown hook to notify neighbours and the nameserver
	 */
	private static void initShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			SystemyLogger.log(Level.INFO, logName + "Shutdown procedure started");
			try
			{
				nextNodeInterface.updatePreviousNode(previousNode);
				previousNodeInterface.updateNextNode(nextNode);
				if (nameServerInterface != null)
					nameServerInterface.removeNode(me.getHash());
				nextNodeInterface.replicateLocalFiles();
			} catch (Exception e)
			{
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			}
			SystemyLogger.log(Level.INFO, logName + "Shutdown procedure ended");
		}));
	}

	/**
	 * Send discover request with node data to nameserver
	 */
	private static void discover()
	{
		// Request
		String Message = me.toData();
		MulticastSender.send("234.0.113.0", MULTICASTPORT, Message);
		SystemyLogger.log(Level.INFO, logName + "Send multicast message: " + Message);
	}

	/**
	 * Listen to multicast responses on discover requests
	 * This method creates and starts a thread
	 */
	private static void listenToNewNodes()
	{
		/*
		 * Listen for new nodes
		 */
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", MULTICASTPORT);
		new Thread(() ->
		{
			while (true)
			{
				try
				{
					String receivedMulticastMessage = multicastListener.receive().trim();
					SystemyLogger.log(Level.INFO, logName + "Received multicast message: " + receivedMulticastMessage);
					String messageComponents[] = receivedMulticastMessage.split(",");
					NodeInfo newNode = new NodeInfo(messageComponents[0], messageComponents[2]);
					SystemyLogger.log(Level.INFO, logName + "New node! " + newNode.toString() + " at " + newNode.getIP());

					// When a new node is found, replicate your local files appropriately, whilst adjusting the lists and file records.
					if (rep != null)
						rep.replicateLocalFiles();

					if (nextNode == null || previousNode == null)
					{
						// no nodes -> point to self
						myNodeInterface.updateNextNode(newNode);
						myNodeInterface.updatePreviousNode(newNode);
						SystemyLogger.log(Level.INFO, logName + "setting new node (probably myself) as next and previous node");
					} else if (newNode.getHash() == me.getHash())
					{
						SystemyLogger.log(Level.INFO, logName + "New node is myself while already having neigbors");
					} else if (nextNode.getHash() == me.getHash() && previousNode.getHash() == me.getHash())
					{
						// pointing to myself -> point in both ways to 2nd known node
						myNodeInterface.updateNextNode(newNode);
						myNodeInterface.updatePreviousNode(newNode);
						nextNodeInterface.updatePreviousNode(me);
						previousNodeInterface.updateNextNode(me);
					} else if (newNode.isNewNext(me, nextNode))
					{
						// New next node
						myNodeInterface.updateNextNode(newNode);
						nextNodeInterface.updatePreviousNode(me);
					} else if (newNode.isNewPrevious(me, previousNode))
					{
						// New previous node
						myNodeInterface.updatePreviousNode(newNode);
						previousNodeInterface.updateNextNode(me);
					} else
					{
						SystemyLogger.log(Level.INFO, logName + "Node is not a (new) neighbor");
					}
					SystemyLogger.log(Level.INFO, logName + networkStatus());
				} catch (RemoteException e)
				{
					SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				}
			}
		}).start();
	}

	/**
	 * Thread which checks whether or not neighbours are still alive
	 */
	private static void startHeartbeat()
	{
		new Thread(() ->
		{
			while (true)
			{
				if (nextNode != null)
				{
					// ping next node
					try
					{
						if (!InetAddress.getByName(nextNode.getIP()).isReachable(15))
						{
							SystemyLogger.log(Level.SEVERE, logName + "Next node lost. Starting recovery.");
							nextFailed();
						}
					} catch (Exception e)
					{
						SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
					}
				}
				if (previousNode != null)
				{
					// ping previous node
					try
					{
						if (!InetAddress.getByName(nextNode.getIP()).isReachable(15))
						{
							SystemyLogger.log(Level.SEVERE, logName + "Next node lost. Starting recovery.");
							nextFailed();
						}
					} catch (Exception e)
					{
						SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
					}
				}
				// wait for 3 seconds
				try
				{
					TimeUnit.SECONDS.sleep(3);
				} catch (Exception e)
				{
					SystemyLogger.log(Level.SEVERE, logName + "Unable to perform sleep");
				}
			}
		}).start();
	}

	/**
	 * If previous node has failed, replace this node's previous node by the failed node's previous node and remove the previous node from the name server's registry
	 */
	public static void previousFailed()
	{
		try
		{
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
		} catch (RemoteException e)
		{
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * If next node has failed, replace this node's next node by the failed node's next node and remove the failed node from the name server's registry
	 */
	public static void nextFailed()
	{
		try
		{
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
		} catch (RemoteException e)
		{
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
	public void setDNSIP(String IP)
	{
		dnsIP = IP;
		SystemyLogger.log(Level.INFO, logName + "NameServer is on IP: " + dnsIP);
		rmiNameServerInterface = new RMI<NameServerInterface>();
		nameServerInterface = rmiNameServerInterface.getStub(nameServerInterface, REMOTENSNAME, dnsIP, RMIPORT);
		SystemyLogger.log(Level.INFO, logName + "Created nameserver stub");
	}

	/**
	* Update next node + stub
	*
	* @param newNode: NodeInfo of new next node
	*/
	@Override
	public void updateNextNode(NodeInfo newNode)
	{
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
	public void updatePreviousNode(NodeInfo newNode)
	{
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
	private static String networkStatus()
	{
		String status = "Current situation: ";
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
	 * Method to force a node's replicator to replicate its localFiles again.
	 * Used upon shutdown, forcing the next node to replicate its files to the new previous node. 
	 * This this is done consistently, this current node doesn't need to go through all
	 * its replicated/downloaded files and reconsider new owners. It'll be up to the next node to
	 * do that.
	 */
	@Override
	public void replicateLocalFiles() throws RemoteException
	{
		rep.replicateLocalFiles();
	}

	/**
	 * Method to print the current file records status on the replicator of a node
	 */
	@Override
	public void printFileRecordsStatus()
	{
		rep.printFileRecords();
	}
}