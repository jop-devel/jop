/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	
	public Vertex addVertex(Vertex v) {
		vmap.put(v, v);
		return v;
	}
	
	public Vertex findVertex(Object data) {
		return (Vertex) vmap.get(data);
	}
	
	public void addEdge(Vertex from, Vertex to) {
		
		from.succ.add(to);
		to.pred.add(from);
		System.out.println(from.succ);
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
	    
	    System.out.println("******************");
	    
	    Iterator iter = vmap.values().iterator();
	    while (iter.hasNext()) {
	    	Vertex v = (Vertex) iter.next();
	    	System.out.println(v);
	    	System.out.println("\t"+v.succ);
	    	for (Iterator eit = v.getSucc().iterator(); eit.hasNext(); ) {
	    		Vertex suc = (Vertex) eit.next();
	    		sb.append("\t\""+v.toDotString()+"\" -> \""+suc.toDotString()+"\"\n");
	    	}
	    }

	    
		sb.append("}\n");
		return sb.toString();
	}
}


