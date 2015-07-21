/**
 * 
 */
package org.iguana.embeded.graph;

import java.util.List;


/**
 * @author Ragozina Anastasiya
 *
 */
public class InputGraph {
	
	private List<Edge>[] adjacencyList; 
	
	private List<Integer> startVerticies;
	
	private List<Integer> finalVerticies;
	
	public InputGraph(List<Edge>[] a, List<Integer> s, List<Integer> f) {
		this.adjacencyList = a;
		this.startVerticies = s;
		this.finalVerticies = f;
	}
	
	public List<Integer> getStartsVerticies() {
		return this.startVerticies;
	}
	
	public boolean isFinal(int v) {
		return finalVerticies.contains(v);
	}
	
	public int getDestVertix(int v, String t) {
		for(Edge edge : this.adjacencyList[v]) {
			if(edge.getTag() == t) {
				return edge.getDestVertix();
			}
		}
		return -1;
	}
}
