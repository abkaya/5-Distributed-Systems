package be.uantwerpen.group1.systemy.nameserver;

import java.net.SocketException;
import java.net.UnknownHostException;

import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.node.NodeInterface;
import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

import java.util.logging.Level;

public class NameServer implements NameServerInterface {

	private static String logName = NameServer.class.getName().replace("be.uantwerpen.group1.systemy.", "") + " >> ";
	
	private static final int MULTICASTPORT = Integer.parseInt(ParserXML.parseXML("MulticastPort"));
	private static final int RMIPORT = Integer.parseInt(ParserXML.parseXML("RMIPort"));
	private static final String MULTICASTIP = ParserXML.parseXML("MulticastIp");
	private static final String REMOTENSNAME = ParserXML.parseXML("RemoteNsName");
	private static String nameServerIp;
	private static boolean debugMode = false;

	private static final NameServerRegister nsr = new NameServerRegister();

	/**
	 * Constructor only for debug purposes
	 * @param ipAddress_debug: if where in debug mode, the ipaddress is the ipaddress for the nameserver, otherwise it's zero
	 */
	public NameServer(String nameServerIP, boolean debugMode) {
		NameServer.nameServerIp = nameServerIP;
		NameServer.debugMode = debugMode;
	}

	/**
	 * Constructor for RMI
	 */
	public NameServer() {
		// empty
	}

	public static void main(String args[]) throws UnknownHostException, SocketException {

		if (!debugMode) {
			nameServerIp = Interface.getIP();
		}

		SystemyLogger.log(Level.INFO, logName + "NameServer started on " + nameServerIp);

		// create RMI skeleton
		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>(nameServerIp, REMOTENSNAME, nsi);


		/*
		 * New nodes will apply to join the multicast group, be added to the DNS registry,
		 * and will receive the DNS IP through TCP retransmission
		 */
		new Thread(() -> {
			MulticastListener multicastListener = new MulticastListener(MULTICASTIP, MULTICASTPORT);
			RMI<NodeInterface> rmiNode = new RMI<NodeInterface>();
			NodeInterface nodeInterface = null;
			while (true) {
				String[] hostnameIP;
				String multicastMessage = multicastListener.receive().trim();
				SystemyLogger.log(Level.INFO, logName + "Received multicast message: " + multicastMessage);
				hostnameIP = multicastMessage.split(",");
				String hostName = hostnameIP[0];
				String hostIP = hostnameIP[2];
				SystemyLogger.log(Level.INFO, logName + "Received hostname " + hostName + ", IP : " + hostIP);
				nsr.addNode(hostName, hostIP);
				/*After having received the IP address of a new node, we need to send it a reply,
				 * letting it know what our DNS server IP is. He got to us, but it doesn't know
				 * our IP yet. That what this retransmission is about.
				 */
				nodeInterface = rmiNode.getStub(nodeInterface, "node", hostIP, RMIPORT);
				try {
					nodeInterface.setDNSIP(nameServerIp);
				} catch (Exception e) {
					SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				}
			}
		}).start();

	}

	@Override
	public String getIPAddress(int filehash) {
		return nsr.getFileLocation(filehash);
	}

	@Override
	public void removeNode(int hash) {
		nsr.removeNodeFromRegister(hash);
	}

	@Override
	public int getNextNode(int hash) {
		return nsr.getNextNode(hash);
	}

	@Override
	public int getPreviousNode(int hash) {
		return nsr.getPreviousNode(hash);
	}

	@Override
	public String getNodeIP(int hash) {
		return nsr.getNodeIPFromHash(hash);
	}

}