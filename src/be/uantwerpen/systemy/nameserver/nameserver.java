package be.uantwerpen.systemy.nameserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class nameserver {

	public static void main(String args[]) {
		
		MulticastSocket s = null;
		
		try {
			InetAddress group = InetAddress.getByName("234.0.113.0");	// needs to be a multicast address
			s = new MulticastSocket(1337);
			s.joinGroup(group);
			while(true) {
				byte[] buffer = null;	// clearing buffer
				buffer = new byte[64];
				DatagramPacket message = new DatagramPacket(buffer, buffer.length);
				s.receive(message);
				System.out.println("Received: " + new String(message.getData()));
			}
			//s.leaveGroup(group);
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if(s != null)
				s.close();
		}
		
	}
	
}