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
		nameServerRegister.addNode("Node 1", "192.168.1.106");
		nameServerRegister.addNode("Node 1", "192.168.1.107");
		nameServerRegister.addNode("Node 2", "192.168.1.110");
		nameServerRegister.addNode("Node 3", "192.168.1.109");
		nameServerRegister.addNode("Node 4", "192.168.1.120");
		nameServerRegister.addNode("Node 5", "192.168.1.125");
		nameServerRegister.addNode("Node 25", "192.168.1.150");
		nameServerRegister.addNode("Node 50", "192.168.1.200");
		
		//get size register
		System.out.println(nameServerRegister.getSize());

		//test ringnetwork
		nameServerRegister.getNextNode(Hashing.hash("Node 1"));
		nameServerRegister.getNextNode(Hashing.hash("Node 4"));
		nameServerRegister.getPreviousNode(Hashing.hash("Node 2"));
		nameServerRegister.getPreviousNode(Hashing.hash("Node 5"));
		nameServerRegister.getPreviousNode(Hashing.hash("Node 25"));
		
		//remove node from register
		nameServerRegister.removeNodeFromRegister(Hashing.hash("Node 2"));

		//get size register
		System.out.println(nameServerRegister.getSize());
	}

}
