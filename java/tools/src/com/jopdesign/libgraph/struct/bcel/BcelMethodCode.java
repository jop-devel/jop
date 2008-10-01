/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct.bcel;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.bcel.BcelGraphCompiler;
import com.jopdesign.libgraph.cfg.bcel.BcelGraphCreator;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.MethodInvocation;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.MethodSignature;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ObjectType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * MethodCode bcel wrapper.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelMethodCode extends MethodCode {

    private BcelMethodInfo methodInfo;
    private Method method;

    private static final Logger logger = Logger.getLogger(BcelMethodCode.class);
    
    public BcelMethodCode(BcelMethodInfo methodInfo, Method method) {
        super(methodInfo);
        this.methodInfo = methodInfo;
        this.method = method;
    }

    public int getMaxStackSize() {
        return method.getCode().getMaxStack();
    }

    public int getMaxLocals() {
        return method.getCode().getMaxLocals();
    }

    /**
     * get code size in bytes.
     * @return current code size in bytes.
     */
    public int getCodeSize() {
        return method.getCode().getCode().length;
    }


    /**
     * get a list of all invoked methods of this method.
     * TODO return also instruction-nr, constantpool-nr?; also return invocations outside loaded classes?
     * @return a list of MethodInvocation classes for invoked methods.
     * @throws com.jopdesign.libgraph.struct.TypeException if referenced class is missing
     */
    public List getInvokedMethods() throws TypeException {

        ConstantPoolGen cpg = methodInfo.getConstantPoolGen();
        List invoked = new ArrayList();

        InstructionList il = new InstructionList(method.getCode().getCode());
        Instruction[] instructions = il.getInstructions();

        for (int i = 0; i < instructions.length; i++) {
            if ( instructions[i] instanceof InvokeInstruction) {
                InvokeInstruction instruction = (InvokeInstruction) instructions[i];
                boolean special = instruction.getOpcode() == 0xb7;
                MethodInvocation invoke = createMethodInvoke(cpg, instruction, special);

                if ( invoke != null ) {
                    invoke.setInstructionIndex(i);
                    invoked.add(invoke);
                }

            }
        }

        return invoked;
    }

    /**
     * get a list of all referenced contants in this method.
     * @return a list of all indices of referenced constants in the constant pool of the class.
     */
    public int[] getReferencedConstants() {
        return null;
    }

    public ControlFlowGraph createGraph() throws GraphException {

        BcelGraphCreator creator;
        ControlFlowGraph graph;
        try {
            creator = new BcelGraphCreator(methodInfo);
            graph = creator.createGraph(method.getCode());
        } catch (TypeException e) {
            throw new GraphException("Could not initialize graph-creator", e);
        }

        return graph;
    }

    protected void compileGraph(ControlFlowGraph graph) throws GraphException {

        ConstantPoolGen cpg = methodInfo.getConstantPoolGen();

		ClassInfo classInfo = methodInfo.getClassInfo();

        BcelGraphCompiler compiler = new BcelGraphCompiler(classInfo, cpg);

        method = compiler.compile(graph, classInfo.getClassName(), method);

        methodInfo.setMethod(method);
    }

    private MethodInvocation createMethodInvoke(ConstantPoolGen cpg, InvokeInstruction invoke, boolean special)
            throws TypeException
    {

        // get class of invoked method
        ObjectType type = invoke.getClassType(cpg);

        if ( getAppStruct().getConfig().isNativeClassName(type.getClassName()) ) {
            if (logger.isInfoEnabled()) {
                logger.info("Ignoring invocation of native class method {" + invoke.getMethodName(cpg) + "} in {" +
                        methodInfo.getFQMethodName() + "}.");
            }
            return null;
        }

        ClassInfo invokedClass = getAppStruct().getClassInfo(type.getClassName());
        if ( invokedClass == null ) {

            // try to reload class
            invokedClass = getAppStruct().tryLoadMissingClass(type.getClassName());

            if ( invokedClass == null ) {
                if (logger.isInfoEnabled()) {
                    logger.info("Could not resolve class reference to invoked method of {" +
                            type.getClassName() + "} in {" + methodInfo.getFQMethodName() + "}.");
                }
                return null;
            }

        }

        // find invoked methodinfo
        MethodInfo invoked = invokedClass.getInheritedMethodInfo(
                MethodSignature.createFullName(invoke.getMethodName(cpg), invoke.getSignature(cpg)), false, true );

        if ( invoked == null ) {
            logger.warn("Could not find invoked method {" + invoke.getMethodName(cpg) + "} in class {"+
                    type.getClassName()+"}.");
            return null;
        }

        return new MethodInvocation(methodInfo, invokedClass, invoked, special);
    }

}
