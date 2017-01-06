package be.uantwerpen.group1.systemy.log_debug;

import be.uantwerpen.group1.systemy.nameserver.NameServer;
import be.uantwerpen.group1.systemy.nameserver.NameServerRegister;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class Debugger
{

	public static void main(String[] args)
	{
		NameServerRegister nameServerRegister = new NameServerRegister();
		//NameServer nameServer = new NameServer("localhost", true);

		//Add nodes to the register
		nameServerRegister.addNode("Node_Abdil", "192.168.1.106");
		nameServerRegister.addNode("Node_Robin", "192.168.1.110");
		nameServerRegister.addNode("Node_Levi", "192.168.1.109");
		nameServerRegister.addNode("Node_Extra", "192.168.1.120");

		
		//get size register
		System.out.println(nameServerRegister.getSize());

		nameServerRegister.getFileLocation(Hashing.hash("testFile"));
		nameServerRegister.getFileLocation(Hashing.hash("Abdil"));
		nameServerRegister.getFileLocation(Hashing.hash("Robin"));
		nameServerRegister.getFileLocation(Hashing.hash("Levi"));
		nameServerRegister.getFileLocation(Hashing.hash("test.txt"));
		nameServerRegister.getFileLocation(Hashing.hash("picture.jpg"));
		nameServerRegister.getFileLocation(Hashing.hash("Node 1"));
		nameServerRegister.getFileLocation(Hashing.hash("192.168.1.110"));
	}

}
