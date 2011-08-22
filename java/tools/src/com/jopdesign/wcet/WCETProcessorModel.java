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

import org.apache.bcel.generic.InstructionHandle;

import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.wcet.jop.MethodCache;

public interface WCETProcessorModel {

	/** A human readable name of the Processor Model */
    String getName();

    /** get execution time for the instruction handle in the given execution context (callstring+method)*/
	long getExecutionTime(ExecutionContext context, InstructionHandle i);

	/** get execution time for the basic block in the given execution context */
	long basicBlockWCET(ExecutionContext context, BasicBlock codeBlock);

	/**
	 * @return whether there is a method cache.
	 */
	boolean hasMethodCache();

	/**
	 * Get method cache model
	 * @return the method cache
	 */
    MethodCache getMethodCache();

}
