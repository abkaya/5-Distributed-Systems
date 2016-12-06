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

	private static TreeMap<String, String> register;
	private String fileName = "NSRegister.ser";

	/**
	 * Auto generated constructor
	 * @param clear: if clear = 'true' clear previous register, if clear = 'false' continue with previous register
	 */
	public NameServerRegister(boolean clear) {
		// TODO Auto-generated 	constructor stub

		// The integer value is the hash calculated and the String is the IPAddres of the host/node
		register = new TreeMap<String, String>();

		//if (clear) {
		//loadRegister();
		//register.clear();
		//saveRegister();
		//System.out.println("Registered cleared and loaded");
		//} else {
		SystemyLogger.log(Level.INFO, logName + "Register loaded");
		//System.out.println("NameServerRegister >> Register loaded");
		//}
	}

	/**
	 * This method loads the register from the hard drive
	 */
	@SuppressWarnings("unchecked")
	public void loadRegister() {
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(fileName));
			//cast to Treemap<String, String>)
			register = (TreeMap<String, String>) objectInputStream.readObject();
			objectInputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
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
		//loadRegister();
		String nodeHash = String.valueOf(hashing(hostName));
		if (register.containsKey(nodeHash)) {
			SystemyLogger.log(Level.WARNING, logName + "This node already exist");
		} else {
			register.put(nodeHash, hostIP);
			SystemyLogger.log(Level.INFO,
					logName + hostName + " (hashcode: " + nodeHash + ")" + hostIP + " is added to the register");
		}
		//saveRegister();
	}

	/**
	 * This method removes a node from the register based on his hash code
	 * @param nodeName: this is the name of the node that's need to be removed
	 */
	public void removeNodeFromRegister(String nodeHash) {
		//loadRegister();
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
	public String getFileLocation(String fileHash) {

		//loadRegister();

		System.out.println(fileHash);
		TreeMap<String, String> temp = new TreeMap<>();
		//if register is empty
		if (register.size() == 0) {
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
			return null;
		} else {
			// if register is not empty iterate over the register and search for hashvalues smaller than the filehash
			for (Entry<String, String> entry : register.entrySet()) {
				if (Integer.parseInt(entry.getKey()) < Integer.parseInt(fileHash)) {
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
	 * This method will calculate the hash value of the next node based on his own
	 * hash value
	 * @param nodeHash: this is the hashvalue of the current node in the form of a string 
	 * @return This returns the next node IPAddress in the form of a string
	 */
	public String getNextNode(String nodeHash) {

		String tempKey = null;

		//loadRegister();

		//if there are no nodes in the network return null
		if (register.size() == 0) {
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
			return null;
			// if there is one node in the network point to himself
		} else if (register.size() == 1) {
			SystemyLogger.log(Level.INFO, logName + "This node is the only node in the network");
			return register.get(nodeHash);
			// if node is the last node in the network, point to the first one (ring network)
		} else if (register.lastKey() == nodeHash) {
			SystemyLogger.log(Level.INFO, logName + "This is the nextNode " + register.firstKey() + " ("
					+ register.get(register.firstKey()) + ")");
			return register.get(register.firstKey());
			//if this is all not the case then find the nextnode in the network
		} else {
			loop: for (Entry<String, String> entry : register.entrySet()) {
				if (Integer.parseInt(entry.getKey()) > Integer.parseInt(nodeHash)) {
					tempKey = entry.getKey();
					break loop;
				}
			}

			SystemyLogger.log(Level.INFO,
					logName + "This is the nextNode " + tempKey + " (" + register.get(tempKey) + ")");
			return register.get(tempKey);
		}

	}

	/**
	 * This method will calculate the hash value of the previous node based on his own
	 * hash value
	 * @param nodeName: this is the name of the current node
	 * @return: this is the hash value of the previous node (calculated with the parameter nodeHash)
	 */
	public String getPreviousNode(String nodeHash) {

		int tempKey = 0;

		//loadRegister();

		if (register.size() == 0) {
			SystemyLogger.log(Level.WARNING, logName + "There are no nodes in the network");
			return null;
		} else if (register.size() == 1) {
			SystemyLogger.log(Level.INFO, logName + "This node is the only node in the network");
			return register.get(nodeHash);
		} else if (register.firstKey() == nodeHash) {
			SystemyLogger.log(Level.INFO, logName + "This is the nextNode " + register.firstKey() + " ("
					+ register.get(register.firstKey()) + ")");
			return String.valueOf(register.firstKey());
		} else {
			loop: for (Entry<String, String> entry : register.entrySet()) {
				if (Integer.parseInt(entry.getKey()) < Integer.parseInt(nodeHash)) {
					tempKey = Integer.parseInt(entry.getKey());
					break loop;
				}
			}
			SystemyLogger.log(Level.INFO, logName + "getPreviousNode >> This is the previousNode " + tempKey + " ("
					+ register.get(tempKey) + ")");
			return register.get(tempKey);
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
		if (register.containsKey(String.valueOf(nodeHash))) {
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
	public int getHashFromNodeIP(String nodeIP) {
		//loadRegister();
		int nodeHash = -1;
		for (Entry<String, String> entry : register.entrySet()) {
			if (entry.getValue().equals(nodeIP)) {
				nodeHash = Integer.parseInt(entry.getKey());
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
	}
}