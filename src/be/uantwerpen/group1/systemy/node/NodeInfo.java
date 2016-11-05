package be.uantwerpen.group1.systemy.node;

public class NodeInfo {

	private String name;
	private int hash;
	private String ip;
	
	/**
	 * abstract data type for storing node info
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

	// -----
	// GET
	// -----
	public String getName() {
		return name;
	}
	public int getHash() {
		return hash;
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
	
	
}
