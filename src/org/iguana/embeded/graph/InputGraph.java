/**
 * 
 */
package org.iguana.embeded.graph;
import org.iguana.util.Input;

import java.util.List;

/**
 * @author Ragozina Anastasiya
 *
 */
public class InputGraph {
	
	private List<Edge>[] adjacencyList; 
	
	private int startVertix;
	
	private List<Integer> finalVertices;
	
	public InputGraph(List<Edge>[] a, int s, List<Integer> f) {
		this.adjacencyList = a;
		this.startVertix = s;
		this.finalVertices = f;
	}
	
	public int getStartsVertices() {
		return this.startVertix;
	}
	
	public boolean isFinal(int v) {
		return finalVertices.contains(v);
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
