package be.uantwerpen.group1.systemy.node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class node {

	public static void main(String args[]) {
		
		MulticastSocket s = null;
		
		try {
			InetAddress group = InetAddress.getByName("234.0.113.0");	// needs to be a multicast address
			s = new MulticastSocket(1337);
			s.joinGroup(group);
			String message = InetAddress.getLocalHost().getHostAddress();
			byte [] m = message.getBytes();
			DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 1337);
			s.send(messageOut);
			s.leaveGroup(group);
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(s != null)
				s.close();
		}
		
	}
	
}

