package be.uantwerpen.group1.systemy.node;

/**
 * abstract data type for storing node info
 * 
 * @author Robin Janssens
 */
public class NodeInfo {

	private String name;
	private int hash;
	private String ip;
	
	/**
	 * constructor
	 * 
	 * @param nodeName
	 * @param nodeHash
	 * @param nodeIP
	 */
	public NodeInfo(String nodeName, int nodeHash, String nodeIP) {
		this.setName(nodeName);
		this.setHash(nodeHash);
		this.setIP(nodeIP);
	}
	
	/**
	 * empty object Constructor
	 */
	public NodeInfo() {
		this.setName(null);
		this.setHash(0);
		this.setIP(null);
	}

	// -----
	// GET
	// -----
	public String getName() {
		return name;
	}
	public int getHash() {
		return hash;
	}
	public int getPort() {
		return 3000 + ( hash % 999 );
	}
	public String getIP() {
		return ip;
	}

	// -----
	// SET
	// -----
	public void setName(String nodeName) {
		this.name = nodeName;
	}
	public void setHash(int nodeHash) {
		this.hash = nodeHash;
	}
	public void setIP(String nodeIP) {
		this.ip = nodeIP;
	}
	
	/**
	 * pretty output
	 * 
	 * @return String: formatted output
	 */
	public String toString() {
		return name + " (" + hash + ")";
	}
	
	/**
	 * output data as "<name>,<hash>,<ip>"
	 * ideal for sending data as a package
	 * 
	 * @return String: formatted output
	 */
	public String toData() {
		return name + "," + hash + "," + ip;
	}
	
	
}
