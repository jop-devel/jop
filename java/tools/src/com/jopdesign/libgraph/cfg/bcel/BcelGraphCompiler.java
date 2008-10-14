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
package com.jopdesign.libgraph.cfg.bcel;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.ExceptionTable;
import com.jopdesign.libgraph.cfg.Features;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.TypeException;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.ObjectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelGraphCompiler {

    private ClassInfo classInfo;
    private ConstantPoolGen cpg;

    private Map bbStarts = new HashMap();
    private Map bbEnds = new HashMap();

    public BcelGraphCompiler(ClassInfo classInfo, ConstantPoolGen cpg) {
        this.classInfo = classInfo;
        this.cpg = cpg;
    }

    public Method compile(ControlFlowGraph graph, String className, Method method) throws GraphException {

        // create a new method
        MethodGen mg = new MethodGen(
                method.getAccessFlags(),
                method.getReturnType(),
                method.getArgumentTypes(),
                new String[method.getArgumentTypes().length],
                method.getName(),
                className,
                new InstructionList(),
                cpg
        );

        return compile(graph, mg);
    }

    public Method compile(ControlFlowGraph graph, MethodGen method) throws GraphException {

        if ( !graph.getFeatures().hasFeature(Features.FEATURE_VAR_ALLOC) ) {
            throw new GraphException("Cannot compile graph: variables not correctly allocated.");
        }

        // reset state
        bbStarts.clear();
        bbEnds.clear();

        InstructionList il = new InstructionList();
        method.setInstructionList(il);

        List targetList = buildTargetList(graph, il);
        BcelStmtFactory factory = new BcelStmtFactory(classInfo.getAppStruct(), classInfo.getConstantPoolInfo());

        // create instructions
        for (Iterator it = graph.getBlocks().iterator(); it.hasNext();) {
            BasicBlock block = (BasicBlock) it.next();
            InstructionHandle ih = ((InstructionHandle)targetList.get(block.getBlockIndex()));
            try {
                appendCode(factory, block, il, ih, targetList);
            } catch (TypeException e) {
                throw new GraphException("Could not create statements.", e);
            }
        }

        // create exception table
		ExceptionTable exceptionTable = graph.getExceptionTable();
		List exceptionHandlers = exceptionTable.getExceptionHandlers();
		for (Iterator it = exceptionHandlers.iterator(); it.hasNext();) {
			BasicBlock.ExceptionHandler eh = (BasicBlock.ExceptionHandler)it.next();

			for (Iterator k = eh.getHandledBlocks().iterator(); k.hasNext(); ) {
				BasicBlock bb = (BasicBlock)k.next();

				int srcIndex = bb.getBlockIndex();
				InstructionHandle srcStart = (InstructionHandle)bbStarts.get(new Integer(srcIndex));
				InstructionHandle srcEnd = (InstructionHandle)bbEnds.get(new Integer(srcIndex));

				int dstIndex = eh.getExceptionBlock().getBlockIndex();
				InstructionHandle dstStart = (InstructionHandle)bbStarts.get(new Integer(dstIndex));

				ObjectType type;
				if (eh.getExceptionClass() == null) {
					type = null;
				} else {
					type = new ObjectType(eh.getExceptionClass().getClassName());
				}

				method.addExceptionHandler(srcStart, srcEnd, dstStart, type);
			}
		}		

		// merge exception handlers
		CodeExceptionGen[] cg = method.getExceptionHandlers();
		for (int i = 0; i < cg.length-1; i++) {
			if (cg[i].getEndPC().getNext() == cg[i+1].getStartPC()
				&& cg[i].getHandlerPC() == cg[i+1].getHandlerPC()
				&& cg[i].getCatchType() == cg[i+1].getCatchType()) {				

				InstructionHandle srcStart = cg[i].getStartPC();
				InstructionHandle srcEnd = cg[i+1].getEndPC();
				InstructionHandle dstStart = cg[i].getHandlerPC();
				ObjectType type = cg[i].getCatchType();

				method.removeExceptionHandler(cg[i]);
				method.removeExceptionHandler(cg[i+1]);
				method.addExceptionHandler(srcStart, srcEnd, dstStart, type);
			}
		}

        // cleanup
        method.removeNOPs();
        method.setMaxLocals();
        method.setMaxStack();

        // needed to strip linetable and vartable, else bcel fails if both tables are not completely created
        method.stripAttributes(true);

        Method newMethod = method.getMethod();
        newMethod.setModifiers(method.getModifiers());

        il.dispose();

        return newMethod;
    }

    /**
     * Insert code of block into the instruction list.
     * @param factory factory to create instructions.
     * @param block the block  to add.
     * @param il the instruction list to add the code to.
     * @param ih the last instruction before the block code.
     * @param targetList the first instruction handle per block.
     */
    private void appendCode(BcelStmtFactory factory, BasicBlock block, InstructionList il, 
                            InstructionHandle ih, List targetList) throws TypeException, GraphException
    {

        StackCode code = block.getStackCode();
        VariableTable varTable = block.getGraph().getVariableTable();

        // get target instructions
        InstructionHandle[] targets = new InstructionHandle[block.getTargetCount()];
        for ( int i = 0; i < block.getTargetCount(); i++ ) {
            BasicBlock.Edge edge = block.getTargetEdge(i);
            if ( edge != null ) {
                targets[i] = (InstructionHandle) targetList.get(edge.getTargetBlock().getBlockIndex());
            }
        }

		bbStarts.put(new Integer(block.getBlockIndex()), ih);
		bbEnds.put(new Integer(block.getBlockIndex()), ih);

        // TODO get targets for jsr

        for (Iterator it = code.getStatements().iterator(); it.hasNext();) {
            StackStatement stmt = (StackStatement) it.next();
            Instruction is = factory.getInstruction(stmt, varTable, targets);

            if ( is == null ) {
                continue;
            }
            if ( is.getOpcode() != stmt.getOpcode() ) {
                throw new GraphException("Created BCEL instruction opcode {"+is.getOpcode()+
                        "} does not match opcode of stack-code {"+stmt.getOpcode()+"}.");
            }

            if ( is instanceof BranchInstruction ) {
                ih = il.append(ih, (BranchInstruction)is);
            } else {
                ih = il.append(ih, is);
            }

			bbEnds.put(new Integer(block.getBlockIndex()), ih);
        }

        // add goto if needed for default edge
        BasicBlock.Edge next = block.getNextBlockEdge();
        if ( next != null ) {
            int targetNr = next.getTargetBlock().getBlockIndex();
            if ( targetNr != block.getBlockIndex() + 1 ) {
                ih = il.append(ih, new GOTO((InstructionHandle) targetList.get(targetNr)) );
				bbEnds.put(new Integer(block.getBlockIndex()), ih);
            }
        }
    }

    private List buildTargetList(ControlFlowGraph graph, InstructionList il) {
        List targets = new ArrayList(graph.getBlocks().size());

        for (int i = 0; i < graph.getBlocks().size(); i++) {
            targets.add( il.append( new NOP() ) );              
        }
        
        return targets;
    }

}
