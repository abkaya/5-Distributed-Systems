package be.uantwerpen.group1.systemy.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * MulticastSender class
 * Class for sending multicast messages
 */
public class MulticastSender {
	
	/**
	 * send a multicast message
	 * 
	 * @param IP: needs to be a multicast adress e.g. 234.0.113.0
	 * @param port 
	 * @param message
	 */
	public static void send(String IP, int port, String message){
		MulticastSocket s = null;
		try {
			InetAddress group = InetAddress.getByName(IP);	
			s = new MulticastSocket(port);
			byte [] m = message.getBytes();
			DatagramPacket messageOut = new DatagramPacket(m, m.length, group, port);
			s.send(messageOut);
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(s != null)
				s.close(); 
		}
	}
	
}
