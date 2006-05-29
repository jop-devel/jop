package com.jopdesign.wcet;

import com.jopdesign.build.AppInfo;
import com.jopdesign.util.*;

public class CallGraph extends AppInfo {

	public CallGraph(String[] args) {
		super(args);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CallGraph la = new CallGraph(args);
		
		System.out.println("in CallGraph");
		
		Graph cg = new Graph();
		Vertex v1 = cg.addVertex("abc");
		Vertex v2 = cg.addVertex("def");
		Vertex v3 = cg.addVertex("XYZ");
		cg.addEdge(v1, v2);
		cg.addEdge(v1, v3);
		
		
		
		System.out.println(cg.printGraph());
	}

}
