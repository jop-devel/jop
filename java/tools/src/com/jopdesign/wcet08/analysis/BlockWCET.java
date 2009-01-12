package com.jopdesign.wcet08.analysis;

import java.util.Iterator;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.DedicatedNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.InvokeNode;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.SummaryNode;

/**
 * Get WCET of basic blocks (utility class)
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class BlockWCET {
	/**
	 * Estimate the WCET of a basic block (only local effects) for debugging purposes
	 * @param b the basic block
	 * @return the cost of executing the basic block, without cache misses
	 */
	public static int basicBlockWCETEstimate(BasicBlock b) {
		int wcet = 0;
		for(InstructionHandle ih : b.getInstructions()) {
			int jopcode = b.getAppInfo().getJOpCode(b.getClassInfo(), ih.getInstruction());
			int opCost = WCETInstruction.getCycles(jopcode,false,0);						
			wcet += opCost;
		}
		return wcet;
	}

	public static long getMissOnInvokeCost(ControlFlowGraph invoked) {
		int invokedWords = invoked.getNumberOfWords();
		int invokedCost = Math.max(0, WCETInstruction.calculateB(false, invokedWords) - 
									  WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
		return invokedCost;
	}

	public static long getMissOnReturnCost(ControlFlowGraph invoker) {
		int invokerWords = invoker.getNumberOfWords();
		return Math.max(0,WCETInstruction.calculateB(false, invokerWords) - 
				          WCETInstruction.MIN_HIDDEN_LOAD_CYCLES);		
	}

	/**
	 * Get an upper bound for the miss cost involved in invoking a method of length
	 * <pre>invokedBytes</pre> and returning to a method of length <pre>invokerBytes</pre> 
	 * @param invoker
	 * @param invoked
	 * @return the maximal cache miss penalty for the invoke/return
	 */
	public static long getInvokeReturnMissCost(ControlFlowGraph invoker, ControlFlowGraph invoked) {
		return getMissOnInvokeCost(invoked)+getMissOnReturnCost(invoker);
	}

	public static int numberOfBlocks(ControlFlowGraph flowGraph, int blockSize) {
		int mWords = flowGraph.getNumberOfWords();
		return ((mWords+blockSize-1) / blockSize);
	}
	
	private Config config;
	private Project project;
	public BlockWCET(Project p) {
		this.config = Config.instance();
		this.project = p;
	}
	public void checkCache(MethodInfo m) {
		if(requiredNumberOfBlocks(m) <= config.getOption(CacheConfig.CACHE_BLOCKS)) return;
		throw new AssertionError("Too few cache blocks for "+m+" - requires "+
								 requiredNumberOfBlocks(m) + " but have " +
								 config.getOption(CacheConfig.CACHE_BLOCKS));		
	}
	public boolean allFit(MethodInfo m) {
		return getMaxCacheBlocks(m) <= config.getOption(CacheConfig.CACHE_BLOCKS);
	}

	/**
	 * Compute the number of cache blocks which might be needed when calling this method
	 * @param mi
	 * @return the maximum number of cache blocks needed, s.t. we won't run out of cache
	 * blocks when invoking the given method
	 * @throws TypeException 
	 */
	public long getMaxCacheBlocks(MethodInfo mi) {
		long size = 0;
		Iterator<CallGraphNode> iter = project.getCallGraph().getReachableMethods(mi);
		while(iter.hasNext()) {
			CallGraphNode n = iter.next();
			if(n.isAbstractNode()) continue;
			size+= requiredNumberOfBlocks(n.getMethodImpl());
		}
		return size;
	}

	private long requiredNumberOfBlocks(MethodInfo mi) {
		return BlockWCET.numberOfBlocks(project.getFlowGraph(mi),
										Config.instance().getOption(CacheConfig.BLOCK_SIZE_WORDS).intValue());
	}
	public long getMissCost(MethodInfo mi) {
		ControlFlowGraph cfg = project.getFlowGraph(mi);
		if(project.getCallGraph().isLeafNode(cfg.getMethodInfo())) {
			return getMissOnInvokeCost(cfg);
		} else {
			return getMissOnReturnCost(cfg);
		}
	}

	/* FIXME: we need to merge this with the stuff in SimpleAnalysis */ 
	public WcetCost computeLocalCost(CFGNode n) {
		return computeLocalCost(n,WcetVisitor.class);
	}
	public WcetCost computeLocalCost(CFGNode n, Class<? extends WcetVisitor> c) {
		WcetVisitor wcetVisitor;
		try {
			wcetVisitor = c.newInstance();
			wcetVisitor.project = project;
		} catch (Exception e) {
			throw new AssertionError("Failed to instantiate WcetVisitor: "+c+" : "+e);
		}
		n.accept(wcetVisitor);
		return wcetVisitor.cost;
	}
	public static class WcetVisitor implements ControlFlowGraph.CfgVisitor {
		WcetCost cost;
		Project project;
		public WcetVisitor() {
			this.cost = new WcetCost();
		}
		public void visitSpecialNode(DedicatedNode n) {
		}
		public void visitSummaryNode(SummaryNode n) {
		}
		public void visitInvokeNode(InvokeNode n) {
			visitBasicBlockNode(n);
			/* should be overriden by local/global analysis */
		}
		public void visitBasicBlockNode(BasicBlockNode n) {
			BasicBlock bb = n.getBasicBlock();
			for(InstructionHandle ih : bb.getInstructions()) {
				addInstructionCost(n,ih);
			}
		}
		private void addInstructionCost(BasicBlockNode n, InstructionHandle ih) {
			// FIXME: handle java implemented bytecodes
			Instruction ii = ih.getInstruction();
			int jopcode = project.getWcetAppInfo().getJOpCode(n.getBasicBlock().getClassInfo(), ii);
			int cycles = WCETInstruction.getCycles(jopcode,false,0);
			cost.addLocalCost(cycles);
		}
	}
	public static class AlwaysMissVisitor extends WcetVisitor {
		public void visitInvokeNode(InvokeNode n) {
			super.visitInvokeNode(n);
			this.cost.addCacheCost(BlockWCET.getInvokeReturnMissCost(n.invokerFlowGraph(), n.receiverFlowGraph()));
		}
	}
}
