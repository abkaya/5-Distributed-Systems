package be.uantwerpen.group1.systemy.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NodeXML
{

	public void create()
	{

		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root element ==> SYSTEMY
			Document docNameServer = docBuilder.newDocument();
			Element rootElement = docNameServer.createElement("SystemY");
			docNameServer.appendChild(rootElement);

			// NameServer
			Element Node = docNameServer.createElement("Node");
			rootElement.appendChild(Node);

			Element NodeIpAddress = docNameServer.createElement("IPAddress");
			NodeIpAddress.appendChild(docNameServer.createTextNode("192.168.1.110"));
			Node.appendChild(NodeIpAddress);

			Element NodeHostname = docNameServer.createElement("hostname");
			NodeHostname.appendChild(docNameServer.createTextNode("Node1"));
			Node.appendChild(NodeHostname);

			Element NodeDnsIP = docNameServer.createElement("dnsIP");
			NodeDnsIP.appendChild(docNameServer.createTextNode("null"));
			Node.appendChild(NodeDnsIP);

			Element NodeMulticastPort = docNameServer.createElement("MulticastPort");
			NodeMulticastPort.appendChild(docNameServer.createTextNode("2000"));
			Node.appendChild(NodeMulticastPort);

			Element NodeTcpFileTranferPort = docNameServer.createElement("tcpFileTranferPort");
			NodeTcpFileTranferPort.appendChild(docNameServer.createTextNode("2001"));
			Node.appendChild(NodeTcpFileTranferPort);

			Element NodeTcpDNSRetransmissionPort = docNameServer.createElement("tcpDNSRetransmissionPort");
			NodeTcpDNSRetransmissionPort.appendChild(docNameServer.createTextNode("2002"));
			Node.appendChild(NodeTcpDNSRetransmissionPort);

			// write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(docNameServer);
			StreamResult result = new StreamResult(new File("Node.xml"));

			transformer.transform(source, result);

		} catch (Exception e)
		{
			// TODO: handle exception
		}
	}

}
