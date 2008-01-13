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
package joptimizer.optimizer;

import joptimizer.config.BoolOption;
import joptimizer.config.JopConfig;
import joptimizer.config.StringOption;
import joptimizer.framework.JOPtimizer;
import joptimizer.framework.actions.AbstractGraphAction;
import joptimizer.framework.actions.ActionException;
import com.jopdesign.libgraph.cfg.BlockCloner;
import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.Features;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.CodeBlock;
import com.jopdesign.libgraph.cfg.block.QuadCode;
import com.jopdesign.libgraph.cfg.block.StackCode;
import com.jopdesign.libgraph.cfg.statements.ControlFlowStmt;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.common.ReturnStmt;
import com.jopdesign.libgraph.cfg.statements.quad.*;
import com.jopdesign.libgraph.cfg.statements.stack.*;
import com.jopdesign.libgraph.cfg.variable.OffsetVariableMapper;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An optimizer which inlines as many function calls as possible
 *
 * Option '..':
 * check for more 'unsafe' inlining criterias (only valid if complete transitive
 * hull is known, no dynamic classloading is performed. As the JOPizer may remove
 * unused functions, reflections will be even more unsafe.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class InlineOptimizer extends AbstractGraphAction {

    private class CheckResult {
        private StmtHandle stmt;
        private ControlFlowGraph srcGraph;
        private boolean unsafeInline;
        private int srcCodeSize;
        private int srcLocals;
        private int localsOffset;
    }

    public static final String ACTION_NAME = "inline";

    public static final String CONF_INLINE_IGNORE = "ignore";

    public static final String CONF_INLINE_CHECK = "checkcode";

    private static final Logger logger = Logger.getLogger(InlineOptimizer.class);

    private boolean checkCode;
    private String[] ignore;
    private int maxLocals;
    private int maxSize;

    private Set inlinedMethods;
    private int currentSize;
    private int inlineCount;

    public InlineOptimizer(String name, JOPtimizer joptimizer) {
        super(name, joptimizer);
    }

    public void appendActionArguments(String prefix, List options) {
        options.add(new StringOption(prefix + CONF_INLINE_IGNORE,
                "Do not inline code from the given package or class prefix. Give classes as comma-separated list.",
                "packages"));
        options.add(new BoolOption(prefix + CONF_INLINE_CHECK,
                "Insert check code before inlined code to ensure correct devirtualization (NYI). " +
                "Defaults to true if dynamic class loading is assumed to be disabled."));
    }


    public String getActionDescription() {
        return "Inline and devirtualize method calls.";
    }

    public boolean doModifyClasses() {
        return true;
    }

    public boolean configure(String prefix, JopConfig config) {

        String ignorepgk = config.getOption(prefix + CONF_INLINE_IGNORE);
        if ( ignorepgk != null ) {
            ignore = ignorepgk.split(",");
        } else {
            ignore = new String[0];
        }

        checkCode = config.isEnabled(prefix + CONF_INLINE_CHECK);

        maxLocals = config.getArchConfig().getMaxLocalVars();
        maxSize = config.getArchConfig().getMaxMethodSize();

        return true;
    }

    /**
     * check if a method call can be inlined, depending on the configuration and the class structs.
     * <p/>
     * This depends on the flags and classinfos of the called method, and on the
     * configuration. This function does not check for resulting code sizes etc.
     *
     * @param methodInfo the calling method
     * @param block the block containing the stmt
     * @param stmt the invoke stmt
     * @param offset the variable offset to use for the inlined method.
     * @return null if no inlining should be performed, else a checkresult.
     */
    public CheckResult canInline(MethodInfo methodInfo, BasicBlock block, InvokeStmt stmt, int offset) {

        // check for private, static, virtual?, overwritten, final, synchronized,..
        ConstantMethod method = stmt.getMethodConstant();
        if ( method.isAnonymous() ) {
            return null;
        }
        
        MethodInfo invoked = method.getMethodInfo();

        // invokation of synchronized or unknown method not supported
        if ( invoked == null || invoked.isSynchronized() ) {
            return null;
        }

        // check for recursions
        if ( inlinedMethods.contains(invoked) ) {
            return null;
        }

        // check for excluded packages
        String className = invoked.getClassInfo().getClassName();
        String callName = methodInfo.getClassInfo().getClassName();
        // NOTICE maybe separate configs for ignore from and ignore to
        for (int i = 0; i < ignore.length; i++) {
            if ( className.startsWith(ignore[i]) || callName.startsWith(ignore[i]) ) {
                return null;
            }            
        }

        // TODO check for possible devirtualizations, resolve and set invoked (need to return resolved method)
        if ( stmt.getInvokeType() == InvokeStmt.TYPE_SPECIAL || stmt.getInvokeType() == InvokeStmt.TYPE_INTERFACE ) {
            return null;
        }

        if ( invoked.isOverwritten() || invoked.isAbstract() ) {
            return null;
        }

        CheckResult rs = new CheckResult();
        rs.localsOffset = offset;

        // check if graph can be created without exceptions
        try {
            MethodCode code = invoked.getMethodCode();
            rs.srcCodeSize = code.getCodeSize();
            
            // use a clean, new graph for inlining, to do recursive inlining under 'controlled' conditions
            rs.srcGraph = code.createGraph();
            rs.srcLocals = rs.srcGraph.getVariableTable().size();

            // TODO move this checks to 'shouldInline', find all inlines first, sort by size/loop, use queue
            if ( rs.srcLocals + rs.localsOffset > maxLocals ) {
                return null;
            }

            invoked.getMethodCode().compileGraph();
            if ( rs.srcCodeSize + currentSize > maxSize ) {
                return null;
            }

            // TODO handle exceptions correctly, then remove this check
            if ( rs.srcGraph.getExceptionTable().getExceptionHandlers().size() > 0 ) {
                return null;
            }
        } catch (GraphException e) {
            logger.warn("Could not get graph for invoked method {" + invoked.getFQMethodName() + "}, skipping.");
            return null;
        }

        if ( invoked.isStatic() || invoked.isPrivate() || invoked.isFinal() ) {
            rs.unsafeInline = false;

        } else if ( !invoked.isOverwritten() ) {
            // virtual call to methods which are not overwritten by any other class can be inlined too ('pseudo-final')
            rs.unsafeInline = getJopConfig().doAssumeDynamicLoading();

        } else {
            // don't inline unknown invoke types
            return null;
        }

        // only inline if checked or safe
        return checkCode || !rs.unsafeInline ? rs : null;
    }

    /**
     * check if a method call should be inlined, depending on tradeoffs.
     * This assumes that all method calls in the given set can be safely inlined.
     *
     * @param calls a set of methodInvokations to check
     * @return a set of methodInvokations to inline.
     */
    public Set shouldInline(Set calls) {
        return calls;
    }

    public void startAction() throws ActionException {
        inlineCount = 0;
    }

    public void finishAction() throws ActionException {
        if (logger.isInfoEnabled()) {
            logger.info("Inlined {" + inlineCount + "} methods.");
        }
    }

    public int getDefaultStage() {
        return STAGE_STACK_TO_QUAD;
    }

    public int getRequiredForm() {
        return 0;
    }

    public void execute(MethodInfo methodInfo, ControlFlowGraph graph) throws ActionException {

        // reset recursion check
        inlinedMethods = new HashSet();
        inlinedMethods.add(methodInfo);
        currentSize = methodInfo.getMethodCode().getCodeSize();

        int newBlocks = 0;
        try {
            newBlocks = processBlocks(graph, methodInfo, graph.getVariableTable().size(),
                    0, graph.getBlocks().size() );
        } catch (GraphException e) {
            throw new ActionException("Could not inline code.", e);
        }

        if ( newBlocks > 0 ) {
            graph.setModified(true);
            graph.getFeatures().removeFeature(Features.FEATURE_STACK_INFO);
            graph.getFeatures().removeFeature(Features.FEATURE_SSA);
        }
    }

    /**
     * Search blocks for invokes and do inlining recursively.
     * @param graph the graph of the caller method.
     * @param methodInfo the method of the caller.
     * @param offset the variable offset to use for the inlined method.
     * @param firstBlock the index of the first block to search.
     * @param lastBlock the (current) index of the block after the last block to search.
     * @return the number of inserted blocks.
     */
    private int processBlocks(ControlFlowGraph graph, MethodInfo methodInfo, int offset,
                              int firstBlock, int lastBlock) throws GraphException
    {

        int newLastBlock = lastBlock;

        for (int i = firstBlock; i < newLastBlock; i++) {
            BasicBlock block = (BasicBlock) graph.getBlocks().get(i);

            CodeBlock codeBlock = block.getCodeBlock();
            for ( int j = 0; j < codeBlock.size(); j++ ) {

                StmtHandle stmt = codeBlock.getStmtHandle(j);
                if ( stmt.getStatement() instanceof InvokeStmt ) {
                    InvokeStmt ivStmt = (InvokeStmt) stmt.getStatement();

                    CheckResult rs = canInline(methodInfo, block, ivStmt, offset);
                    if ( rs != null ) {

                        // found one, inline and continue with next block (code after invoke)
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inlining method {" + ivStmt.getMethodInfo().getFQMethodName() +
                                    "} in method {" + methodInfo.getFQMethodName() + "}.");
                        }
                        inlineCount++;

                        int k = doInline( methodInfo, stmt, rs );
                        newLastBlock += k;
                        i += k - 1;

                        break;
                    }
                }
            }

        }

        return newLastBlock - lastBlock;
    }

    /**
     * Do inlining by removing the invoke statement and inserting new blocks.
     *
     * @param methodInfo the current method.
     * @param stmt the handle for the invoke statement.
     * @param rs the result of the inline check.
     * @return the number of new blocks.
     */
    private int doInline(MethodInfo methodInfo, StmtHandle stmt, CheckResult rs) throws GraphException {

        InvokeStmt invokeStmt = (InvokeStmt) stmt.getStatement();
        ControlFlowGraph graph = stmt.getBlock().getGraph();
        int offset = rs.localsOffset;

        int newBlocks = 1;
        BasicBlock next = stmt.splitBefore();
        next.getCodeBlock().deleteStatement(0);

        int firstBlock = stmt.getBlock().getBlockIndex() + 1;

        // copy blocks into graph
        BlockCloner cloner = new BlockCloner(rs.srcGraph, new OffsetVariableMapper(offset));
        newBlocks += cloner.copyBlocks(graph, firstBlock);

        // link first block
        linkFirstBlock(stmt.getBlock(), graph.getBlock(firstBlock), invokeStmt, offset);

        // link inlined blocks to next block and exceptionhandler
        linkBlocks(graph, firstBlock, next, invokeStmt);

        currentSize += rs.srcCodeSize;

        // do recursive inlining
        inlinedMethods.add(invokeStmt.getMethodInfo());

        newBlocks += processBlocks(graph, methodInfo, offset + rs.srcLocals,
                firstBlock, next.getBlockIndex());

        inlinedMethods.remove(invokeStmt.getMethodInfo());

        // add check code+failsafe invoke, must be done after recursive inlining for obvious reasons.
        if ( checkCode && rs.unsafeInline ) {
            newBlocks += createCheckCode(stmt.getBlock(), next, invokeStmt);
        }

        return newBlocks;
    }

    private void linkFirstBlock(BasicBlock block, BasicBlock firstBlock, InvokeStmt invokeStmt, int offset) {

        // set inlined code as fallthrough code
        block.setNextBlock(firstBlock);

        // handle parameter passing
        if ( block.getGraph().getType() == ControlFlowGraph.TYPE_STACK ) {
            StackCode code = firstBlock.getStackCode();

            // remove existing pseudo param assign code
            int i = 0;
            while ( i < code.size() ) {
                StackStatement stmt = code.getStatement(i);

                if ( stmt instanceof StackThisAssign ) {
                    code.deleteStatement(i);
                } else if ( stmt instanceof StackParamAssign ) {
                    code.deleteStatement(i);
                } else {
                    i++;
                }
            }

            // pop params from stack and assign to local vars
            StackInvoke invoke = (StackInvoke) invokeStmt;
            TypeInfo[] params = invoke.getPopTypes();
            int[] slots = invoke.getParamSlots();

            for (int j = 0; j < params.length; j++) {
                TypeInfo param = params[j];
                VariableTable table = block.getGraph().getVariableTable();
                code.insertStatement(0, new StackStore(param, table.getDefaultLocalVariable(offset + slots[j]) ));
            }
        } else {
            QuadInvoke invoke = (QuadInvoke) invokeStmt;
            QuadCode code = firstBlock.getQuadCode();

            TypeInfo[] usedTypes = invoke.getUsedTypes();
            Variable[] params = invoke.getUsedVars();

            // replace param pseudo code with copy stmts
            for ( int i = 0; i < code.size(); i++ ) {
                QuadStatement stmt = code.getStatement(i);

                if ( stmt instanceof QuadThisAssign ) {
                    code.setStatement(i, new QuadCopy(usedTypes[0],
                            ((QuadThisAssign)stmt).getAssignedVar(), params[0]));
                }
                if ( stmt instanceof QuadParamAssign ) {
                    int nr = ((QuadParamAssign)stmt).getParamNr();
                    if ( !invoke.isStatic() ) {
                        nr++;
                    }
                    code.setStatement(i, new QuadCopy(usedTypes[nr],
                            ((QuadParamAssign)stmt).getAssignedVar(), params[nr]));
                }
            }
        }
    }

    /**
     * Link returns and exceptionhandler of inlined blocks. This assumes that the next codeblock
     * has the same exceptionhandler set as the original invoke statement.
     * @param graph the current graph.
     * @param firstBlock the index of the first inlined block.
     * @param next the next block after the inlined code.
     * @param invokeStmt the invokation statement.
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
            if ( cf != null && cf instanceof ReturnStmt ) {
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

    /**
     * Create code which checks if the invoked instance has the assumed class, else do normal
     * invokation and skip inlined code.
     * TODO implement
     * 
     * @param block the block before the invoked code.
     * @param next the block after the invoked code.
     * @param invokeStmt the invokation statement.
     * @return the number of new blocks.
     */
    private int createCheckCode(BasicBlock block, BasicBlock next, InvokeStmt invokeStmt) {
        return 0;
    }
}
