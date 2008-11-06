package com.jopdesign.wcet08.graphutils.test;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.Vector;

import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;

import com.jopdesign.wcet08.graphutils.Dominators;
import com.jopdesign.wcet08.graphutils.LoopColoring;
import com.jopdesign.wcet08.graphutils.TopOrder;
import com.jopdesign.wcet08.graphutils.TopOrder.BadGraphException;

import static junit.framework.Assert.*;
public class GraphTopologyTest {
	private static final Integer ENTRY_VERTEX = 0;
	
	private static class EdgeCmp implements Comparator<DefaultEdge> {
		private DirectedGraph<Integer, DefaultEdge> g;
		public EdgeCmp(DirectedGraph<Integer, DefaultEdge> g) {
			this.g = g;
		}
		public int compare(DefaultEdge o1, DefaultEdge o2) {
			int r1 = g.getEdgeSource(o1).compareTo(g.getEdgeSource(o2));
			if(r1 == 0) return g.getEdgeTarget(o1).compareTo(g.getEdgeTarget(o2));
			else return r1;
		}		
	}
	public static DefaultDirectedGraph<Integer,DefaultEdge> mkGraph(int[] vxs, int[][]edges) {
		DefaultDirectedGraph<Integer, DefaultEdge> gr = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for(int v : vxs) {
			gr.addVertex(v);
		}
		for(int[] e: edges) {
			gr.addEdge(e[0], e[1]);
		}
		return gr;
	}
	public static TreeSet<DefaultEdge> mkEdgeSet(DefaultDirectedGraph<Integer,DefaultEdge> gr,int [][] es) {
		TreeSet<DefaultEdge> v = new TreeSet<DefaultEdge>(new EdgeCmp(gr));
		for(int[] e : es) { v.add(gr.getEdge(e[0], e[1])); }
		return v;
	}
	public static TreeSet<DefaultEdge> mkEdgeSet(DefaultDirectedGraph<Integer,DefaultEdge> gr,
												 Vector<DefaultEdge> edges) {
		TreeSet<DefaultEdge> v = new TreeSet<DefaultEdge>(new EdgeCmp(gr));
		v.addAll(edges);
		return v;
	} 

	public static int[] vxs1 = { 0,1,2,3,4,5,6,7 };
	public static int[][] edges1 = {
		{0,1},
		{1,2},
		{2,3},
		{3,4},{3,5},
		{4,6},{4,2},
		{5,7},{5,1},
		{7,6}
	};
	public static int[][] backEdges1 = {
		{4,2},{5,1}
	};
	public static int[][] toporders1 = {
		{1,2,3,4,5,6,7}, // 0
		{2,3,4,5,6,7}, // 1
		{3,4,5,6,7},   // 2
		{4,5,6,7},     // 3
		{6},           // 4
		{6,7},         // 5
		{},            // 6
		{6}            // 7
	};
	public static int[] idoms1 = { 0,0,1,2,3,3,3,5 };
	
	private DefaultDirectedGraph<Integer, DefaultEdge> gr1;
	private TopOrder<Integer, DefaultEdge> to1;

	@Before
	public void setUp() throws Exception {
		gr1 = mkGraph(vxs1,edges1);
		to1 = new TopOrder<Integer,DefaultEdge>(gr1,ENTRY_VERTEX);
	}
	@Test
	public void testBackEdges1() {
		assertEquals(mkEdgeSet(gr1,to1.getBackEdges()),mkEdgeSet(gr1, backEdges1));
	}
	@Test
	public void testTopOrder1() {
		Hashtable<Integer, Integer> preOrder = to1.getTopOrder();
		for(int i = 0; i < vxs1.length; i++) {
			int[] succs = toporders1[i];
			for(int j : succs) {
				assertEquals(true, preOrder.get(vxs1[i]) < preOrder.get(j));
			}
		}
	}
	@Test
	public void testDominators1() {
		Dominators<Integer, DefaultEdge> doms1 = new Dominators<Integer, DefaultEdge>(gr1,to1.getDfsOrder());
		Hashtable<Integer, Integer> idoms = doms1.getIDoms();
		for(int i  = 0; i < vxs1.length; i++) {
			assertEquals(idoms.get(vxs1[i]).intValue(),idoms1[i]);
		}
	}
	
	// small demo
	static class StringVertexFactory implements VertexFactory<String> {
		private int genId;
		public StringVertexFactory(DirectedGraph<String,DefaultEdge> g) {
			this.genId = g.vertexSet().size() + 1;			
		}
		public String createVertex() {
			return ("v"+(genId++));
		}		
	}
	public static void main(String argv[]) {
		DefaultDirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		g.addVertex("Entry");
		g.addVertex("1");g.addVertex("2");g.addVertex("3");
		g.addVertex("4");g.addVertex("5");g.addVertex("6");
		g.addVertex("7");g.addVertex("8");g.addVertex("9");g.addVertex("10");
		g.addEdge("Entry", "1");
		g.addEdge("1", "2");g.addEdge("2", "3");g.addEdge("3", "4");g.addEdge("3", "5");
		g.addEdge("4", "6");g.addEdge("4", "2");g.addEdge("5", "6");g.addEdge("5", "1");
		g.addEdge("1","7");g.addEdge("7","8");g.addEdge("8", "8");g.addEdge("8","9");
		g.addEdge("6", "10");g.addEdge("9", "10");g.addEdge("9", "7");

		System.out.println("Graph: "+g);
		exportDOT("g1-cfg.dot",g);
		TopOrder<String, DefaultEdge> topOrder;
		try {
			topOrder = new TopOrder<String,DefaultEdge>(g,"Entry");
		} catch (BadGraphException e) {
			e.printStackTrace();
			System.exit(1);
			topOrder = null;
		}
		System.out.println("DfsOrder: "+topOrder.getDfsOrder());
		System.out.println("TopOrder: "+topOrder.getTopOrder());
		System.out.println("Back-Edges: "+topOrder.getBackEdges());
		Dominators<String,DefaultEdge> doms = 
			new Dominators<String,DefaultEdge>(g,topOrder.getDfsOrder());
		System.out.println("Dominators: "+doms.getIDoms());
		LoopColoring<String, DefaultEdge> loopColoring = 
			new LoopColoring<String, DefaultEdge>(g,topOrder);
		System.out.println("Loop coloring: "+loopColoring.getLoopColors());
		System.out.println("Loop nest tree: "+loopColoring.getLoopNestForest());
		exportDOT("g1-lnf.dot",loopColoring.getLoopNestForest());		
		StringVertexFactory vf = new StringVertexFactory(g);
		loopColoring.unpeelLoop("8", vf);
		System.out.println("Graph (unpeel 8): "+g);
		exportDOT("g1-up8.dot",g);				
		loopColoring.unpeelLoop("7", vf);
		System.out.println("Graph (unpeel 7,8): "+g);
		exportDOT("g1-up7.dot",g);
		loopColoring.unpeelLoop("2", vf);
		loopColoring.unpeelLoop("1", vf);
		System.out.println("Graph (unpeel all): "+g);
		exportDOT("g1-up-all.dot",g);
	}
	private static void exportDOT(String file, DirectedGraph<String, DefaultEdge> g) {
		DOTExporter<String, DefaultEdge> export = new DOTExporter<String, DefaultEdge>(
			new IntegerNameProvider<String>(),
			new StringNameProvider<String>(),
			null
		);
		try {
			File tmpFile = File.createTempFile(file, ".dot");
			System.out.println("Creating .dot file: "+tmpFile);
			FileWriter fw = new FileWriter(tmpFile);
			export.export(fw, g);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
