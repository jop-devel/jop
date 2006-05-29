package com.jopdesign.util;

import java.util.*;

/**
 * Utility class for manipulation of graphs
 * TODO: find a library that does exactly this and
 * is not too big.
 * @author martin, rasmus
 *
 */
public class Graph {

	HashMap vmap = new HashMap();
	
	public Vertex addVertex(Object data) {
		Vertex v = new Vertex(data);
		vmap.put(data, v);
		return v;
	}
	
	public Vertex findVertex(Object data) {
		return (Vertex) vmap.get(data);
	}
	
	public void addEdge(Vertex from, Vertex to) {
		from.succ.add(to);
		to.pred.add(from);
	}
	
	/**
	 * Generate dot graph
	 * 
	 * @return
	 */
	public String printGraph() {
		
		StringBuffer sb = new StringBuffer();
	    sb.append("digraph G {\n");
	    sb.append("size = \"10,7.5\"\n");
	    
	    Iterator iter = vmap.entrySet().iterator();
	    while (iter.hasNext()) {
	    	Map.Entry me = (Map.Entry) iter.next();
	    	Vertex v = (Vertex) me.getValue(); 
	    	for (Iterator eit = v.getSucc().iterator(); eit.hasNext(); ) {
	    		Vertex suc = (Vertex) eit.next();
	    		sb.append("\t"+v.toString()+" -> "+suc.toString()+"\n");
	    	}
	    }

	    
		sb.append("}\n");
		return sb.toString();
	}
}


