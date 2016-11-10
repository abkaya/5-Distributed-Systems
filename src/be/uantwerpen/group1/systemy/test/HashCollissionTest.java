package be.uantwerpen.group1.systemy.test;

import java.util.Arrays;

import be.uantwerpen.group1.systemy.node.NodeInfo;


/**
 * Test class to check for collisions in hashing function
 * 
 * @author Robin Janssens
 */
public class HashCollissionTest {

	/**
	 * hash function
	 * 
	 * @String name: input of hash function
	 * @return int: hashed value between 0 and 32767
	 */
	private static int hash(String name) {
		return Math.abs((name.hashCode()) % 32768);
	}
	
	/**
	 * Test Code
	 */
	public static void main(String args[]) {
		
		int amount = 1000;
		
		// ------------------------------
		// create 20 nodes
		// ------------------------------
		NodeInfo[] nodes = new NodeInfo[amount];
		for (int i = 0; i<amount; i++ ) {
			NodeInfo newNode = new NodeInfo();
			newNode.setName("node" + i);
			newNode.setHash( hash(newNode.getName()) );
			nodes[i] = newNode;
		}
		
		// ------------------------------
		// sort on nodes own hash
		// ------------------------------
		Arrays.sort(nodes);
		
		// ------------------------------
		// compare hashes
		// ------------------------------
		for (int i = 0; i<amount-1; i++ ) {		
			if( nodes[i].compareTo(nodes[i+1]) == 0 ) {
				System.out.println( nodes[i].toString() + " = " + nodes[i+1].toString() );
			}
		}
	
	}
	
}
