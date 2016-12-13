package be.uantwerpen.group1.systemy.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserXML {

	// boolean for checking if it's a Node or Server
	// False for node, True for server
	private static boolean NodeOrServer;
	private static File file;

	// Declarations of the variables for the node
	private static String hostNameN;
	private static String dnsIpN;
	private static int dnsPortN;
	private static int multicastPortN;
	private static int tcpFileTranferPortN;
	private static int tcpDnsRetransmissionPortN;
	private static int neighborPortN;
	private static String remoteNsNameN;

	// Declarations of the variables for the NameServer
	private static int multicastPortNS;
	private static int tcpDnsRetransmissionPortNS;
	private static String multicastIpNS;
	private static String remoteNsNameNS;

	public ParserXML(String NodeOrServer) {
		if (NodeOrServer.contains("NameServer")) {
			ParserXML.NodeOrServer = true;
			ParserXML.file = new File("NameServer.xml");
			parse();
		} else {
			ParserXML.NodeOrServer = false;
			ParserXML.file = new File("Node.xml");
			parse();
		}
	}

	/*
	 * Getters for the Node
	 */

	public String getHostNameN() {
		return hostNameN;
	}

	public String getDnsIPN() {
		return dnsIpN;
	}

	public int getDnsPortN() {
		return dnsPortN;
	}

	public int getMulticastPortN() {
		return multicastPortN;
	}

	public int geTcpFileTranferPortN() {
		return tcpFileTranferPortN;
	}

	public int getTcpDnsRetransmissionPortN() {
		return tcpDnsRetransmissionPortN;
	}

	public int getNeighborPortN() {
		return neighborPortN;
	}

	public String getRemoteNsNameN() {
		return remoteNsNameN;
	}

	/*
	 * Getters for the NameServer
	 */

	public int getMulticastPortNS() {
		return multicastPortNS;
	}

	public int getTcpDNSRetransmissionPortNS() {
		return tcpDnsRetransmissionPortNS;
	}

	public String getMulticastIpNS() {
		return multicastIpNS;
	}
	
	public String getRemoteNsNameNS() {
		return remoteNsNameNS;
	}

	private void parse() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);

			document.getDocumentElement().normalize();

			// if NameServer
			if (NodeOrServer) {
				NodeList nodeList = document.getElementsByTagName("NameServer");

				for (int i = 0; i < nodeList.getLength(); i++) {

					Node node = nodeList.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;

						multicastPortNS = Integer
								.parseInt(element.getElementsByTagName("MulticastPort").item(0).getTextContent());
						tcpDnsRetransmissionPortNS = Integer.parseInt(
								element.getElementsByTagName("TcpDnsRetransmissionPort").item(0).getTextContent());
						multicastIpNS = element.getElementsByTagName("MulticastIp").item(0).getTextContent();
						remoteNsNameNS = element.getElementsByTagName("RemoteNsName").item(0).getTextContent();

					}
				}

			} else {
				NodeList nodeList = document.getElementsByTagName("Node");

				for (int i = 0; i < nodeList.getLength(); i++) {

					Node node = nodeList.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;

						hostNameN = element.getElementsByTagName("Hostname").item(0).getTextContent();
						dnsIpN = element.getElementsByTagName("DnsIp").item(0).getTextContent();
						dnsPortN = Integer.parseInt(element.getElementsByTagName("DnsPort").item(0).getTextContent());
						multicastPortN = Integer
								.parseInt(element.getElementsByTagName("MulticastPort").item(0).getTextContent());
						tcpFileTranferPortN = Integer
								.parseInt(element.getElementsByTagName("TcpFileTranferPort").item(0).getTextContent());
						tcpDnsRetransmissionPortN = Integer.parseInt(
								element.getElementsByTagName("TcpDnsRetransmissionPort").item(0).getTextContent());
						neighborPortN = Integer
								.parseInt(element.getElementsByTagName("NeighborPort").item(0).getTextContent());
						remoteNsNameN = element.getElementsByTagName("RemoteNsName").item(0).getTextContent();

					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
