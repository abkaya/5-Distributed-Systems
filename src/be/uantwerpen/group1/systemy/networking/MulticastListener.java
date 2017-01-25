package be.uantwerpen.group1.systemy.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;

/**
 * MulticastListener class
 * Class for listening to multicast messages
 *
 * @author Robin Janssens
 */
public class MulticastListener {

	private static String logName = MulticastListener.class.getName().replace("be.uantwerpen.group1.systemy.", "") + " >> ";

	private MulticastSocket s = null;

	/**
	 * create a multicast socket
	 *
	 * @param IP: needs to be a multicast address e.g. 234.0.113.0
	 * @param port
	 */
	public MulticastListener(String IP, int port) {
		try {
			InetAddress group = InetAddress.getByName(IP);
			s = new MulticastSocket(port);
			s.joinGroup(group);
		} catch (IOException e) {
			SystemyLogger.log(Level.SEVERE, logName + "IO: " + e.getMessage());
			//System.out.println("IO: " + e.getMessage());
		}
	}

	/**
	 * receive on multicast socket
	 *
	 * @return String: received message (max 64 characters)
	 */
	public String receive() {
		try {
			// clearing buffer
			byte[] buffer = null;
			buffer = new byte[64];
			DatagramPacket message = new DatagramPacket(buffer, buffer.length);
			s.receive(message);
			return new String(message.getData());
		} catch (IOException e) {
			SystemyLogger.log(Level.SEVERE, logName + "IO: " + e.getMessage());
			//System.out.println("IO: " + e.getMessage());
			return null;
		}
	}

}
