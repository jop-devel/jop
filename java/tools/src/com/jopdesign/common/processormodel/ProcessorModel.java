/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008-2009, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.processormodel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import org.apache.bcel.generic.Instruction;

import java.util.List;

public interface ProcessorModel {

	/**
     * A human readable name of the Processor Model
     * @return the name of the processor model.
     */
    String getName();

	/**
	 * Check whether we need to deal with the given statement in a special way,
	 * because it is translated to a processor specific bytecode.
     *
	 * @param ctx the method containing the instruction
     * @param i the instruction to check
	 * @return true, if the instruction is translated to a processor specific bytecode
	 */
    boolean isSpecialInvoke(MethodInfo ctx, Instruction i);

	/**
	 * Check whether the given instruction is implemented in Java.
     * @param i the instruction to check
     * @return true if it is implemented as a java method
     */
    boolean isImplementedInJava(Instruction i);

	/**
	 * For Java implemented bytecodes, get the method
	 * implementing the bytecode.
	 * @param ai the AppInfo containing the classes containing the java implementations of bytecodes
     * @param ctx the method of the instruction
     * @param instr the instruction to check
     * @return the reference to the Java implementation of the bytecode,
	 *         or null if the instruction is not implemented in Java.
	 */
    MethodInfo getJavaImplementation(AppInfo ai, MethodInfo ctx, Instruction instr);

	int getNativeOpCode(MethodInfo ctx, Instruction instr);

	/**
	 * Get number of bytes needed to encode an instruction
	 * @param context The method the instruction belongs to
	 * @param instruction the instruction to check
	 * @return the number of bytes this instruction needs
	 */
    int getNumberOfBytes(MethodInfo context, Instruction instruction);

	/**
	 * Get classes, which contain methods invoked by the JVM.
	 * Used for Java implemented bytecodes, exceptions.
     *
	 * @return a list of fully qualified class names used by the JVM or an empty list
	 */
    List<String> getJVMClasses();

    /**
     * Get classes, which are implemented native by the processor or JVM and will
     * normally not be loaded.
     *
     * @return a list of fully qualified class names used by the JVM or an empty list
     */
    List<String> getNativeClasses();

}
