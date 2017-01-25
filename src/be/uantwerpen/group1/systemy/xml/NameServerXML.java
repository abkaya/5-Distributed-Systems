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

public class NameServerXML {
	public void create() {

		try {

			//Create the DocumentBuilder for the XML file
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			//Define the root element
			Document docNameServer = docBuilder.newDocument();
			Element rootElement = docNameServer.createElement("SystemY");
			docNameServer.appendChild(rootElement);

			//Define the base element
			Element NameServer = docNameServer.createElement("NameServer");
			rootElement.appendChild(NameServer);

			//Define the MulticastPort of the NameServer
			Element MulticastPort = docNameServer.createElement("MulticastPort");
			MulticastPort.appendChild(docNameServer.createTextNode("2000"));
			NameServer.appendChild(MulticastPort);
			
			//Define the NodeDnsPort for the node
			Element DnsPort = docNameServer.createElement("RMIPort");
			DnsPort.appendChild(docNameServer.createTextNode("1099"));
			NameServer.appendChild(DnsPort);

			//Define the MulticastIp of the NameServer
			Element MulticastIp = docNameServer.createElement("MulticastIp");
			MulticastIp.appendChild(docNameServer.createTextNode("234.0.113.0"));
			NameServer.appendChild(MulticastIp);
			
			//Define the interface of the NameServer
			Element RemoteNsName = docNameServer.createElement("RemoteNsName");
			RemoteNsName.appendChild(docNameServer.createTextNode("NameServerInterface"));
			NameServer.appendChild(RemoteNsName);

			//Write the content into XML file called NameServer.xml
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(docNameServer);
			StreamResult result = new StreamResult(new File("NameServer.xml"));

			transformer.transform(source, result);

		} catch (Exception e) {

		}
	}

}
