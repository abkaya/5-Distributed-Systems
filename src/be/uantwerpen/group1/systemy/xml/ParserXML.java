package be.uantwerpen.group1.systemy.xml;

import java.io.File;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

/**
 * Class to retrieve config data from xml file
 * 
 * @author Levi
 * @author Robin
 */
public class ParserXML {

	private static String logName = ParserXML.class.getName() + " >> ";
	
	/**
	 * Method to retrieve field from config.xml
	 * If not found: exit application
	 * 
	 * @param key: xml tag of requested field
	 * @return String: value if found (in case of not found 'null')
	 */
	public static String parseXML(String key) {
		try {
			File file = new File("config.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);
			Element rootElement = document.getDocumentElement();
			NodeList nodeList = rootElement.getElementsByTagName(key);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					return element.getTextContent();
				}
			}
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + "Can't parse configuration from config.xml");
			System.exit(-1);
		}
		// default answer if nothing
		SystemyLogger.log(Level.SEVERE, logName + "config attribute [" + key + "] not found in config.xml");
		System.exit(-1);
		return null;
	}
	
}
