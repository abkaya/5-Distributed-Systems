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
 * Key = hash
 * Value = ip address
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
import java.util.logging.Level;
import java.util.TreeMap;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.networking.Hashing;

public class NameServerRegister implements Serializable {

	private static String logName = NameServerRegister.class.getName() + " >> ";

	/*
	 * Default number
	 */
	private static final long serialVersionUID = 1L;

	/* Parameter list
	 * ---------------------
	 * map: the map that contains the hashes and IPAddresses of the nodes (will be implemented as a Treemap)
	 * fileName: the name of the file for saving on the hard drive
	 */

	private static TreeMap<Integer, String> register;
	private String fileName = "NSRegister.ser";

	/**
	 * Auto generated constructor
	 * @param clear: if clear = 'true' clear previous register, if clear = 'false' continue with previous register
	 */
	public NameServerRegister() {
		// TODO Auto-generated 	constructor stub

		// The integer value is the hash calculated and the String is the IPAddres of the host/node
		register = new TreeMap<Integer, String>();
		SystemyLogger.log(Level.INFO, logName + "Register loaded");
		//System.out.println("NameServerRegister >> Register loaded");
	}

	/**
	 * This method loads the register from the hard drive
	 * Extra
	 */
	@SuppressWarnings("unchecked")
	public void loadRegister() {
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName));
			//cast to Treemap<String, String>)
			register = (TreeMap<Integer, String>) objectInputStream.readObject();
			objectInputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * This method saves the register to the hard drive
	 * Extra
	 */
	public void saveRegister() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));
			objectOutputStream.writeObject(register);
			objectOutputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
		}
	}

	/**
	 * This method returns the size of the register
	 * @return: the size of the register is returned as an integer
	 */
	public int getSize() {
		return register.size();
	}

	/**
	 * Converts a string to hashcode (from 0 - 32768, see specifications)
	 * @param name: the name where we need to calculate the hash from
	 */
	public int hashing(String nameToConvert) {
		return Hashing.hash(nameToConvert);
	}

	/**
	 * This method adds a host as a node to the register
	 * @param hostName: hostname of the new node
	 * @param hostIP: IPAddress of the new node
	 */
	public void addNode(String hostName, String hostIP) {
		int nodeHash = hashing(hostName);
		if (register.containsKey(nodeHash)) {
			SystemyLogger.log(Level.WARNING, logName + "This node already exist");
		} else {
			register.put(nodeHash, hostIP);
			SystemyLogger.log(Level.INFO,
					logName + hostName + " (hashcode: " + nodeHash + ")" + hostIP + " is added to the register");
		}
	}

	/**
	 * This method removes a node from the register based on his hash code
	 * @param nodeName: this is the name of the node that's need to be removed
	 */
	public void removeNodeFromRegister(Integer nodeHash) {
		if (register.containsKey(nodeHash)) {
			register.remove(nodeHash);
			SystemyLogger.log(Level.INFO, logName + nodeHash + " is removed from the register");
		} else {
			SystemyLogger.log(Level.WARNING, logName + "This node doesn't exist in the network");
		}
	}

	/**
	 * This method calculates the ownership of the files.
	 * @param fileName: the name of the file we want the IPAddress from where it is stored
	 * @return nodeIP: this is string that will contains the IPAddress of the node containing the file
	 */
	public String getFileLocation(int fileHash) {

		System.out.println(fileHash);
		TreeMap<Integer, String> temp = new TreeMap<>();
		//if register is empty
		if (register.size() == 0) {
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
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
			SystemyLogger.log(Level.INFO, logName + register.get(register.lastKey()) + " (" + register.lastKey()
					+ " ): is the owner of this file");
			return register.get(register.lastKey());
		} else {
			//else get node with hash closest to filehash
			SystemyLogger.log(Level.INFO,
					logName + temp.get(temp.lastKey()) + " (" + temp.lastKey() + " ): is the owner of this file");
			return temp.get(temp.lastKey());
		}
	}

	/**
	 * This method will lookup the hash value of the next node based on his own hash value
	 *
	 * @param nodeHash: String with hash of the current node
	 * @return Hash of next node
	 */
	public int getNextNode(int nodeHash) {
		if (register.size() == 0) {
			// If there are no nodes in the network return 0
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
			return 0;
		} else if (register.size() == 1) {
			// If there is one node in the network point to himself
			SystemyLogger.log(Level.INFO, logName + "This node is the only node in the network");
			return nodeHash;
		} else if (register.lastKey() == nodeHash) {
			// If node is the last node in the network, point to the first one (ring network)
			SystemyLogger.log(Level.INFO, logName + "This is the nextNode " + register.firstKey());
			return register.firstKey();
		} else {
			// If this is all not the case then find the next node in the network
			int tempKey = 0;
			loop: for (Entry<Integer, String> entry : register.entrySet()) {
				if (entry.getKey() > nodeHash) {
					tempKey = entry.getKey();
					break loop;
				}
			}
			SystemyLogger.log(Level.INFO, logName + "This is the nextNode " + tempKey);
			return tempKey;
		}

	}

	/**
	 * This method will lookup the hash value of the previous node based on his own hash value
	 *
	 * @param nodeHash: String with hash of the current node
	 * @return Hash of previous node
	 */
	public int getPreviousNode(int nodeHash) {
		if (register.size() == 0) {
			// If there are no nodes in the network return 0
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
			return 0;
		} else if (register.size() == 1) {
			// If there is one node in the network point to himself
			SystemyLogger.log(Level.INFO, logName + "This node is the only node in the network");
			return nodeHash;
		} else if (register.firstKey() == nodeHash) {
			// If node is the last node in the network, point to the first one (ring network)
			SystemyLogger.log(Level.INFO, logName + "This is the previousNode " + register.lastKey());
			return register.firstKey();
		} else {
			// If this is all not the case then find the next node in the network
			int tempKey = 0;
			loop: for (Entry<Integer, String> entry : register.entrySet()) {
				if (entry.getKey() < nodeHash) {
					tempKey = entry.getKey();
					break loop;
				}
			}
			SystemyLogger.log(Level.INFO, logName + "This is the previousNode " + tempKey);
			return tempKey;
		}
	}

	/**
	 * This method looks up the hostIP in the register based on the hash
	 * @param hash: this is the hash (of the hostname) of who we want to lookup the IPAddress from.
	 * @return nodeIP: this will return a string which is the IPAddress of the node if the entry is found else it will return null
	 */
	public String getNodeIPFromHash(int nodeHash) {
		if (register.containsKey(nodeHash)) {
			String nodeIP = register.get(nodeHash);
			SystemyLogger.log(Level.INFO, logName + "The hash: " + nodeHash + " correspond with ip address: " + nodeIP);
			return nodeIP;
		} else {
			SystemyLogger.log(Level.WARNING, logName + "The hash doesn't exist in the register");
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
	/*
	 *	Doesn't make sense because an IP address is not always unique 
	 */
	/*public int getHashFromNodeIP(String nodeIP) {
		//loadRegister();
		int nodeHash = -1;
		for (Entry<Integer, String> entry : register.entrySet()) {
			if (entry.getValue().equals(nodeIP)) {
				nodeHash = entry.getKey();
			}
		}
		if (nodeHash == -1) {
			SystemyLogger.log(Level.WARNING, logName + "The ip address doesn't exist in the register");
			return nodeHash;
		} else {
			SystemyLogger.log(Level.INFO,
					logName + "The ip address: " + nodeIP + " corresponds with hash: " + nodeHash);
			return nodeHash;
		}
	}*/
}
