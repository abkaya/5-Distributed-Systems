package be.uantwerpen.group1.systemy.test;

import java.util.Arrays;
import java.util.Random;

import be.uantwerpen.group1.systemy.node.NodeInfo;

public class NodeInfo_test {

	public static void main(String args[]) {
		
		// ------------------------------
		// create 20 nodes
		// ------------------------------
		NodeRepresentation[] nodes = new NodeRepresentation[20];
		Random random = new Random();
		for (int i = 0; i<20; i++ ) {
			NodeInfo newNode = new NodeInfo();
			int r = random.nextInt(1000);
			newNode.setName("node" + r);
			newNode.setHash( Math.abs((newNode.getName().hashCode()) % 32768) );
			NodeRepresentation addedNode = new NodeRepresentation();
			addedNode.me = newNode;
			nodes[i] = addedNode;
			System.out.println( "Node added: " + newNode.toString() );
		}
		
		// ------------------------------
		// look for next and previous
		// ------------------------------
		for (int i = 0; i<20; i++ ) {
			for (int j = 0; j<20; j++ ) {
				if ( nodes[j].me.isNewPrevious(nodes[i].me,nodes[i].previousNode) )
					nodes[i].previousNode = nodes[j].me;
				if ( nodes[j].me.isNewNext(nodes[i].me,nodes[i].nextNode) )
					nodes[i].nextNode = nodes[j].me;
			}
		}
		
		// ------------------------------
		// sort on nodes own hash
		// ------------------------------
		Arrays.sort(nodes);
		
		// ------------------------------
		// display outcome
		// ------------------------------
		System.out.println();
		System.out.println("\t\t\t    Sorted ");
		System.out.println("\t\t\t   --------");
		System.out.println( " Previous Node " + "\t   |    " + "      Me      " + "\t   |    " + "   Next Node   " );
		System.out.println( " ------------- " + "\t   |    " + "      --      " + "\t   |    " + "   ---------   " );
		for (NodeRepresentation node : nodes)
			System.out.println( node.previousNode.toString() + "\t   |    " + node.me.toString() + "\t   |    " + node.nextNode.toString() );
		
		
		
	}
	
}
