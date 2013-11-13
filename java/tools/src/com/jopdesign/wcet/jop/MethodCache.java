package com.jopdesign.wcet.jop;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.wcet.WCETTool;

public interface MethodCache extends CacheModel {

    public static final Logger logger = Logger.getLogger(WCETTool.LOG_WCET_CACHE+".MethodCache");

    public CacheImplementation getName();

    public boolean allFit(long blocks);

    public boolean isLRU();

    public boolean fitsInCache(int sizeInWords);

    /** @return total number of cache blocks (0 if no method cache is available) */
    public int getNumBlocks();

    public int requiredNumberOfBlocks(int sizeInWords);


	/**
	 * Compute the delay (in cycles) caused by a method cache miss
	 * @param invokeeWords number of words to load
	 * @param invokeInstruction the invoke instruction
	 * @return the maximum miss penalty for loading {@code words} words from method cache during {@code invokeInstruction}
	 */
	public long getMissPenaltyOnInvoke(int invokeeWords, Instruction invokeInstruction);

	/**
	 * Compute the delay (in cycles) caused by a method cache miss
	 * @param invokerWords number of words to load
	 * @param returnType the return type of the invoked method
	 * @return maximum difference between a hit and a miss, in cycles
	 */
	public long getMissPenaltyOnReturn(int invokerWords, Type returnType);

	/**
	 * Compute the delay (in cycles) caused by a method cache miss
	 * @param words size of the loaded method in words
	 * @param isInvokeInstruction whether the load happens on invoke (cheaper on JOP)
	 * @return maximum difference between a hit and a miss, in cycles
	 */
	public long getMissPenalty(int words, boolean isInvokeInstruction);

	/**
	 * return the number of cache blocks needed for the given method
	 * @param mi method info
	 * @return cache block count
	 */
	public int requiredNumberOfBlocks(MethodInfo mi);
}
