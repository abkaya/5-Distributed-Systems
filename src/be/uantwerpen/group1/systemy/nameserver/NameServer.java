package be.uantwerpen.group1.systemy.nameserver;

import java.net.SocketException;
import java.net.UnknownHostException;

import be.uantwerpen.group1.systemy.networking.Interface;
import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.xml.ParserXML;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

import java.util.logging.Level;

public class NameServer implements NameServerInterface {

	private static String logName = NameServer.class.getName() + " >> ";
	private static ParserXML parserXML = new ParserXML(logName);

	private static final int MULTICASTPORT = parserXML.getMulticastPortNS();
	private static final int TCPDNSRETRANSMISSIONPORT = parserXML.getTcpDNSRetransmissionPortNS();
	private static final String MULTICASTIP = parserXML.getMulticastIpNS();
	private static final String REMOTENSNAME = parserXML.getRemoteNsNameNS();

	private static final NameServerRegister nsr = new NameServerRegister(false);

	public static void main(String args[]) throws UnknownHostException, SocketException {

		String nameServerIp = Interface.getIP();

		SystemyLogger.log(Level.INFO, logName + "NameServer started on " + nameServerIp);

		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>(nameServerIp, REMOTENSNAME, nsi);

		/*
		 * New nodes will apply to join the multicast group, be added to the DNS registry,
		 * and will receive the DNS IP through TCP retransmission
		 */
		MulticastListener multicastListener = new MulticastListener(MULTICASTIP, MULTICASTPORT);
		new Thread(() -> {
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
				TCP dnsIPRetransmission = new TCP(TCPDNSRETRANSMISSIONPORT, hostIP);
				dnsIPRetransmission.sendText(nameServerIp);
			}
		}).start();

	}

	@Override
	public String getIPAddress(String fileName) {
		return nsr.getFileLocation(fileName);
	}

	@Override
	public void removeNode(int hash) {
		nsr.removeNodeFromRegister(Integer.toString(hash));
	}

	@Override
	public int getNextNode(int hash) {
		return Integer.parseInt(nsr.getNextNode(Integer.toString(hash)));
	}

	@Override
	public int getPreviousNode(int hash) {
		return Integer.parseInt(nsr.getPreviousNode(Integer.toString(hash)));
	}

}
