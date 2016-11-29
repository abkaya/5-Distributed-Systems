package be.uantwerpen.group1.systemy.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParserXML
{

	// boolean for checking if it's a Node or Server
	// False for node, True for server
	private boolean NodeOrServer;
	private File file;

	// Declarations of the variables for the node
	private String IPAddressN;
	private String hostNameN;
	private String dnsIPN;
	private int MulticastPortN;
	private int tcpFileTranferPortN;
	private int tcpDNSRetransmissionPortN;

	// Declarations of the variables for the NameServer
	private String IPAddressNS;
	private int MulticastPortNS;
	private int tcpDNSRetransmissionPortNS;
	private String MulticastIPNS;

	public ParserXML(String NodeOrServer)
	{
		if (NodeOrServer.contains("NameServer"))
		{
			this.NodeOrServer = true;
			this.file = new File("NameServer.xml");
		} else
		{
			this.NodeOrServer = false;
			this.file = new File("Node.xml");
		}
	}

	/*
	 * Getters for the Node
	 */
	public String getIPAddressN()
	{
		return this.IPAddressN;
	}

	public String getHostNameN()
	{
		return this.hostNameN;
	}

	public String getDNSIPN()
	{
		return this.dnsIPN;
	}

	public int getMulticastPortN()
	{
		return this.MulticastPortN;
	}

	public int geTtcpFileTranferPortN()
	{
		return this.tcpFileTranferPortN;
	}

	public int getTcpDNSRetransmissionPortN()
	{
		return this.tcpDNSRetransmissionPortN;
	}

	/*
	 * Getters for the NameServer
	 */
	public String getIPAddressNS()
	{
		return this.IPAddressNS;
	}

	public int getMulticastPortNS()
	{
		return this.MulticastPortNS;
	}

	public int getTcpDNSRetransmissionPortNS()
	{
		return this.tcpDNSRetransmissionPortNS;
	}

	public String getMulticastIPNS()
	{
		return this.MulticastIPNS;
	}

	public void parse()
	{
		try
		{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(this.file);

			document.getDocumentElement().normalize();

			// if NameServer
			if (this.NodeOrServer)
			{
				NodeList nodeList = document.getElementsByTagName("NameServer");

				for (int i = 0; i < nodeList.getLength(); i++)
				{

					Node node = nodeList.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						Element element = (Element) node;

						IPAddressNS = element.getElementsByTagName("IPAddress").item(0).getTextContent();
						MulticastPortNS = Integer.parseInt(element.getElementsByTagName("MulticastPort").item(0).getTextContent());
						tcpDNSRetransmissionPortNS = Integer.parseInt(element.getElementsByTagName("tcpDNSRetransmissionPort").item(0)
								.getTextContent());
						MulticastIPNS = element.getElementsByTagName("MulticastIP").item(0).getTextContent();

					}
				}

			} else
			{
				NodeList nodeList = document.getElementsByTagName("Node");

				for (int i = 0; i < nodeList.getLength(); i++)
				{

					Node node = nodeList.item(i);

					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						Element element = (Element) node;

						IPAddressN = element.getElementsByTagName("IPAddress").item(0).getTextContent();
						hostNameN = element.getElementsByTagName("hostname").item(0).getTextContent();
						dnsIPN = element.getElementsByTagName("dnsIP").item(0).getTextContent();
						MulticastPortN = Integer.parseInt(element.getElementsByTagName("MulticastPort").item(0).getTextContent());
						tcpFileTranferPortN = Integer.parseInt(element.getElementsByTagName("tcpFileTranferPort").item(0).getTextContent());
						tcpDNSRetransmissionPortN = Integer.parseInt(element.getElementsByTagName("tcpDNSRetransmissionPort").item(0)
								.getTextContent());

					}
				}
			}

		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
