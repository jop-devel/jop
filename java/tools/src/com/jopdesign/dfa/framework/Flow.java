package com.jopdesign.dfa.framework;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.generic.InstructionHandle;

public class Flow {

	Map<InstructionHandle, Set<FlowEdge>> flow;
	
	public Flow() {
		flow = new LinkedHashMap<InstructionHandle, Set<FlowEdge>>();
	}
	
	public void addEdge(FlowEdge f) {
		Set<FlowEdge> set = flow.get(f.getTail());
		if (set == null) {
			set = new LinkedHashSet<FlowEdge>();
			flow.put(f.getTail(), set);	
		}
		set.add(f);
	}
	
	public Set<FlowEdge> getOutEdges(InstructionHandle h) {
		return flow.get(h);
	}
	
}
