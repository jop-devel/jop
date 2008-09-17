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
package joptimizer.optimizer.inline;

import com.jopdesign.libgraph.cfg.BlockCloner;
import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.CodeBlock;
import com.jopdesign.libgraph.cfg.block.QuadCode;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.CmpStmt;
import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.common.ReturnStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadCopy;
import com.jopdesign.libgraph.cfg.statements.quad.QuadIfZero;
import com.jopdesign.libgraph.cfg.statements.quad.QuadInvoke;
import com.jopdesign.libgraph.cfg.statements.quad.QuadNew;
import com.jopdesign.libgraph.cfg.statements.quad.QuadParamAssign;
import com.jopdesign.libgraph.cfg.statements.quad.QuadReturn;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.statements.quad.QuadThisAssign;
import com.jopdesign.libgraph.cfg.statements.quad.QuadThrow;
import com.jopdesign.libgraph.cfg.statements.stack.StackDup;
import com.jopdesign.libgraph.cfg.statements.stack.StackGoto;
import com.jopdesign.libgraph.cfg.statements.stack.StackIfZero;
import com.jopdesign.libgraph.cfg.statements.stack.StackInvoke;
import com.jopdesign.libgraph.cfg.statements.stack.StackNew;
import com.jopdesign.libgraph.cfg.statements.stack.StackParamAssign;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.statements.stack.StackStore;
import com.jopdesign.libgraph.cfg.statements.stack.StackThisAssign;
import com.jopdesign.libgraph.cfg.statements.stack.StackThrow;
import com.jopdesign.libgraph.cfg.variable.OffsetVariableMapper;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class does the actual inlining, but performs no code checks at all.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CodeInliner {

    private boolean insertCheckCode;
    private AppStruct appStruct;

    public CodeInliner(AppStruct appStruct) {
        this.appStruct = appStruct;
        insertCheckCode = false;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    /**
     * Check if checkcode for unsafe inlines should be created.
     * @return true if checkcode may be created.
     */
    public boolean doInsertCheckCode() {
        return insertCheckCode;
    }

    /**
     * Set if checkcode for unsafe inlines should be created.
     * @param insertCheckCode true if checkcode should be created.
     */
    public void setInsertCheckCode(boolean insertCheckCode) {
        this.insertCheckCode = insertCheckCode;
    }

    public InlineResult doInline(CheckResult check) throws GraphException {
        return doInline(check.getStmt(), check.getSrcGraph(), check.getLocalsOffset(), check.isUnsafeInline());
    }

    /**
     * Do the actual inlining.
     *
     * @param stmt the invoke statement. This must be a handle of an {@link InvokeStmt}.
     * @param srcGraph the graph of the invoked method which will be inlined.
     * @param offset the variable offset to use for new variables of the inlined method.
     * @param unsafeInline true if checkcode should be created if enabled.
     * @return an inlineresult containing informations about the new code.
     * @throws GraphException if anything goes wrong.
     */
    public InlineResult doInline(StmtHandle stmt, ControlFlowGraph srcGraph, int offset, boolean unsafeInline)
            throws GraphException
    {

        BasicBlock block = stmt.getBlock();
        InvokeStmt invokeStmt = (InvokeStmt) stmt.getStatement();
        ControlFlowGraph graph = block.getGraph();

        int newBlocks = 0;
        Collection checkBlocks = new ArrayList();

        BasicBlock next = stmt.splitBefore();
        next.getCodeBlock().deleteStatement(0);

        int firstBlock = block.getBlockIndex() + 1;

        // copy blocks into graph
        BlockCloner cloner = new BlockCloner(srcGraph, new OffsetVariableMapper(offset));
        newBlocks += cloner.copyBlocks(graph, firstBlock);

        // link first block
        checkBlocks.addAll( linkFirstBlock(block, graph.getBlock(firstBlock), invokeStmt, offset) );

        // link inlined blocks to next block and exceptionhandler
        linkBlocks(graph, firstBlock, next, invokeStmt);

        if ( insertCheckCode && unsafeInline) {
            checkBlocks.addAll( createCheckCode(block, next, invokeStmt) );
        }

        int diff = getDeltaBytecode(invokeStmt, unsafeInline);
        int maxLocals = offset + srcGraph.getVariableTable().size();
        int[] checkBlockIds = new int[checkBlocks.size()];

        newBlocks += checkBlocks.size();
        int i = 0;
        for (Iterator it = checkBlocks.iterator(); it.hasNext();) {
            BasicBlock checkBlock = (BasicBlock) it.next();
            checkBlockIds[i] = checkBlock.getBlockIndex();
            i++;
        }
        Arrays.sort(checkBlockIds);

        return new InlineResult(firstBlock, newBlocks, checkBlockIds, diff, maxLocals);
    }

    /**
     * Get the maximum bytecode size change of the removed invoke and the created check code.
     * 
     * @param result the checkresult of the invocation.
     * @return the bytecode size difference (checkcodesize - invokesize).
     */
    public int getDeltaBytecode(CheckResult result) {
        return getDeltaBytecode((InvokeStmt) result.getStmt().getStatement(), result.isUnsafeInline());
    }

    public int getDeltaBytecode(InvokeStmt stmt, boolean unsafeInline) {
        int size = 0;

        // invoke is removed
        if (stmt.getInvokeType() == InvokeStmt.TYPE_INTERFACE) {
            size -= StackInvoke.BYTE_SIZE_INTERFACE;
        } else {
            size -= StackInvoke.BYTE_SIZE;
        }

        // parameter assignment
        int[] slots = stmt.getParamSlots();
        for (int i = 0; i < slots.length; i++) {
            // NOTICE: code may be removed anyway, and lower slots may be stored with smaller version of store.
            size += StackStore.BYTE_SIZE;            
        }

        // size of Nullpointer-checkcode
        if ( stmt.getInvokeType() != InvokeStmt.TYPE_STATIC ) {
            size += StackDup.BYTE_SIZE + StackIfZero.BYTE_SIZE + StackGoto.BYTE_SIZE;
//             size += StackNew.BYTE_SIZE + StackDup.BYTE_SIZE + StackInvoke.BYTE_SIZE + StackThrow.BYTE_SIZE;
            size += StackThrow.BYTE_SIZE;
        }

        return size;
    }

    /**
     * Link the first block of the inlined code into the current graph and handle param passing.
     *
     * @param block the block which contained the invocation as last statement.
     * @param firstBlock the first block of the inlined method.
     * @param invokeStmt the original invoke
     * @param offset local variable slot offset
     * @return collection of additional blocks
     * @throws com.jopdesign.libgraph.cfg.GraphException if checkcode could not be created.
     */
    private Collection linkFirstBlock(BasicBlock block, BasicBlock firstBlock, InvokeStmt invokeStmt, int offset) throws GraphException {

        Collection newBlocks = new LinkedList();

        // set inlined code as fallthrough code
        block.setNextBlock(firstBlock);

        // handle parameter passing
        if ( block.getGraph().getType() == ControlFlowGraph.TYPE_STACK ) {
            StackCode code = firstBlock.getStackCode();

            createStackInlineHeader(block, invokeStmt, offset, newBlocks, code);
        } else {
            QuadInvoke invoke = (QuadInvoke) invokeStmt;
            QuadCode code = firstBlock.getQuadCode();

            createQuadInlineHeader(invokeStmt, invoke, code, newBlocks);
        }

        return newBlocks;
    }

    /**
     * Link returns and exceptionhandler of inlined blocks. This assumes that the next codeblock
     * has the same exceptionhandler set as the original invoke statement.
     * @param graph the current graph.
     * @param firstBlock the index of the first inlined block.
     * @param next the next block after the inlined code.
     * @param invokeStmt the invocation statement.
     */
    private void linkBlocks(ControlFlowGraph graph, int firstBlock, BasicBlock next, InvokeStmt invokeStmt) {

        int nextIndex = next.getBlockIndex();
        List exHandler = next.getExceptionHandlers();

        Variable resultVar = null;
        if ( graph.getType() == ControlFlowGraph.TYPE_QUAD ) {
            resultVar = ((QuadInvoke)invokeStmt).getAssignedVar();
        }

        for ( int i = firstBlock; i < nextIndex; i++ ) {
            BasicBlock block = graph.getBlock(i);

            for (Iterator it = exHandler.iterator(); it.hasNext();) {
                BasicBlock.ExceptionHandler handler = (BasicBlock.ExceptionHandler) it.next();
                block.addExceptionHandler(handler);
            }

            // remove returns, relink to next block
            ControlFlowStmt cf = block.getControlFlowStmt();
            if ( cf != null && cf instanceof ReturnStmt) {
                CodeBlock code = block.getCodeBlock();
                code.deleteStatement(code.size() - 1);

                // for quad code, need to copy result variable.
                if ( resultVar != null ) {
                    QuadReturn ret = (QuadReturn) cf;
                    block.getQuadCode().addStatement(new QuadCopy(ret.getType(), resultVar, ret.getReturnVar()));
                }

                block.setNextBlock(next);
            }

        }
    }

    private Collection createStackInlineHeader(BasicBlock block, InvokeStmt invokeStmt, int offset, Collection newBlocks, StackCode code) throws GraphException {

        // remove existing pseudo param assign code
        int i = 0;
        while ( i < code.size() ) {
            StackStatement stmt = code.getStackStatement(i);

            if ( stmt instanceof StackThisAssign) {
                code.deleteStatement(i);
            } else if ( stmt instanceof StackParamAssign) {
                code.deleteStatement(i);
            } else {
                i++;
            }
        }

        // pop params from stack and assign to local vars
        StackInvoke invoke = (StackInvoke) invokeStmt;
        TypeInfo[] params = invoke.getPopTypes();
        int[] slots = invoke.getParamSlots();

        VariableTable table = block.getGraph().getVariableTable();
        for (int j = 0; j < params.length; j++) {
            TypeInfo param = params[j];
            code.insertStatement(0, new StackStore(param, table.getDefaultLocalVariable(offset + slots[j]) ));

            // insert NPE check for object ref before assignment
            if ( j == 0 && invokeStmt.getInvokeType() != InvokeStmt.TYPE_STATIC ) {
                try {
                    createStackNPECheck(code, param, newBlocks);
                } catch (TypeException e) {
                    throw new GraphException("Could not create NPE check code.", e);
                }
            }
        }
        return newBlocks;
    }

    private void createQuadInlineHeader(InvokeStmt invokeStmt, QuadInvoke invoke, QuadCode code, Collection newBlocks) throws GraphException {
        TypeInfo[] usedTypes = invoke.getUsedTypes();
        Variable[] params = invoke.getUsedVars();

        // replace param pseudo code with copy stmts
        for ( int i = 0; i < code.size(); i++ ) {
            QuadStatement stmt = code.getQuadStatement(i);

            if ( stmt instanceof QuadThisAssign) {
                code.setQuadStatement(i, new QuadCopy(usedTypes[0],
                        ((QuadThisAssign)stmt).getAssignedVar(), params[0]));
            }
            if ( stmt instanceof QuadParamAssign) {
                int nr = ((QuadParamAssign)stmt).getParamNr();
                if ( !invoke.isStatic() ) {
                    nr++;
                }
                code.setQuadStatement(i, new QuadCopy(usedTypes[nr],
                        ((QuadParamAssign)stmt).getAssignedVar(), params[nr]));
            }
        }

        if ( invokeStmt.getInvokeType() != InvokeStmt.TYPE_STATIC ) {
            try {
                createQuadNPECheck(code, usedTypes[0], params[0], newBlocks);
            } catch (TypeException e) {
                throw new GraphException("Could not create NPE check code.", e);
            }
        }
    }

    /**
     * Insert a NPE check code at the beginning of the given code, using the current top of stack.
     * @param code the code where the npe check should be inserted.
     * @param thisRef the type of the checked value.
     * @param newBlocks a list of {@link BasicBlock}s, to which all created blocks will be added.
     * @return number of new blocks.
     * @throws TypeException if code could not be created.
     */
    private int createStackNPECheck(StackCode code, TypeInfo thisRef, Collection newBlocks)
            throws TypeException
    {
        code.insertStatement(0, new StackDup(thisRef));
        code.insertStatement(1, new StackIfZero(thisRef, CmpStmt.OP_NOTEQUAL));
        code.getStmtHandle(1).splitAfter();

        BasicBlock targetBlock = code.getBasicBlock().createTarget(0).getTargetBlock();
		code.getBasicBlock().setTarget(0, code.getBasicBlock().getNextBlockEdge().getTargetBlock());
		code.getBasicBlock().setNextBlock(targetBlock);
        StackCode newCode = targetBlock.getStackCode();
        newBlocks.add(targetBlock);

        // NPE is created implicitly when throwing null
        newCode.addStatement(new StackThrow());

        return 1;
    }

    private int createQuadNPECheck(QuadCode code, TypeInfo thisRef, Variable thisVar, Collection newBlocks)
            throws TypeException
    {
        code.insertStatement(0, new QuadIfZero(thisRef, CmpStmt.OP_NOTEQUAL, thisVar));
        code.getStmtHandle(0).splitAfter();

        BasicBlock targetBlock = code.getBasicBlock().createTarget(0).getTargetBlock();
		code.getBasicBlock().setTarget(0, code.getBasicBlock().getNextBlockEdge().getTargetBlock());
		code.getBasicBlock().setNextBlock(targetBlock);
        QuadCode newCode = targetBlock.getQuadCode();
        newBlocks.add(targetBlock);

        newCode.addStatement(new QuadThrow(thisVar));
        
        return 1;
    }

    /**
     * Create code which checks if the invoked instance has the assumed class, else do normal
     * invocation and skip inlined code.
     * TODO implement
     *
     * @param block the block before the invoked code.
     * @param next the block after the invoked code.
     * @param invokeStmt the invocation statement.
     * @return collection of new blocks.
     */
    private Collection createCheckCode(BasicBlock block, BasicBlock next, InvokeStmt invokeStmt) {
        return Collections.EMPTY_SET;
    }
}
