package be.uantwerpen.group1.systemy.networking;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Class to get 
 * 
 * @author Robin
 */
public class Interface {
	
	/**
	 * Method to get IP of local machine and preventing loopback, link local and IPv6 addresses
	 * Iterates interfaces and takes last IP
	 * 
	 * @return String: IP address
	 * @throws SocketException
	 */
	public static String getIP() throws SocketException {
		
		/*
		 * Old ways:
		 * String myIP = InetAddress.getLocalHost().getHostAddress();	// Automatic
		 * String myIP = "192.168.1.103";								// Manual
		 */
		
		String IP = null;
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while(en.hasMoreElements()){
			NetworkInterface ni =(NetworkInterface) en.nextElement();
			Enumeration<InetAddress> ee = ni.getInetAddresses();
			while(ee.hasMoreElements()) {
				InetAddress ia = (InetAddress) ee.nextElement();
				if( !ia.isLoopbackAddress() && !ia.isLinkLocalAddress() && ia instanceof Inet4Address ) {
					// prevent loopback, link-local and IPv6
					IP = ia.getHostAddress();
					System.out.println("Detected address: " + ia.getHostAddress());
				}
			}
		}
		return IP;
		
	}
		
}
