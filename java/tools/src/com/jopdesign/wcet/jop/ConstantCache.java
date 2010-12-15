package com.jopdesign.wcet.jop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ObjectType;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.Project;
import com.jopdesign.wcet.frontend.BasicBlock;
import com.jopdesign.wcet.frontend.ControlFlowGraph;
import com.jopdesign.wcet.frontend.WcetAppInfo;
import com.jopdesign.wcet.frontend.ControlFlowGraph.CFGNode;

public class ConstantCache {

	private Project project;
	private Map<MethodInfo,Set<Integer>> cpoolAddressMap =
		new HashMap<MethodInfo, Set<Integer>>();
	private Map<MethodInfo, Set<Integer>> staticAddressMap =
		new HashMap<MethodInfo, Set<Integer>>();;

	private void addAddress(Map<MethodInfo, Set<Integer>> map, MethodInfo mi, int address) {
		if(! map.containsKey(mi)) {
			map.put(mi, new TreeSet<Integer>());
		}
		map.get(mi).add(address);
	}

	public ConstantCache(Project project) {
		this.project = project;
	}

	public ConstantCache build() {
		List<MethodInfo> methods = project.getCallGraph().getImplementedMethods(project.getTargetMethod());
		for(int i = methods.size() - 1; i>=0; i--) {
			MethodInfo mi = methods.get(i);
			ControlFlowGraph cfg = project.getFlowGraph(mi);
			for(CFGNode n : cfg.getGraph().vertexSet()) {
				BasicBlock bb = n.getBasicBlock();
				if(bb == null) continue;
				for(InstructionHandle ii : bb.getInstructions()) {
					extractConstantAddresses(cfg, ii);
				}
			}
		}
		return this;
	}
	/* Prototyping ... */
	/**
	 * PROTOTYPE status: Find the set of non-heap addresses that might be accessed by the instruction handle
	 * @param cfg
	 * @param ih
	 * @return
	 */
	private void extractConstantAddresses(ControlFlowGraph cfg, InstructionHandle ih) {
		Instruction ii = ih.getInstruction();
		switch(ii.getOpcode()) {
		case Constants.PUTSTATIC:
		case Constants.GETSTATIC:
			addStaticFieldAddress(cfg, (FieldInstruction) ii); break;
		case Constants.LDC:
		case Constants.LDC_W:
		case Constants.LDC2_W:
			addConstantPoolAddress(cfg, (CPInstruction) ii); break;
		default:
			if(ii instanceof InvokeInstruction) {
				addConstantPoolAddress(cfg, (InvokeInstruction) ii);
			}
			break;
		}
	}
	private void addStaticFieldAddress(ControlFlowGraph cfg, FieldInstruction fii) {
		WcetAppInfo appInfo = cfg.getAppInfo();
		ConstantPoolGen cpg = cfg.getMethodInfo().getConstantPoolGen();
		String fieldName = fii.getFieldName(cpg) + fii.getSignature(cpg);
		Integer address = appInfo.getProject().getLinkerInfo().getStaticFieldAddress(
				((ObjectType) fii.getReferenceType(cpg)).getClassName(),fieldName);
		addAddress(staticAddressMap, cfg.getMethodInfo(), address);
	}
	private void addConstantPoolAddress(ControlFlowGraph cfg,  CPInstruction ii) {
		WcetAppInfo appInfo = cfg.getAppInfo();
		LinkerInfo linker = appInfo.getProject().getLinkerInfo();
		Integer address = linker.getLinkInfo(cfg.getMethodInfo().getCli()).getConstAddress(ii.getIndex());
		addAddress(cpoolAddressMap, cfg.getMethodInfo(), address);
	}
	public void dumpStats() {
		System.out.println("Static addresses");
		Set<Integer> allStatics = new TreeSet<Integer>();
		for(Entry<MethodInfo, Set<Integer>> addressEntry : staticAddressMap.entrySet()) {
			System.out.print("  "+addressEntry.getKey().getFQMethodName());
			for(int address : addressEntry.getValue()) {
				System.out.print(String.format(" 0x%x",address));
				allStatics .add(address);
			}
			System.out.println();
		}
		System.out.println("  Total: "+allStatics.size());
		System.out.println("Constant Pool addresses");
		Set<Integer> allConsts = new TreeSet<Integer>();
		for(Entry<MethodInfo, Set<Integer>> addressEntry : cpoolAddressMap.entrySet()) {
			System.out.print("  "+addressEntry.getKey().getFQMethodName());
			for(int address : addressEntry.getValue()) {
				System.out.print(String.format(" 0x%x",address));
				allConsts .add(address);
			}
			System.out.println();
		}
		System.out.println("  Total: "+allConsts.size());
	}
}
