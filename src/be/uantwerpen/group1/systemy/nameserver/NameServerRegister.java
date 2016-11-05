/**
 * This class is the register of the NameServer.
 * It will keep track of the nodes, performs the hashing function, calculate the ownership of a file, ...
 * 
 * Convention
 * -------------
 * If a host is not added to the network the terms hostName and hostIP are used
 * If a host is added to the network it becomes a node and the terms nodeName and nodeIP are used
 * 
 * Treemap
 * -------------
 * A treemap is chosen because it stores the values in order. This way the lookup for the file location, next node and previous node 
 * can be done fast and efficient. 
 * 
 * @author	Mariën Levi
 * @version 1.0
 * @since 29/10/2016
 * 
 */

package be.uantwerpen.group1.systemy.nameserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.TreeMap;


public class NameServerRegister implements Serializable {

	/*
	 * Default number
	 */
	private static final long serialVersionUID = 1L;

	/* Parameter list
	 * ---------------------
	 * map: the map that contains the names and IPAddresses of the nodes (will be implemented as a Treemap)
	 * fileName: the name of the file for saving on the hard drive
	 */

	/*---------------------------------------------------------------
	 * This can be updated in the future to make use of JSON (extra)
	 ---------------------------------------------------------------*/

	private static TreeMap<Integer, String> register;
	private String fileName = "NSRegister.ser";

	/**
	 * Auto generated constructor
	 * @param clear: if clear = 'true' clear previous register, if clear = 'false' continue with previous register
	 */
	public NameServerRegister(boolean clear) {
		// TODO Auto-generated 	constructor stub

		// The integer value is the hash calculated and the String is the IPAddres of the host/node
		register = new TreeMap<Integer, String>();

		//if (clear) {
			//loadRegister();
			//register.clear();
			//saveRegister();
			//System.out.println("Registered cleared and loaded");
		//} else {
			System.out.println("Register loaded");
		//}
	}

	/**
	 * This method loads the register from the hard drive
	 */
	@SuppressWarnings("unchecked")
	public void loadRegister() {
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName));
			//cast to Treemap<Integer, String>)
			register = (TreeMap<Integer, String>) objectInputStream.readObject();
			objectInputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * This method saves the register to the hard drive
	 */
	public void saveRegister() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));
			objectOutputStream.writeObject(register);
			objectOutputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * Converts a string to hashcode (from 0 - 32768, see specifications)
	 * @param name: the name where we need to calculate the hash from
	 */
	public int hashing(String nameToConvert) {
		return Math.abs((nameToConvert.hashCode()) % 32768);
	}

	/**
	 * This method adds a host as a node to the register
	 * @param hostName: hostname of the new node
	 * @param hostIP: IPAddress of the new node
	 */
	public void addNode(String hostName, String hostIP) {
		//loadRegister();
		int nodeHash = hashing(hostName);
		if (register.containsKey(nodeHash)) {
			System.out.println("This node already exist");
		} else {
			register.put(nodeHash, hostIP);
			System.out.println(hostName + " (hashcode: " + nodeHash + "): " + hostIP + " is added to the register");
		}
		//saveRegister();
	}

	/**
	 * This method removes a node from the register based on his hash code
	 * @param nodeHash: this is the hash of the node that's need to be removed
	 */
	public void removeNodeFromRegister(int nodeHash) {
		//loadRegister();
		if (register.containsKey(nodeHash)) {
			register.remove(nodeHash);
			System.out.println(nodeHash + " is removed from the register");
		} else {
			System.out.println("There is no corresponding entry in the register");
		}
	}

	/**
	 * This method calculates the ownership of the files. 
	 * @param fileName: the name of the file we want the IPAddress from where it is stored
	 * @return nodeIP: this is string that will contains the IPAddress of the node containing the file
	 */
	public String getFileLocation(String fileName) {
		//loadRegister();
		int fileHash = hashing(fileName);
		System.out.println(fileHash);
		TreeMap<Integer, String> temp = new TreeMap<>();
		//if register is empty
		if (register.size() == 0) {
			System.out.println("there are no nodes in the network");
			return null;
		} else {
			// if register is not empty iterate over the register and search for hashvalues smaller than the filehash
			for (Entry<Integer, String> entry : register.entrySet()) {
				if (entry.getKey() < fileHash) {
					temp.put(entry.getKey(), entry.getValue());
				}
			}
		}
		// if the temp Treemap is empty take the node with biggest hash
		if (temp.size() == 0) {
			return register.get(register.lastKey());
		} else {
			//else get node with hash closest to filehash
			return temp.get(temp.lastKey());
		}
	}

	/**
	 * This method is extra for now, maybe it comes in handy later
	 * -------------------------------------------------------------
	 * This method looks up the hostIP in the register based on the hash
	 * @param hash: this is the hash (of the hostname) where we will calculate the IPAddress of.
	 * @return nodeIP: this will return a string which is the IPAddress of the node if the entry is found else it will return null
	 */
	public String getNodeIPFromHash(int nodeHash) {
		//loadRegister();
		if (register.containsKey(nodeHash)) {
			String nodeIP = register.get(nodeHash);
			System.out.println(nodeHash + ": " + nodeIP);
			return nodeIP;
		} else {
			System.out.println("The hash doesn't exist in the register");
			return null;
		}
	}

	/**
	 * This method is extra for now, maybe it comes in handy later
	 * -------------------------------------------------------------
	 * This method will look up the hashvalue of a node giving by it's IPAddres
	 * @param nodeIP: this is the ip address of the node
	 * @return hashNode: this is the hashvalue based on the IPAddres, if the value = -1 there is no corresponding entry
	 */
	public int getHashFromNodeIP(String nodeIP) {
		//loadRegister();
		int nodeHash = -1;
		for (Entry<Integer, String> entry : register.entrySet()) {
			if (entry.getValue().equals(nodeIP)) {
				nodeHash = entry.getKey();
			}
		}
		if (nodeHash == -1) {
			System.out.println("There is no corresponding entry for the given IPAddress ");
			return nodeHash;
		} else {
			System.out.println(nodeHash + ": " + nodeIP);
			return nodeHash;
		}
	}
	
	/**
	 * Get amount of entries in register
	 * -------------------------------------------------------------
	 * @return int: amount of entries
	 */
	public int getSize() {
		return register.size();
	}
}