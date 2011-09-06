/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.wcet.jop;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.timing.MethodCacheTiming;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.WCETTool;

/**
 * Purpose: Base class for method cache implementations
 *
 */
public abstract class MethodCacheImplementation implements MethodCache {

    public static MethodCache getCacheModel(WCETTool p, MethodCacheTiming timing) {

    	OptionGroup options = JOPConfig.getOptions(p.getConfig());
        switch (options.getOption(JOPConfig.CACHE_IMPL)) {
            case NO_METHOD_CACHE:
                return new NoMethodCache();
            case LRU_CACHE:
                return BlockCache.fromConfig(p, timing, true);
            case FIFO_CACHE:
                return BlockCache.fromConfig(p, timing, false);
            case LRU_VARBLOCK_CACHE:
                return VarBlockCache.fromConfig(p, timing, true);
            case FIFO_VARBLOCK_CACHE:
                return VarBlockCache.fromConfig(p, timing, false);
            default:
                throw new AssertionError("Non-exhaustive match on enum: CACHE_IMPL: " +
                        options.getOption(JOPConfig.CACHE_IMPL));
        }
    }

    protected WCETTool project;
	protected MethodCacheTiming timing;
    protected int cacheSizeWords;

    public MethodCacheImplementation(WCETTool p, MethodCacheTiming timing, int cacheSizeWords) {

    	this.project = p;
    	this.timing = timing;
        this.cacheSizeWords = cacheSizeWords;
    }
    
    @Override
    public long getSizeInWords() {
    	
    	return cacheSizeWords;
    }

    @Override
    public int requiredNumberOfBlocks(MethodInfo mi) {
    	
        return requiredNumberOfBlocks(project.getSizeInWords(mi));
    }

    public long getMaxMissCost(ControlFlowGraph cfg) {
    	
        long invokeCost = getMissPenalty(cfg.getNumberOfWords(), true);
        if (!cfg.isLeafMethod()) return Math.max(invokeCost, getMissOnReturnCost(cfg));
        else return invokeCost;
    }

    /**
     * Get miss penalty for invoking the given method
     */
    public long getMissOnInvokeCost(ControlFlowGraph cfg) {
    	
        return this.getMissPenalty(cfg.getNumberOfWords(), true);
    }

    /**
     * Get miss penalty for returning to the given method
     */
    public long getMissOnReturnCost(ControlFlowGraph cfg) {
    	
        return getMissPenalty(cfg.getNumberOfWords(), false);    	
    }

    /**
     * Get an upper bound for the miss cost involved in invoking a method of length
     * <pre>invokedBytes</pre> and returning to a method of length <pre>invokerBytes</pre>
     *
     * @param proc
     * @param invoker
     * @param invoked
     * @return the maximal cache miss penalty for the invoke/return
     */
    public long getInvokeReturnMissCost(WCETProcessorModel proc, ControlFlowGraph invoker, ControlFlowGraph invoked) {
    	
        return getMissPenalty(invoked.getNumberOfWords(), true) +
        		getMissPenalty(invoker.getNumberOfWords(), false);
    }

	/**
	 * @param words number of words to load
	 * @param loadOnInvoke whether this is an invoke or return instruction
	 * @return the maximum miss penalty for loading {@code words} words from method cache
	 */
	public long getMissPenalty(int words) {

		return Math.max( getMissPenalty(words,true), getMissPenalty(words,false));
	}
	
	/**
	 * Compute the delay (in cycles) caused by a method cache miss
	 * @param invokeSite the invoke site
	 * @param words size of the loaded method in words
	 * @param isInvokeInstruction whether the load happens on invoke (cheaper on JOP)
	 * @return maximum difference between a hit and a miss, in cycles
	 */
	public long getMissPenalty(int words, boolean loadOnInvoke) {

		return timing.getMethodCacheMissPenalty(words, loadOnInvoke);
	}

	/**
	 * @param invokeeWords number of words to load
	 * @param invokeInstruction the invoke instruction
	 * @return the maximum miss penalty for loading {@code words} words from method cache during {@code invokeInstruction}
	 */
	public long getMissPenaltyOnInvoke(int invokeeWords, Instruction invokeInstruction) {

		return timing.getMethodCacheMissPenalty(invokeeWords, invokeInstruction.getOpcode());
	}

	/**
	 * @param invokerWords number of words to load
	 * @param invokeeRef reference to the invoked method
	 * @return
	 */
	@Override
	public long getMissPenaltyOnReturn(int invokerWords, Type returnType) {

		short opcode;
		switch(returnType.getType()) {
		case Constants.T_BOOLEAN : 
		case Constants.T_BYTE :
		case Constants.T_CHAR : 
		case Constants.T_SHORT : 
		case Constants.T_INT : opcode = Constants.IRETURN; break;
		case Constants.T_DOUBLE : opcode = Constants.DRETURN; break;
		case Constants.T_FLOAT : opcode = Constants.FRETURN; break;
		case Constants.T_LONG : opcode = Constants.LRETURN; break;
		case Constants.T_ARRAY :
		case Constants.T_REFERENCE : opcode = Constants.IRETURN; break;
		case Constants.T_VOID: opcode = Constants.RETURN; break;
		default: return getMissPenalty(invokerWords, false);
		}
		return timing.getMethodCacheMissPenalty(invokerWords, opcode);
	}

}
