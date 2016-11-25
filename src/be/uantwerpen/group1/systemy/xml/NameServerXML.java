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

public class NameServerXML
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
			Element NameServer = docNameServer.createElement("NameServer");
			rootElement.appendChild(NameServer);

			// Add IP address to NameServer
			Element IpAddressNS = docNameServer.createElement("IPAddress");
			IpAddressNS.appendChild(docNameServer.createTextNode("192.168.1.103"));
			NameServer.appendChild(IpAddressNS);

			// Add MulticasPort to NameServer
			Element MulticastPort = docNameServer.createElement("MulticastPort");
			MulticastPort.appendChild(docNameServer.createTextNode("2000"));
			NameServer.appendChild(MulticastPort);

			//
			Element tcpDNSRetransmissionPort = docNameServer.createElement("tcpDNSRetransmissionPort");
			tcpDNSRetransmissionPort.appendChild(docNameServer.createTextNode("2002"));
			NameServer.appendChild(tcpDNSRetransmissionPort);

			//
			Element multicastIP = docNameServer.createElement("MulticastIP");
			multicastIP.appendChild(docNameServer.createTextNode("234.0.113.0"));
			NameServer.appendChild(multicastIP);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(docNameServer);
			StreamResult result = new StreamResult(new File("NameServer.xml"));
			transformer.transform(source, result);

		} catch (Exception e)
		{

		}
	}

}
