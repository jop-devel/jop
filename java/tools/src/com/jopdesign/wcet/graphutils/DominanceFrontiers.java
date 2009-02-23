package com.jopdesign.wcet.graphutils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
/**
 * Implementation of dominance frontiers / control dependencies.
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @see 
 * 	 <p>Efficiently computing SSA form and the control dependency graph.<br/>
 *     R.Cytron, J. Ferrante, B.Rosen and M.Wegman/</p> 
 * @param <V>
 * @param <E>
 */
public class DominanceFrontiers<V,E> {

	private DirectedGraph<V, E> graph;
	private V entry;
	private Dominators<V,E> doms;
	private EdgeReversedGraph<V, E> rGraph;
	private Dominators<V, E> rDoms;
	private V exit;

	public DominanceFrontiers(DirectedGraph<V, E> g, V entry, V exit) {
		this.graph = g;
		this.entry = entry;
		this.exit = exit;
	}
	/**
	 * get the graph's dominance frontiers.
	 */
	public Map<V,Set<V>> getDominanceFrontiers() {
		if(doms == null) doms = new Dominators<V, E>(graph,entry);
		return getDF(graph,doms);
	}
	public Map<V, Set<E>> getControlDependencies() {
		if(rGraph == null) rGraph = new EdgeReversedGraph<V, E>(graph);
		if(rDoms == null)  rDoms = new Dominators<V, E>(rGraph,exit);
		Map<V, Set<V>> controlDeps = getDF(rGraph,rDoms);
		Map<V, Set<E>> controlDepEdges = new HashMap<V, Set<E>>();
		for(Entry<V, Set<V>> nodeDeps: controlDeps.entrySet()) {
			V node = nodeDeps.getKey();
			Set<E> depEdges = new HashSet<E>();
			for(V controller : nodeDeps.getValue()) {
				// one of the successors is postdominated by node
				for(E edge : graph.outgoingEdgesOf(controller)) {
					V target = graph.getEdgeTarget(edge);
					if(target.equals(node)) depEdges.add(edge);
					else if(rDoms.dominates(node, target)) depEdges.add(edge);					
				}
			}
			controlDepEdges.put(node,depEdges);
		}
		return controlDepEdges;
	}
	/** TODO: rather slow implementation */
	private Map<V,Set<V>> getDF(DirectedGraph<V,E> g, Dominators<V,E> doms) {
		SimpleDirectedGraph<V, DefaultEdge> domTree = doms.getDominatorTree();
		TopologicalOrderIterator<V, DefaultEdge> bottomUp = 
			new TopologicalOrderIterator<V, DefaultEdge>(new EdgeReversedGraph<V,DefaultEdge>(domTree));
		HashMap<V,Set<V>> domFrontiers =
			new HashMap<V, Set<V>>();
		while(bottomUp.hasNext()) {
			V n = bottomUp.next();
			Set<V> domFront = new HashSet<V>();
			for(E e : g.outgoingEdgesOf(n)) {
				V succ = g.getEdgeTarget(e);
				if(doms.getIDom(succ) != n) domFront.add(succ);
			}
			for(DefaultEdge e : domTree.outgoingEdgesOf(n)) {
				V domKid = domTree.getEdgeTarget(e);
				for(V kidDF : domFrontiers.get(domKid)) {
					if(doms.getIDom(kidDF) != n) domFront.add(kidDF);
				}
			}
			domFrontiers.put(n, domFront);
		}		
		return domFrontiers;
	}
//	public Collection<Set<V>> getSingleEntrySingleExitSets() {
//	if(doms == null) doms = new Dominators<V, E>(graph,entry);
//	Map<V, Set<E>> controlDeps = getControlDependencies();
//	Map<Set<E>,Set<V>> ccPartMap = new HashMap<Set<E>, Set<V>>();
//	for(Entry<V, Set<E>> controlDepEntry : controlDeps.entrySet()) {
//		V node = controlDepEntry.getKey();
//		Set<E> controlPartKey = controlDepEntry.getValue();
//		if(! ccPartMap.containsKey(controlPartKey)) {
//			ccPartMap.put(controlPartKey, new HashSet<V>());
//		}
//		Set<V> part = ccPartMap.get(controlPartKey);
//		part.add(node);
//	}
//	return ccPartMap.values();
//}

}
