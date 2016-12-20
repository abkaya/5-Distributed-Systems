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

public class NodeXML {

	public void create() {
		
		String fileName = "node.xml";
		String workingDir = System.getProperty("user.dir");
		

		try {

			//Create the DocumentBuilder for the XML file
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			//Define the root element
			Document docNode = docBuilder.newDocument();
			Element rootElement = docNode.createElement("SystemY");
			docNode.appendChild(rootElement);

			//Define the base element 
			Element Node = docNode.createElement("Node");
			rootElement.appendChild(Node);

			//Define the hostname of the node
			Element Hostname = docNode.createElement("Hostname");
			Hostname.appendChild(docNode.createTextNode("Node1"));
			Node.appendChild(Hostname);

			//Define the dnsIP for the node
			Element DnsIp = docNode.createElement("DnsIp");
			DnsIp.appendChild(docNode.createTextNode("null"));
			Node.appendChild(DnsIp);

			//Define the NodeDnsPort for the node
			Element DnsPort = docNode.createElement("DnsPort");
			DnsPort.appendChild(docNode.createTextNode("1099"));
			Node.appendChild(DnsPort);

			//Define the multicastport for the node
			Element MulticastPort = docNode.createElement("MulticastPort");
			MulticastPort.appendChild(docNode.createTextNode("2000"));
			Node.appendChild(MulticastPort);

			//Define the tcpFileTransferPort for the node 
			Element TcpFileTranferPort = docNode.createElement("TcpFileTranferPort");
			TcpFileTranferPort.appendChild(docNode.createTextNode("2001"));
			Node.appendChild(TcpFileTranferPort);

			//Define the tcpDNSRetransmissionPort for the node
			Element TcpDnsRetransmissionPort = docNode.createElement("TcpDnsRetransmissionPort");
			TcpDnsRetransmissionPort.appendChild(docNode.createTextNode("2002"));
			Node.appendChild(TcpDnsRetransmissionPort);

			//Define the neighborport for the Node
			Element NeighborPort = docNode.createElement("NeighborPort");
			NeighborPort.appendChild(docNode.createTextNode("2003"));
			Node.appendChild(NeighborPort);

			//Define the interface of the NameServer
			Element RemoteNsName = docNode.createElement("RemoteNsName");
			RemoteNsName.appendChild(docNode.createTextNode("NameServerInterface"));
			Node.appendChild(RemoteNsName);

			// write the content into XML file calles Node.xml
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(docNode);
			StreamResult result = new StreamResult(new File("Node.xml"));

			transformer.transform(source, result);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}