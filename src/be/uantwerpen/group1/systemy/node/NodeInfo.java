package be.uantwerpen.group1.systemy.node;

import java.util.logging.Level;

import be.uantwerpen.group1.systemy.logging.SystemyLogger;
import be.uantwerpen.group1.systemy.networking.Hashing;

/**
 * abstract data type for storing node info
 * 
 * @author Robin Janssens
 */
public class NodeInfo implements Comparable<NodeInfo> {

	private static String logName = NodeInfo.class.getName() + " >> ";
	
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
	public NodeInfo(String nodeName, String nodeIP) {
		this.setName(nodeName);
		this.setIP(nodeIP);
	}
	
	/**
	 * empty object Constructor
	 */
	public NodeInfo() {
		this.setName(null);
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
	public String getIP() {
		return ip;
	}

	// -----
	// SET
	// -----
	public void setName(String nodeName) {
		this.name = nodeName;
		this.hash = Hashing.hash(nodeName);
	}
	public void setIP(String nodeIP) {
		this.ip = nodeIP;
	}
	
	/**
	 * function to check if this node is a new next node
	 * 
	 * @param me: own NodeInfo
	 * @param next: next node NodeInfo
	 * @return boolean: true if this is a new next node
	 */
	public boolean isNewNext(NodeInfo me, NodeInfo next) {
		if (me == null) {
			// own hash not known
			SystemyLogger.log(Level.SEVERE, logName + "NodeInfo >> own hash not known");
			return false;
		} else if (next == null) {
			// no next node known yet -> make this one next
			return true;
		} else {
			// we need to look in to this
			if (next.getHash() > me.getHash()) {
				// next is bigger than me
				if (me.getHash() < this.getHash() && this.getHash() < next.getHash()) {
					// if between
					return true;
				} else {
					// not relevant
					return false;
				}
			} else if (next.getHash() < me.getHash()) {
				// next is smaller than me
				if (me.getHash() < this.getHash() || this.getHash() < next.getHash()) {
					// only one bigger then me OR smaller than next
					return true;
				} else {
					// not relevant
					return false;
				}
			} else {
				// next == me
				return true;
			}
		}
	}
	
	/**
	 * function to check if this node is a new previous node
	 * 
	 * @param me: own NodeInfo
	 * @param previous: previous node NodeInfo
	 * @return boolean: true if this is a new previous node
	 */
	public boolean isNewPrevious(NodeInfo me, NodeInfo previous) {
		if (me == null) {
			// own hash not known
			SystemyLogger.log(Level.SEVERE, logName + "NodeInfo >> own hash not known");
			return false;
		} else if (previous == null) {
			// no previous node known yet -> make this one previous
			return true;
		} else {
			// we need to look in to this
			if (previous.getHash() < me.getHash()) {
				// previous is smaller than me
				if (previous.getHash() < this.getHash() && this.getHash() < me.getHash()) {
					// if between
					return true;
				} else {
					// not relevant
					return false;
				}
			} else if (me.getHash() < previous.getHash()) {
				// previous is bigger than me
				if (this.getHash() < me.getHash()  || previous.getHash() < this.getHash()) {
					// only one smaller then me OR bigger than previous
					return true;
				} else {
					// not relevant
					return false;
				}
			} else {
				// next == me
				return true;
			}
		}
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
	
	/**
	 * Comparable implementation
	 * 
	 * @param other: NodeInfo to compare with
	 * @return int: outcome of comparison
	 */
	@Override
	public int compareTo(NodeInfo other) {
		if ( this.getHash() > other.getHash() )
			return 1;
		else if ( this.getHash() < other.getHash() )
			return -1;
		else
			return 0;
	}
	
	
}
