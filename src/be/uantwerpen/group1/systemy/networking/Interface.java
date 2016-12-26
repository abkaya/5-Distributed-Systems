package be.uantwerpen.group1.systemy.networking;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

/**
 * Class to get 
 * 
 * @author Robin
 */
public class Interface {

	private static String logName = Interface.class.getName() + " >> ";

	/**
	 * Method to get IP of local machine and preventing loopback, link local and IPv6 addresses
	 * Iterates interfaces and takes last IP
	 * 
	 * @return String: IP address
	 * @throws SocketException
	 */
	public static ArrayList<String> getIPList() throws SocketException {

		/*
		 * Old ways:
		 * String myIP = InetAddress.getLocalHost().getHostAddress();	// Automatic
		 * String myIP = "192.168.1.103";								// Manual
		 */

		ArrayList<String> IPs = new ArrayList<String>();
		//String IP = null;
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) en.nextElement();
			Enumeration<InetAddress> ee = ni.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress ia = (InetAddress) ee.nextElement();
				if (!ia.isLoopbackAddress() && !ia.isLinkLocalAddress() && ia instanceof Inet4Address) {
					// prevent loopback, link-local and IPv6
					IPs.add(ia.getHostAddress());
					SystemyLogger.log(Level.INFO, logName + "Detected address: " + ia.getHostAddress());
				}
			}
		}
		return IPs;

	}

	/**
	 * Method to select an IP from list
	 * 
	 * @param IPs: list of IPs
	 * @return String: selected IP
	 */
	public static String chooseIP(ArrayList<String> IPs) {
		String IP = null;
		if (IPs.size() == 1) {
			IP = IPs.get(0);
		} else if (IPs.size() > 1) {
			System.out.println("Choose one of the following IP addresses:");
			for (int i = 0; i < IPs.size(); i++) {
				System.out.println("  (" + i + ") " + IPs.get(i));
			}
			int n = -1;
			Scanner reader = new Scanner(System.in);
			while (n < 0 || n > IPs.size() - 1) {
				System.out.print("Enter prefered number: ");
				n = reader.nextInt();
			}
			reader.close();
			IP = IPs.get(n);
		} else {
			SystemyLogger.log(Level.SEVERE, logName + "No usable IP address detected");
			System.exit(-1);
		}
		return IP;
	}
	
	/**
	 * Select IP from possible interfaces
	 * 
	 * @return String: Selected IP
	 * @throws SocketException
	 */
	public static String getIP() throws SocketException {
		ArrayList<String> IPs = getIPList();
		return chooseIP(IPs);
	}
		
}
