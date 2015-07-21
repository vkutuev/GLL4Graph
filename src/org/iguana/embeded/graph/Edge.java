/**
 * 
 */
package org.iguana.embeded.graph;

/**
 * @author Ragozina Anastasiya
 *
 */
public class Edge {
	private String tag;
	private int destVertix;
	
	public Edge(String tag, int dest) {
		this.tag = tag;
		this.destVertix = dest;
	}
	
	public int getDestVertix() {
		return this.destVertix;
	}
	
	public String getTag() {
		return this.tag;
	}
}
