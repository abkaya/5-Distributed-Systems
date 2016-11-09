package be.uantwerpen.group1.systemy.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * MulticastListener class
 * Class for listening to multicast messages
 * 
 * @author Robin Janssens
 */
public class MulticastListener {

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
			e.printStackTrace();
			System.out.println("MulticastSender >> " + e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * receive on multicast socket
	 * 
	 * @return String: received message (max 64 characters) 
	 */
	public String receive() {
		try
		{
			// clearing buffer
			byte[] buffer = null;
			buffer = new byte[64];
			DatagramPacket message = new DatagramPacket(buffer, buffer.length);
			s.receive(message);
			return new String(message.getData());
		} catch (IOException e)
		{
			System.out.println("IO: " + e.getMessage());
			return null;
		}
	}
	
}
