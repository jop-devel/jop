package com.jopdesign.wcet.graphutils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.jopdesign.wcet.graphutils.TopOrder.BadGraphException;


/** Module to compute relative progess measure
 * Fun fact: While implementing, I suddenly recognized this is a generalized tree based WCET computation :) */
public  class  ProgressMeasure<V,E> {
	/* input */
	private LoopColoring<V, E> loopColors;
	private FlowGraph<V, E> cfg;
	private Map<V, Integer> loopBounds;
	private Map<V, Integer> blockMeasure;
	/* state */
	private HashMap<V, Long> pMaxMap;
	private HashMap<V, Long> loopProgress;

	public ProgressMeasure(FlowGraph<V,E> cfg, 
						   LoopColoring<V, E> loopColors,
			               Map<V, Integer> loopBounds, 
			               Map<V, Integer> blockMeasure) {
		this.cfg = cfg;
		this.loopBounds = loopBounds;
		this.loopColors = loopColors;
		this.blockMeasure = blockMeasure;
	}
	/**
	 * The fun algorithm to compute relative progress measures.
	 * @return
	 */
	public Map<V, Long> getMaxProgress() {
		/* pmax: maximal progress upto the given node */
		pMaxMap = new HashMap<V, Long>();
		loopProgress = new HashMap<V, Long>();
		/* for all loops in reverse topo order of the loop nest tree */
		List<V> reverseTopo = MiscUtils.reverseTopologicalOrder(loopColors.getLoopNestDAG());
		for(V hol: reverseTopo) {
			/* set progress of loop header to 0 */
			pMaxMap.put(hol, 0L);
			/* get linear subgraph */
			DirectedGraph<V, E> subGraph = loopColors.getLinearSubgraph(hol);
			/* update pmax (in topo order of the linear subgraph) */
			for(V node : MiscUtils.topologicalOrder(subGraph)) {
				if(node == hol) continue;
				updatePMax(subGraph, node, loopColors);
			}
			/* update loopProgress */
			long loopProgressMax = 0;
			for(E backEdge : loopColors.getBackEdgesByHOL().get(hol)) {
				loopProgressMax = Math.max(loopProgressMax, getMaxProgressVia(cfg,backEdge));
			}
			this.loopProgress.put(hol, loopProgressMax);
		}
		/* update pmax (linear subgraph of the method) */
		DirectedGraph<V, E> subGraph = loopColors.getLinearSubgraph(null);
		pMaxMap.put(cfg.getEntry(),0L);
		for(V node : MiscUtils.topologicalOrder(subGraph)) {
			if(node == cfg.getEntry()) continue;
			updatePMax(subGraph, node, loopColors);
		}
		return pMaxMap;
	}
	private void updatePMax(DirectedGraph<V, 
			                E> subgraph,V node, 
			                LoopColoring<V, E> color) {
		long pMax = 0;
		for(E incoming : subgraph.incomingEdgesOf(node)) {
			pMax = Math.max(pMax, getMaxProgressVia(subgraph,incoming));
		}
		pMaxMap.put(node,pMax);
	}
	private long getMaxProgressVia(DirectedGraph<V, E> subgraph, E incoming) {
		V src = subgraph.getEdgeSource(incoming);
		long pMaxPred = pMaxMap.get(src);
		for(V exitLoop : loopColors.getLoopExitSet(incoming)) {
			pMaxPred += loopProgress.get(exitLoop) * loopBounds.get(exitLoop);
		}
		return pMaxPred + blockMeasure.get(src);
	}
	/* test */
	public static void main(String argv[]) {
		int nodes[] = { 1,2,3,4,5,6,7,8,9,10,11};
		int blockm[] = { 1,1,22,4,4,4,2,1,1,1,1};
		int edges[][] = { {1,2},{2,3},{2,4},{4,5},
				          {5,6},{6,7},{6,2},{7,5},{7,10},
				          {5,8},{3,9},{8,9},{9,2},
				          {8,10},{9,11},{10,11} };
		FlowGraph<Integer,DefaultEdge> testGraph = 
			new DefaultFlowGraph<Integer,DefaultEdge>(DefaultEdge.class,0,11);
		for(int n : nodes) testGraph.addVertex(n);
		for(int[] e : edges) testGraph.addEdge(e[0],e[1]);
		
		Map<Integer,Integer> testLoopBounds = new HashMap<Integer, Integer>();
		testLoopBounds.put(2,7); testLoopBounds.put(5,15);
		
		TopOrder<Integer, DefaultEdge> topo = null;
		try {
			topo = new TopOrder<Integer, DefaultEdge>(testGraph,1);
		} catch (BadGraphException e1) {
			e1.printStackTrace();
		}
		LoopColoring<Integer,DefaultEdge> testLoopColors 
			= new LoopColoring<Integer, DefaultEdge>(testGraph, topo , 11);
		HashMap<Integer,Integer> testBlockMeasure = new HashMap<Integer, Integer>();
		for(int i = 0; i < nodes.length; i++) {
			testBlockMeasure.put(nodes[i],blockm[i]);
		}
		ProgressMeasure<Integer,DefaultEdge> pm = 
			new ProgressMeasure<Integer,DefaultEdge>(testGraph,testLoopColors,testLoopBounds,testBlockMeasure );
		MiscUtils.printMap(System.out,new TreeMap<Integer,Long>(pm.getMaxProgress()), 10);
	}
}
