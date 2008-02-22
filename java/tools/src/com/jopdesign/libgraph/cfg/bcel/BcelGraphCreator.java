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
import com.jopdesign.libgraph.cfg.Features;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.StackEmulator;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.BranchStmt;
import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.JSRStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackParamAssign;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.statements.stack.StackThisAssign;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantPoolInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.MethodSignature;
import com.jopdesign.libgraph.struct.type.ObjectRefType;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.JSR;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelGraphCreator {

    private class GraphHandle {

        private InstructionList il;
        private ControlFlowGraph graph;
        private int[] ilPos;
        private InstructionHandle[] ih;
        private List blockPos;
        private StackEmulator emu;

        private Stack blockStack;
        private Stack targetStack;
        private Set visited;
        private int walkStage;
        private int nextStageStep;

        public GraphHandle(Code code) {
            graph = new ControlFlowGraph(methodSignature);
            il = new InstructionList(code.getCode());
            ih = il.getInstructionHandles();
            ilPos = il.getInstructionPositions();
            emu = new StackEmulator();
        }

        public InstructionList getInstructionList() {
            return il;
        }

        public ControlFlowGraph getGraph() {
            return graph;
        }

        public int[] getInstructionPositions() {
            return ilPos;
        }

        public InstructionHandle[] getInstructionHandles() {
            return ih;
        }

        public void setBlockPositions(List blockPos) {
            this.blockPos = blockPos;
        }

        public List getBlockPositions() {
            return blockPos;
        }


        public StackEmulator getStackEmulator() {
            return emu;
        }

        public boolean isLastBlock(int blockIndex) {
            return blockIndex == blockPos.size() - 1;
        }

        public BasicBlock getBlockByPosition(int position) {

            int instruction = Arrays.binarySearch(ilPos, position);

            return getBlockByInstruction(instruction);
        }

        public BasicBlock getBlockByInstruction(int instruction) {

            int pos = blockPos.indexOf(new Integer(instruction));
            if ( pos == -1 ) {
                logger.warn("Trying to get block by position failed.");
                return null;
            }

            return graph.getBlock(pos);
        }
        
        public int getStartInstruction(int blockIndex) {
            return ((Number) blockPos.get(blockIndex)).intValue();
        }

        public BasicBlock getFirstBlock() {

            // reset graph walker
            walkStage = 0;
            blockStack = new Stack();
            targetStack = new Stack();
            visited = new HashSet();

            BasicBlock block = graph.getBlock(0);
            blockStack.push(block);
            targetStack.push(new Integer(-1));
            visited.add(new Integer(block.getBlockIndex()));

            return block;
        }

        public BasicBlock getNextBlock() {

            // get next block by target
            while ( blockStack.size() > 0 ) {
                BasicBlock top = pushNextTarget();
                if ( top != null ) {
                    return top;
                }

                // can't descent anymore, so pop up to top
                blockStack.pop();
                targetStack.pop();
            }

            // stack empty, get next exception handler
            if ( walkStage == 0 ) {
                walkStage = 1;
                nextStageStep = 0;
            }

            if ( walkStage == 1 ) {
                List list = graph.getExceptionTable().getExceptionHandlers();
                BasicBlock next = null;
                while ( nextStageStep < list.size() ) {
                    next = graph.getExceptionTable().getExceptionHandler(nextStageStep++).getExceptionBlock();
                    if ( !skipBlock(next, false) ) {
                        break;
                    } else {
                        next = null;
                    }
                }
                if ( next == null ) {
                    walkStage = 2;
                    nextStageStep = 0;
                } else {
                    // Initialize Stack for exception handler
                    emu.init(new TypeInfo[] { new ObjectRefType(next.getHandledException()) });
                    blockStack.push(next);
                    targetStack.push(new Integer(-1));
                    visited.add(new Integer(next.getBlockIndex()));
                    return next;
                }
            }

            // get next JSR
            if ( walkStage == 2 ) {

            }

            return null;
        }

        public void finish() {
            il.dispose();

            // finalize graph setup, set everything to 'new & shiny' state
            graph.getFeatures().addFeature(Features.FEATURE_VAR_ALLOC);
            graph.getFeatures().addFeature(Features.FEATURE_STACK_INFO);
            graph.setModified(false);
        }

        public int countUnvisitedBlocks() {
            return graph.getBlocks().size() - visited.size();
        }

        private BasicBlock pushNextTarget() {

            BasicBlock block = (BasicBlock) blockStack.peek();
            int target = ((Number) targetStack.peek()).intValue();
            BasicBlock next = null;

            // check next block
            if ( target == -1 ) {
                BasicBlock.Edge edge = block.getNextBlockEdge();
                if ( edge != null ) {
                    next = edge.getTargetBlock();
                }
                target = 0;
            }

            if ( skipBlock(next, true) ) {
                next = null;
            }

            // check targets
            if ( next == null ) {
                while ( target < block.getTargetCount() ) {
                    BasicBlock.Edge edge = block.getTargetEdge(target++);
                    if ( edge != null ) {
                        next = edge.getTargetBlock();
                    }
                    if ( !skipBlock(next, true) ) {
                        break;
                    } else {
                        next = null;
                    }
                }
            }

            // update current target and stack, then push next target if found
            if ( next != null ) {
                targetStack.set(targetStack.size() - 1, new Integer(target));
                emu.init(block.getStackCode().getEndStack());

                blockStack.push(next);
                targetStack.push(new Integer(-1));
                visited.add(new Integer(next.getBlockIndex()));
            }

            return next;
        }

        private boolean skipBlock( BasicBlock block, boolean checkException ) {
            if ( block == null ) {
                return true;
            }
            if (  block.isExceptionHandlerBlock() && checkException ) {
                if (logger.isInfoEnabled()) {
                    logger.info("Reached exception handler block {B" + block.getBlockIndex() +
                            "} via normal controlflow in {" + graph.getSignature().getFullName() + "}");                    
                }
                return true;
            }
            return visited.contains(new Integer(block.getBlockIndex()));
        }
    }

    private class ExceptionEntry {
        private int startInstruction;
        private int endInstruction;
        private int handlerInstruction;
        BasicBlock.ExceptionHandler handler;
    }

    private ClassInfo classInfo;
    private MethodInfo methodInfo;
    private MethodSignature methodSignature;
    private BcelStmtFactory stmtFactory;

    private static final Logger logger = Logger.getLogger(BcelGraphCreator.class);

    public BcelGraphCreator(MethodInfo methodInfo) throws TypeException {
        this.classInfo = methodInfo.getClassInfo();
        this.methodInfo = methodInfo;
        this.methodSignature = methodInfo.getMethodSignature();
        stmtFactory = new BcelStmtFactory(classInfo.getAppStruct(),
                classInfo.getConstantPoolInfo());
    }

    public ConstantPoolInfo getConstantPool() {
        return methodInfo.getClassInfo().getConstantPoolInfo();
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    public ControlFlowGraph createGraph(Code code) throws GraphException {

        GraphHandle handle = new GraphHandle(code);

        findBasicBlocks(handle, code.getExceptionTable());

        fillCodeBlocks(handle, code.getLineNumberTable());

        handle.finish();

        return handle.getGraph();
    }

    /**
     * Find and create all basic blocks.
     * @param handle graph handle container
     * @param ex the exception table to use to create exception handler.
     */
    private void findBasicBlocks(GraphHandle handle, CodeException[] ex) throws GraphException {

        InstructionHandle[] ih = handle.getInstructionHandles();
        int[] ilPos = handle.getInstructionPositions();
        List blockPos = new ArrayList();
        Set exceptionBlocks = new HashSet();

        // build exception tables
        ExceptionEntry[] exTable = new ExceptionEntry[ex.length];
        for (int i = 0; i < ex.length; i++) {
            CodeException exception = ex[i];

            exTable[i] = new ExceptionEntry();
            exTable[i].startInstruction = Arrays.binarySearch(ilPos, exception.getStartPC());
            exTable[i].endInstruction = Arrays.binarySearch(ilPos, exception.getEndPC());
            exTable[i].handlerInstruction = Arrays.binarySearch(ilPos, exception.getHandlerPC());

            exTable[i].handler = handle.getGraph().getExceptionTable().addExceptionHandler();
            if ( exception.getCatchType() > 0 ) {
                try {
                    ConstantClass classInfo = getConstantPool().getClassReference(exception.getCatchType());
                    exTable[i].handler.setExceptionClass(classInfo);
                } catch (TypeException e) {
                    throw new GraphException("Could not create exceptionhandler catch class.", e);
                }
            }

            exceptionBlocks.add(new Integer(exTable[i].startInstruction));
            exceptionBlocks.add(new Integer(exTable[i].endInstruction));
            exceptionBlocks.add(new Integer(exTable[i].handlerInstruction));
        }

        // start with new block
        boolean blockEnd = true;
        for (int i = 0; i < ih.length; i++) {
            InstructionHandle instructionHandle = ih[i];

            if ( blockEnd || isBlockStart(instructionHandle) || exceptionBlocks.contains(new Integer(i)) ) {
                BasicBlock block = handle.getGraph().createBlock(blockPos.size());
                blockPos.add(new Integer(i));

                setExceptionHandler(block, exTable, i);
            }

            blockEnd = isBlockEnd(instructionHandle);
        }

        handle.setBlockPositions(blockPos);

        // register blocks as exceptionhandler
        for (int i = 0; i < exTable.length; i++) {
            BasicBlock block = handle.getBlockByInstruction(exTable[i].handlerInstruction);
            exTable[i].handler.setExceptionBlock(block);
        }
    }

    private void setExceptionHandler(BasicBlock block, ExceptionEntry[] exTable, int blockStart) {
        for (int i = 0; i < exTable.length; i++) {
            if ( exTable[i].startInstruction <= blockStart && exTable[i].endInstruction > blockStart ) {
                block.addExceptionHandler(exTable[i].handler);
            }
        }
    }

    private void fillCodeBlocks(GraphHandle handle, LineNumberTable lineTable) throws GraphException {

        initVariablesStmts(handle.getGraph().getVariableTable(), handle.getGraph().getBlock(0));

        // run through blocks, link them and fill with code.
        BasicBlock block = handle.getFirstBlock();
        while (block != null) {

            int ihIndex = handle.getStartInstruction(block.getBlockIndex());

            ihIndex = fillBlockCode(handle, block, ihIndex, lineTable);

            linkBlock(handle, block, ihIndex);

            block = handle.getNextBlock();
        }

        int unvisited = handle.countUnvisitedBlocks();
        if ( unvisited > 0 ) {
            if (logger.isInfoEnabled()) {
                logger.info("Did not reach " + unvisited + " blocks, ignoring.");
            }
        }
    }

    private int fillBlockCode(GraphHandle handle, BasicBlock block, int ihIndex, LineNumberTable lineTable) throws GraphException
    {
        int lastPos;
        InstructionHandle[] ih = handle.getInstructionHandles();
        StackEmulator emu = handle.getStackEmulator();

        if ( handle.isLastBlock(block.getBlockIndex()) ) {
            lastPos = ih.length - 1;
        } else {
            // get index before first instruction of next block
            lastPos = handle.getStartInstruction(block.getBlockIndex() + 1) - 1;
        }

        block.getStackCode().setStartStack(emu.getCurrentStack());

        VariableTable varTable = handle.getGraph().getVariableTable();

        // create statements
        for ( int i = ihIndex; i <= lastPos; i++ ) {
            TypeInfo[] stack = emu.getCurrentStack();
            StackStatement stmt;

            try {
                stmt = stmtFactory.getStackStatement(ih[i].getInstruction(), varTable,
                        stack, stack.length - 1);
            } catch (TypeException e) {
                throw new GraphException( "Could not create stackcode.", e);
            }

            if ( stmt == null ) {
                // read next stmt first
                logger.warn("Skipped instruction opcode {"+ih[i].getInstruction().getOpcode()+"}.");
                continue;
            }
            if ( stmt.getOpcode() != ih[i].getInstruction().getOpcode() ) {
                throw new GraphException("Opcode of created stackcode {"+stmt.getOpcode()+
                        "} does not match BCEL opcode {"+ih[i].getInstruction().getOpcode()+"}.");
            }

            emu.processStmt(stmt);
            if ( lineTable != null ) {
                stmt.setLineNumber(lineTable.getSourceLine(ih[i].getPosition()));
            }
            block.getStackCode().addStatement(stmt);

            // something special for JSRs
            if ( stmt instanceof JSRStmt ) {
                StmtHandle sh = block.getStackCode().getStmtHandle(block.getStackCode().size() - 1);
                linkJSR(handle, sh, ih[i]);
            }
        }

        block.getStackCode().setEndStack(emu.getCurrentStack());

        return lastPos;
    }

    private void linkJSR(GraphHandle handle, StmtHandle sh, InstructionHandle ih) throws GraphException {

        int targetPos = ((JSR)ih.getInstruction()).getTarget().getPosition();
        BasicBlock target = handle.getBlockByPosition(targetPos);

        // TODO implement
        throw new GraphException("Found JSR instruction, currently not supported.");
    }

    private void linkBlock(GraphHandle handle, BasicBlock block, int lastIndex)
    {
        ControlFlowStmt stmt = block.getControlFlowStmt();

        // set next block as fallthrough block
        if ( stmt == null || !stmt.isAlwaysTaken() ) {
            int blockIndex = block.getBlockIndex();

            if ( block.getGraph().getBlocks().size() > blockIndex ) {
                BasicBlock nextBlock = block.getGraph().getBlock(blockIndex + 1);
                block.setNextBlock(nextBlock);
            } else {
                // hu? last block does a fallthrough?
                logger.warn("Last block in graph for method {" + methodInfo.getFQMethodName() +
                        "} does not return or jump!");
            }
        }

        // nothing more to do if no targets for last stmt.
        if ( stmt == null || !(stmt instanceof BranchStmt ) ) {
            return;
        }

        // now, find target blocks by position for target-instructions
        Instruction it = handle.getInstructionHandles()[lastIndex].getInstruction();

        if ( it instanceof IfInstruction ) {
            BasicBlock target = handle.getBlockByPosition( ((IfInstruction)it).getTarget().getPosition() );
            block.setTarget(0, target);
        }
        if ( it instanceof GotoInstruction) {
            BasicBlock target = handle.getBlockByPosition( ((GotoInstruction)it).getTarget().getPosition() );
            block.setTarget(0, target);
        }
        if ( it instanceof Select) {
            Select select = (Select) it;

            BasicBlock target = handle.getBlockByPosition( select.getTarget().getPosition() );
            block.setTarget(0, target);

            InstructionHandle[] targets = select.getTargets();
            for (int i = 0; i < targets.length; i++) {
                target = handle.getBlockByPosition( targets[i].getPosition() );
                block.setTarget( i + 1, target );
            }
        }

    }

    private void initVariablesStmts(VariableTable varTable, BasicBlock block) {
        StackCode code = block.getStackCode();

        int cnt = 0;
        if ( !methodInfo.isStatic() ) {
            code.addStatement(new StackThisAssign(new ConstantClass(classInfo), varTable.getDefaultLocalVariable(cnt)));
            cnt += 1;
        }

        TypeInfo[] params = methodSignature.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            code.addStatement(new StackParamAssign(params[i], varTable.getDefaultLocalVariable(cnt), i));
            cnt += params[i].getLength();
        }
    }

    private boolean isBlockStart(InstructionHandle instructionHandle) {

        // check if this instruction is referenced somewhere in the code
        if ( instructionHandle.getTargeters() != null ) {
            return true;
        }

        return false;
     }

     private boolean isBlockEnd(InstructionHandle instructionHandle) {

         // check all controlflow-instructions (jsr is not control-flow as such ..)
        Instruction instruction = instructionHandle.getInstruction();
        if ( instruction instanceof InstructionTargeter ) {
            return !(instruction instanceof JsrInstruction);
        }

        if ( instruction instanceof ReturnInstruction ) {
            return true;
        }
        if ( instruction instanceof RET) {
            return true;
        }
        if ( instruction instanceof ATHROW) {
            return true;
        }

        return false;
    }

}
