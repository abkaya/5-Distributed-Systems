package be.uantwerpen.group1.systemy.test;

import be.uantwerpen.group1.systemy.node.NodeInfo;

/**
 * abstract data type for representing a node
 * part of NodeInfo_test
 * 
 * @author Robin Janssens
 */
public class NodeRepresentation implements Comparable<NodeRepresentation> {
	
	public NodeInfo me = null;
	public NodeInfo previousNode = null;
	public NodeInfo nextNode = null;
	
	@Override
	public int compareTo(NodeRepresentation other) {
		if ( this.me.getHash() > other.me.getHash() )
			return 1;
		else if ( this.me.getHash() < other.me.getHash() )
			return -1;
		else
			return 0;
	}
	
}
