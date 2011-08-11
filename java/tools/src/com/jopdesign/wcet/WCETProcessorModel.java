/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet;

import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.wcet.jop.MethodCache;
import org.apache.bcel.generic.InstructionHandle;

public interface WCETProcessorModel {

	/** A human readable name of the Processor Model */
    String getName();

	long getExecutionTime(ExecutionContext context, InstructionHandle i);

	long basicBlockWCET(ExecutionContext context, BasicBlock codeBlock);

	boolean hasMethodCache();

	/**
	 * return method cache, or NoMethodCache if the processor does not have a method cache
	 * @return
	 */
    MethodCache getMethodCache();

	/**
	 * get the miss penalty (method cache)
	 * FIXME: We have to rewrite this portion of the analyzer - hardcoding miss penalties
	 * is to inflexible
	 * @param numberOfWords ... size of the method
	 * @param loadOnInvoke  ... whether the method is loaded on invoke
	 * @return
	 */
    long getMethodCacheMissPenalty(int numberOfWords, boolean loadOnInvoke);

	/**
	 * FIXME: We have to rewrite this portion of the analyzer - hardcoding miss penalties
	 * is to inflexible
	 * @param invokerFlowGraph
	 * @param receiverFlowGraph
	 * @return
	 */
    long getInvokeReturnMissCost(ControlFlowGraph invokerFlowGraph, ControlFlowGraph receiverFlowGraph);

    /**
     * @param invokeSite invoke site
     * @param invokeeWords the size of the invokee in words
     * @return the number of additional cycles required by the invoke if the invoke is a cache miss.
     */
    long getInvokeCacheMissPenalty(InvokeSite invokeSite, int invokeeWords);

    /**
     * @param invokeSite the invoke site to which the invokee returns
     * @param invokerWords the size of the invoker in words
     * @return the number of additional cycles required by the return in the invokee if the return is a cache miss.
     */
    long getReturnCacheMissPenalty(InvokeSite invokeSite, int invokerWords);

}
