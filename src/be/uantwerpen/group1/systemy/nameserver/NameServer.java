package be.uantwerpen.group1.systemy.nameserver;

import be.uantwerpen.group1.systemy.networking.RMI;
import be.uantwerpen.group1.systemy.networking.TCP;
import be.uantwerpen.group1.systemy.networking.MulticastListener;
import be.uantwerpen.group1.systemy.logging.SystemyLogger;

import java.util.logging.Level;

public class NameServer implements NameServerInterface {
	
	private static String logName = NameServer.class.getName() + " >> ";

	static NameServerRegister nsr = new NameServerRegister(false);

	public static void main(String args[]) {
		String nameServerIP = "192.168.1.101";
		int multicastPort = 2000;
		int tcpDNSRetransmissionPort = 2002;

		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>(nameServerIP, "NameServerInterface", nsi);

		/*
		 * New nodes will apply to join the multicast group, be added to the DNS registry,
		 * and will receive the DNS IP through TCP retransmission
		 */
		MulticastListener multicastListener = new MulticastListener("234.0.113.0", multicastPort);
		new Thread(() -> {
			while (true) {
				String[] hostnameIP;
				String multicastMessage = multicastListener.receive().trim();
				hostnameIP = multicastMessage.split(",");
				SystemyLogger.log(Level.INFO,
						logName + "Received hostname " + hostnameIP[0] + ", IP: " + hostnameIP[1]);
				//System.out.println("Received hostname " + hostnameIP[0] + ", IP : " + hostnameIP[1]);
				nsr.addNode(hostnameIP[0], hostnameIP[1]);
				/*After having received the IP address of a new node, we need to send it a reply,
				 * letting it know what our DNS server IP is. He got to us, but it doesn't know
				 * our IP yet. That what this retransmission is about.
				 */
				TCP dnsIPRetransmission = new TCP(tcpDNSRetransmissionPort, hostnameIP[1]);
				dnsIPRetransmission.sendText(nameServerIP);
			}
		}).start();

	}

	@Override
	public String getFileLocation(String fileName) {
		return nsr.getFileLocation(fileName);
	}

}