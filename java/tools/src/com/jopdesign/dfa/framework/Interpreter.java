package com.jopdesign.dfa.framework;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.InstructionHandle;

public class Interpreter<K, V> {

	private Analysis<K, V> analysis;
	private AppInfo program;
	
	public Interpreter(Analysis<K, V> a, AppInfo p) {
		program = p;
		analysis = a;
	}
	
	public AppInfo getProgram() {
		return program;
	}
	
	public Map<InstructionHandle, ContextMap<K, V>> interpret(Context context,
			InstructionHandle entry,
			Map<InstructionHandle, ContextMap<K, V>> state,
			boolean start) {
		
		LinkedList<FlowEdge> worklist = new LinkedList<FlowEdge>();

		for (Iterator<FlowEdge> i = program.getFlow().getOutEdges(entry).iterator(); i.hasNext(); ) {
			FlowEdge f = i.next();
			if (entry.equals(f.getTail())) {
				worklist.add(new FlowEdge(f, context));
			}
 		}

		Map<InstructionHandle, ContextMap<K, V>> result = state;
		
		if (start) {
			for (Iterator<InstructionHandle> i = program.getStatements().iterator(); i.hasNext(); ) {
				InstructionHandle s = i.next();
				result.put(s, analysis.bottom());
			}		
			result.put(entry, analysis.initial(entry));
		}
		
		while(!worklist.isEmpty()) {
			
			FlowEdge edge = worklist.removeFirst();
			//System.out.println("computing: "+edge);
			InstructionHandle tail = edge.getTail();
			InstructionHandle head = edge.getHead();

			ContextMap<K, V> tailSet = result.get(tail);
			tailSet.setContext(edge.getContext()); 
			ContextMap<K, V> transferred = analysis.transfer(tail, edge, tailSet, this, result);
			ContextMap<K, V> headSet = result.get(head);

			if (!analysis.compare(transferred, headSet)) {
				
				ContextMap<K, V> joinedSet = analysis.join(headSet, transferred);				
				result.put(head, joinedSet);
				
				Set<FlowEdge> outEdges = program.getFlow().getOutEdges(head);
				if (outEdges != null) {
					for (Iterator<FlowEdge> i = outEdges.iterator(); i.hasNext(); ) {
						FlowEdge f = new FlowEdge(i.next(), transferred.getContext());
						if (worklist.isEmpty() || !worklist.getFirst().equals(f)) {
							worklist.addFirst(f);
							//System.out.println("pushing: "+f);
						}
					}
				}
			}
			
			//System.out.println("worklist: "+worklist);
		}

		return result;
	}
	
}
