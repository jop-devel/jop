package com.jopdesign.wcet08.analysis;

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.wcet.WCETInstruction;
import com.jopdesign.wcet08.frontend.BasicBlock;
import com.jopdesign.wcet08.frontend.ControlFlowGraph;

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
		int invokedWords = bytesToWords(invoked.getNumberOfBytes());
		int invokedCost = Math.max(0, WCETInstruction.calculateB(false, invokedWords) - 
									  WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
		return invokedCost;
	}

	public static long getMissOnReturnCost(ControlFlowGraph invoker) {
		int invokerWords = bytesToWords(invoker.getNumberOfBytes());
		return Math.max(0,WCETInstruction.calculateB(false, invokerWords) - 
				          WCETInstruction.MIN_HIDDEN_LOAD_CYCLES);		
	}
	public static int bytesToWords(int b) {
		return (b+3)/4;
	}

	public static int numberOfBlocks(ControlFlowGraph flowGraph, int blockSize) {
		int mWords = BlockWCET.bytesToWords(flowGraph.getNumberOfBytes());
		return ((mWords+blockSize-1) / blockSize);
	}

}
