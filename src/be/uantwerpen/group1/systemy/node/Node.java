package be.uantwerpen.group1.systemy.node;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.omg.PortableServer.ServantActivator;

import java.awt.Desktop;
import java.awt.event.HierarchyBoundsAdapter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import be.uantwerpen.group1.systemy.gui.UserInterface;
import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServerInterface;
import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastSender;

public class Node extends UserInterface implements NodeInterface {

	private static String logName = Node.class.getName() + " >> ";

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

	// replicator
	private static Replicator rep = null;

	// fileAgent
	private static FileAgent fileAgent = null;
	private static Boolean fileAgentInNetwork = false;
	private static ObservableList<String> fileList = FXCollections.observableArrayList();
	private static ListChangeListener<String> listChangeListener = null;
	private static String fileToDownload = null;
	private static String fileToDeleteInNetwork = null;
	private static String fileToDeleteLocal = null;

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

		createListChangeListener();
    	fileList.addListener(listChangeListener);

		loadingInitialFiles();
		SystemyLogger.log(Level.INFO, logName + "Local files are loaded into the fileList");

		listenToNewNodes();
		discover();
		initShutdownHook();
		startHeartbeat();

		startGUI();

		try
		{
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		startFileAgent();


		/*
		 * Once the nameserver interface stub is retrieved, the replicator can run autonomously.
		 */
		while (dnsIP == null)
		{
			try
			{
				Thread.sleep(100);
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

	private static void createListChangeListener()
	{
		listChangeListener = new ListChangeListener<String>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> change) {
				if (GUI) {
					while (change.next()) {
                        for (String file : change.getAddedSubList()) {
                        	UserInterface.add(file,true);
                        }
                        for (String file : change.getRemoved()) {
                        	UserInterface.remove(file);
                        }
		            }
				}
			}
    	};
	}

	/**
	 * start GUI if the GUI boolean is set
	 */
	private static void startGUI() {
		if (GUI) {
	        new Thread(() -> {
	        	UserInterface.launch();
	        }).start();
		}
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
				if (fileAgent.getActiveNode() == me.getName())
				{
					fileAgent.setNextNodeInterface(nextNodeInterface);
					fileAgent.wait(2500);

				} else if (fileAgent.getActiveNode() == previousNode.getName())
				{
					fileAgent.wait(2500);
				}
				nextNodeInterface.updatePreviousNode(nextNode);
				previousNodeInterface.updateNextNode(previousNode);
				fileAgent.setNextNodeInterface(nextNodeInterface);
				if (nameServerInterface != null)
					nameServerInterface.removeNode(me.getHash());
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
	 * Removes the remaining files in the download folder of the node
	 * Returns a list of the local files within the relative directory localFiles/
	 * @return ObservableList<String> localFiles
	 */
	public static void loadingInitialFiles()
	{
		File[] files = new File("downloadedFiles/").listFiles();
		for (File f : files)
			f.delete();

		files = new File("localFiles/").listFiles();
		for (File file : files)
		{
			if (file.isFile())
			{
				fileList.add(file.getName());
			}
		}
	}

	private static void startFileAgent()
	{
		new Thread(() ->
		{
			try
			{
				if (nameServerInterface.getRegisterSize() == 1 && !fileAgentInNetwork)
				{
					fileAgent = new FileAgent(nameServerInterface);
					fileAgentInNetwork = true;
					fileAgent.setNodeInterface(myNodeInterface);
					myNodeInterface.passFileAgent(fileAgent);
				}
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Method for deleting file localy
	 * @param fileToDelete: the specified file to delete
	 */
	private static void fileToDeleteLocal(String fileToDelete)
	{
		// TODO Auto-generated method stub
		File[] files = new File("downloadedFiles/").listFiles();
		for (File file : files)
		{
			if (file.getName() == fileToDelete)
			{
				file.delete();
			}
		}

		files = new File("localFiles/").listFiles();
		for (File file : files)
		{
			if (file.getName() == fileToDelete)
			{
				file.delete();
			}
		}

		SystemyLogger.log(Level.INFO, logName + fileToDelete + " is deleted localy");

	}

	@Override
	public void passFileAgent(FileAgent fileAgent) throws RemoteException
	{
		// TODO Auto-generated method stub
		Node.fileAgent = fileAgent;
		fileAgentInNetwork = true;

		// make sure the fileAgent is immediately aware of its own location
		fileAgent.setNodeInterface(myNodeInterface);
		fileAgent.setNextNodeInterface(nextNodeInterface);

		// run the tasks a fileAgent needs to perform
		fileAgent.run();
	}

	/**
	 * Method for updating the file list of the node via RMI
	 */
	@Override
	public void updateFileListNode(ArrayList<String> fileList) throws RemoteException
	{
		Node.fileList = FXCollections.observableArrayList( fileList );
		Node.fileList.addListener(listChangeListener);
	}

	@Override
	public String getHostname() throws RemoteException
	{
		// TODO Auto-generated method stub
		return me.getName();
	}

	@Override
	public String getIPAddress() throws RemoteException
	{
		// TODO Auto-generated method stub
		return me.getIP();
	}

	/**
	 * Method for testing
	 */
	@Override
	public ArrayList<String> getFileListNode() throws RemoteException
	{
		// TODO Auto-generated method stub
		return (ArrayList<String>) fileList;
	}

	@Override
	public String getFileToDownload() throws RemoteException
	{
		// TODO Auto-generated method stub
		return fileToDownload;
	}

	@Override
	public void setFileToDownload(String fileToDownload) throws RemoteException
	{
		Node.fileToDownload = fileToDownload;

	}

	@Override
	public Boolean downloadFile(String fileToDownload, String ipOwner) throws RemoteException
	{
		// TODO Auto-generated method stub

		/**
		 * Hier dient de download methode the komen, de naam van de file en het IP adres van de eigenaar wordt meegeven
		 * een boolean op true zetten indien de file gedownload is
		 */
		return true;
	}

	@Override
	public String getFileToDeleteInNetwork() throws RemoteException
	{
		// TODO Auto-generated method stub
		return fileToDeleteInNetwork;
	}

	/**
     * GUI Callback function for button press "Open"
     * @param fileName: file name of file in question
	 * @throws IOException 
     */
    public static void UIOPen(String fileName) {
    	SystemyLogger.log(Level.INFO, logName + "Open: " + fileName);
		try {
        	//if (local??) {
        		File file = new File(LOCALFILESLOCATION + fileName);
    			Desktop.getDesktop().open(file);
        	//}
        	//else { // remote
        		// download
        		// File file = new File(DOWNLOADEDFILESLOCATION + fileName);
        		// Desktop.getDesktop().open(file). 
        	//}
    	} catch (IOException e) {
    		SystemyLogger.log(Level.SEVERE, logName + "Cant't Open " + fileName + "\n" + e.getMessage());
    	}
    	
    	// TODO
    }

    /**
     * GUI Callback function for button press "Delete"
     * @param fileName: file name of file in question
     */
    public static void UIDelete(String fileName) {
    	SystemyLogger.log(Level.INFO, logName + "Delete: " + fileName);
    	if (GUI) {
    		UserInterface.remove(fileName);
    	}
    	// TODO
    }

    /**
     * GUI Callback function for button press "Delete Local"
     * @param fileName: file name of file in question
     */
    public static void UIDeleteLocal(String fileName) {
    	SystemyLogger.log(Level.INFO, logName + "Delete Local: " + fileName);
		fileToDeleteLocal(fileName);
    }

    /**
     * GUI Callback function for button press "Shutdown"
     */
    public static void UIShutdown() {
		SystemyLogger.log(Level.INFO, logName + "Shuting down node after button press");
    	System.exit(0);
	}

}
