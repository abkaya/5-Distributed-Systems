/*
 * This class is the register of the NameServer. It will load/save and calculate the hash for the nodes
 * 
 * Convention
 * -------------
 * If a host is not added to the network the terms hostName and hostIP are used
 * If a host is added to the network it becomes a node and the terms nodeName and nodeIP are used
 * 
 * @author	Mariën Levi
 * @version 1.0
 * @since 29/10/2016
 * 
 */

package be.uantwerpen.systemy.nameserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
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

	private static Map<Integer, String> register;
	private String fileName = "NSRegister.ser";

	/*
	 * Auto generated constructor
	 * @param clear: if clear = 'true' clear previous register, if clear = 'false' continue with previous register
	 */
	public NameServerRegister(boolean clear) {
		// TODO Auto-generated constructor stub

		register = new TreeMap<Integer, String>();

		if (clear) {
			loadRegister();
			register.clear();
			saveRegister();
			System.out.println("Registered cleared and loaded");
		} else {
			loadRegister();
			System.out.println("Register loaded");

		}

	}

	/*
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

	/*
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

	/*
	 * Converts a string to hashcode (from 0 - 32768, see specifications)
	 * @param name: the name where we need to calculate the hash from
	 */
	public int hashing(String nameToConvert) {
		int res = Math.abs(nameToConvert.hashCode());
		res = res % 32768;
		return res;
	}

	/*
	 * This method adds a host as a node to the register
	 * @param hostName: hostname of the new node
	 * @param hostIP: IPAddress of the new node
	 */
	public void addNode(String hostName, String hostIP) {
		loadRegister();
		int nodeHash = hashing(hostName);
		if (register.containsKey(nodeHash)) {
			System.out.println("This node already exist");
		} else {
			register.put(nodeHash, hostIP);
			System.out.println(hostName + " ( hashcode: " + nodeHash + "): " + hostIP + "is added to the register");
		}
		saveRegister();

	}

	/*
	 * This method looks up the hostIP in the register based on the hash
	 * @param hash: this is the hash (of the hostname) where we will calculate the IPAddress of.
	 * @return nodeIP: this will return a string which is the IPAddress of the node if the entry is found else it will return null
	 */
	public String getNodeIP(int hash) {
		loadRegister();
		if (register.containsKey(hash)) {
			String nodeIP = register.get(hash);
			System.out.println(hash + ": " + nodeIP);
			return nodeIP;
		} else {
			System.out.println("The hash doesn't exist in the register");
			return null;
		}

	}

	/*
	 * This method will look up the hashvalue of a node giving by it's IPAddres
	 * @param nodeIP: this is the ip address of the node
	 * @return hashNode: this is the hashvalue based on the IPAddres, if the value = -1 there is no corresponding entry
	 */
	public int getHashFromNodeIP(String nodeIP) {
		loadRegister();
		int nodeHash = -1;
		for (Entry<Integer, String> entry : register.entrySet()) {
			if (entry.getValue().equals(nodeIP)) {
				nodeHash = Integer.parseInt(entry.getValue());
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

	/*
	 * This method removes a node from the register based on his hash code
	 * @param nodeHash: this is the hash of the node that's need to be removed
	 */
	public void removeNodeFromRegister(int nodeHash) {
		loadRegister();
		if (register.containsKey(nodeHash)) {
			register.remove(nodeHash);
			System.out.println(nodeHash + " is removed from the register");
		} else {
			System.out.println("There is no corresponding entry in the register");
		}
	}
}