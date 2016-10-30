package be.uantwerpen.group1.systemy.nameserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;

import be.uantwerpen.group1.systemy.networking.RMI;

public class NameServer implements NameServerInterface
{

	public static void main(String args[])
	{
		
		/*
		MulticastSocket s = null;

		try
		{
			// needs to be a multicast address
			InetAddress group = InetAddress.getByName("234.0.113.0");
			s = new MulticastSocket(1337);
			s.joinGroup(group);
			while (true)
			{
				// clearing buffer
				byte[] buffer = null;
				buffer = new byte[64];
				DatagramPacket message = new DatagramPacket(buffer, buffer.length);
				s.receive(message);
				System.out.println("Received: " + new String(message.getData()));
			}
			// s.leaveGroup(group);
		} catch (IOException e)
		{
			System.out.println("IO: " + e.getMessage());
		} finally
		{
			if (s != null)
				s.close();
		}*/
		
		NameServerInterface nsi = new NameServer();
		RMI<NameServerInterface> rmi = new RMI<NameServerInterface>("localhost","NameServerInterface",nsi);

	}

	@Override
	public String getIPAddress(String fileName) throws RemoteException
	{
		// TODO Auto-generated method stub
		return "Name Server response: You are now supposed to get the ip address of machine holding the file you were looking for. \n"
				+ "This string will be replaced by the output of a nethod provided by the NameServerRegistry class.";
	}

}