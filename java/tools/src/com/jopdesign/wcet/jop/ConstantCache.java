package com.jopdesign.wcet.jop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;

public class ConstantCache {

	private Project project;
	private Map<MethodInfo,Set<Integer>> addressMap =
		new HashMap<MethodInfo, Set<Integer>>();

	public ConstantCache(Project project) {
		this.project = project;
	}
	public ConstantCache build() {
		List<MethodInfo> methods = project.getCallGraph().getImplementedMethods(project.getTargetMethod());
		for(int i = methods.size() - 1; i>=0; i--) {
			MethodInfo mi = methods.get(i);
			ControlFlowGraph cfg = project.getFlowGraph(mi);
			Set<Integer> addressSet = new TreeSet<Integer>();
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				BasicBlock bb = n.getBasicBlock();
				if(bb == null) continue;
				for(InstructionHandle ii : bb.getInstructions()) {
					Integer address = cfg.getConstAddress(ii);
					if(address != null) addressSet.add(address);					
				}
			}
			addressMap.put(mi, addressSet);
		}
		return this;
	}
	public void dumpStats() {
		for(Entry<MethodInfo, Set<Integer>> addressEntry : addressMap.entrySet()) {
			System.out.print(addressEntry.getKey().getFQMethodName());
			for(int address : addressEntry.getValue()) {
				System.out.print(String.format(" 0x%x",address));
			}
			System.out.println();
		}		
	}
}
