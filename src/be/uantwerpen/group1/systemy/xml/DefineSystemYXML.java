package be.uantwerpen.group1.systemy.xml;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefineSystemYXML {

	public static void main(String argv[]) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root element ==> SYSTEMY
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("SystemY");
			doc.appendChild(rootElement);

			// NameServer
			Element NameServer = doc.createElement("NameServer");
			rootElement.appendChild(NameServer);

			// Add IP address to NameServer
			Element IpAddressNS = doc.createElement("IPAddress");
			IpAddressNS.appendChild(doc.createTextNode("192.168.1.103"));
			NameServer.appendChild(IpAddressNS);

			// Add MulticasPort to NameServer
			Element MulticastPort = doc.createElement("MulticastPort");
			MulticastPort.appendChild(doc.createTextNode("2000"));
			NameServer.appendChild(MulticastPort);

			//
			Element tcpDNSRetransmissionPort = doc.createElement("tcpDNSRetransmissionPort");
			tcpDNSRetransmissionPort.appendChild(doc.createTextNode("2002"));
			NameServer.appendChild(tcpDNSRetransmissionPort);

			//
			Element multicastIP = doc.createElement("MulticastIP");
			multicastIP.appendChild(doc.createTextNode("234.0.113.0"));
			NameServer.appendChild(multicastIP);

			//for loop for the lazy person, you can choose how many nodes you want to add to the network
			for (int i = 0; i < 4; i++) {

				// Define the node
				Element Node = doc.createElement("Node" + String.valueOf(i));
				rootElement.appendChild(Node);

				Element NodeIpAddress = doc.createElement("IPAddress");
				NodeIpAddress.appendChild(doc.createTextNode("192.168.1." + String.valueOf(110 + i)));
				Node.appendChild(NodeIpAddress);

				Element NodeHostname = doc.createElement("hostname");
				NodeHostname.appendChild(doc.createTextNode("Node" + String.valueOf(i)));
				Node.appendChild(NodeHostname);

				Element NodeDnsIP = doc.createElement("dnsIP");
				NodeDnsIP.appendChild(doc.createTextNode("null"));
				Node.appendChild(NodeDnsIP);

				Element NodeMulticastPort = doc.createElement("MulticastPort");
				NodeMulticastPort.appendChild(doc.createTextNode("2000"));
				Node.appendChild(NodeMulticastPort);

				Element NodeTcpFileTranferPort = doc.createElement("tcpFileTranferPort");
				NodeTcpFileTranferPort.appendChild(doc.createTextNode("2001"));
				Node.appendChild(NodeTcpFileTranferPort);

				Element NodeTcpDNSRetransmissionPort = doc.createElement("tcpDNSRetransmissionPort");
				NodeTcpDNSRetransmissionPort.appendChild(doc.createTextNode("2002"));
				Node.appendChild(NodeTcpDNSRetransmissionPort);

			}

			// write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("BuildingSystemY.xml"));

			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
}