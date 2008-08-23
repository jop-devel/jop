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

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.Features;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.block.CodeBlock;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.ModifierInfo;
import joptimizer.framework.actions.ActionException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A helper class which contains some methods for common inlining tasks.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class InlineHelper {

    private InlineChecker checker;
    private CodeInliner inliner;
    private InvokeResolver resolver;

    public InlineHelper(InlineChecker checker, CodeInliner inliner, InvokeResolver resolver) {
        this.checker = checker;
        this.inliner = inliner;
        this.resolver = resolver;
    }

    public CodeInliner getCodeInliner() {
        return inliner;
    }

    /**
     * Find all invocations in a given code range, check if they can be inlined, and return
     * the results as list. <br>
     * Note that the results have to be checked with {@link #checkSize(CheckResult, int)} too
     * using the correct method size before it will be inlined.
     *
     * @param method the caller method, used for checks.
     * @param graph the graph to search, does not need to be the graph of the method.
     * @param firstBlock the first block to search.
     * @param lastBlock the next block after the last block to be searched.
     * @param excludeBlocks a list of blocks to skip, or null if not used; must be sorted.
     * @param localsOffset the local variable offset to be used for inlining.
     * @param parentMethods a set of {@link MethodInfo}s, used to check for recursions.
     * @return a collection of {@link CheckResult}.
     */
    public Collection findInlines(MethodInfo method, ControlFlowGraph graph, int firstBlock, int lastBlock,
                                  int[] excludeBlocks, int localsOffset, List parentMethods)
    {
        int exIdx = 0;
        List found = new LinkedList();

        for (int i = firstBlock; i < lastBlock; i++) {

            // check for excluded blocks
            if ( excludeBlocks != null && excludeBlocks.length < exIdx ) {
                while ( i > excludeBlocks[exIdx] ) {
                    exIdx++;
                }
                if ( i == excludeBlocks[exIdx] ) {
                    continue;
                }
            }

            BasicBlock block = (BasicBlock) graph.getBlocks().get(i);

            CodeBlock codeBlock = block.getCodeBlock();
            for ( int j = 0; j < codeBlock.size(); j++ ) {

                StmtHandle stmt = codeBlock.getStmtHandle(j);
                if ( stmt.getStatement() instanceof InvokeStmt) {

                    CheckResult rs = checkInvocation(method, stmt, localsOffset, parentMethods);

                    if ( rs != null ) {
                        found.add(rs);
                    } else {
                        stmt.dispose();
                    }
                } else {
                    stmt.dispose();
                }
            }
        }
        
        return found;
    }

    /**
     * Find invocations in a complete graph.
     *
     * @see #findInlines
     * @param method the invoker method.
     * @param graph the graph of the invoker to be searched.
     * @return a collection of {@link joptimizer.optimizer.inline.CheckResult}.
     */
    public Collection findInlines(MethodInfo method, ControlFlowGraph graph) {
        List parents = new LinkedList();
        parents.add(method);
        return findInlines(method, graph, 0, graph.getBlocks().size(), null, graph.getVariableTable().size(), parents);
    }

    /**
     * Find inlines recursively in an inlined code.
     *
     * @see #findInlines
     * @param method the invoker method.
     * @param graph the graph of the invoker to be searched.
     * @param result the result of an inlining which should be searched for invocations.
     * @param parentMethods a set of previously invoked methods, as created by {@link #getParentMethods(CheckResult)}.
     * @return a collection of {@link joptimizer.optimizer.inline.CheckResult}.
     */
    public Collection findInlines(MethodInfo method, ControlFlowGraph graph, InlineResult result, List parentMethods) {
        return findInlines(method, graph, result.getFirstBlock(), result.getFirstBlock() + result.getNewBlocks(),
                result.getCheckcodeBlocks(), result.getMaxLocals(), parentMethods);
    }

    /**
     * Inline an invocation. This also sets the accessed fields to public if needed.
     * The graph modified flag must be set, and the graph features must be updated correctly. This
     * can be done by calling {@link #setGraphModified(com.jopdesign.libgraph.cfg.ControlFlowGraph)}.
     *
     * @see #findInlines
     * @see #checkSize(CheckResult, int)
     * @see #changeToPublic(java.util.Collection)
     * @param check the checkresult of the invocation to inline.
     * @return an inlineresult.
     */
    public InlineResult doInline(CheckResult check) throws ActionException {

        changeToPublic( check.getChangePublic() );

        InlineResult result;
        try {
            result = inliner.doInline(check);
        } catch (GraphException e) {
            throw new ActionException("Could not perform inlining.", e);
        }

        return result;
    }

    /**
     * Resolves the invoked method and calls
     * {@link InlineChecker#checkInvocation}
     * of the used checker.
     *
     * @param method the caller method.
     * @param stmt the invocation stmt handle.
     * @param localsOffset the local variable offset to use.
     * @param parentMethods the set of already inlined methods on this position.
     * @return the checkresult if valid, else null.
     */
    public CheckResult checkInvocation(MethodInfo method, StmtHandle stmt, int localsOffset, List parentMethods) {

        MethodInfo invoked = resolver.resolveInvokedMethod(method, stmt, parentMethods);
        if ( invoked == null ) {
            return null;
        }

        return checker.checkInvocation(method, invoked, stmt, localsOffset, parentMethods);
    }

    /**
     * Check if the invocation can be inlined, with respect to the current method size.
     *
     * @param result the checkresult of an invocation check to be tested.
     * @param methodSize the current size of the method as created by {@link #calcMethodSize(CheckResult, int, InlineResult)}.
     * @return true if the invocation can be inlined.
     */
    public boolean checkSize(CheckResult result, int methodSize) {
        return checker.checkSize(result, methodSize, inliner.getDeltaBytecode(result));
    }

    public int calcMethodSize(CheckResult result, int methodSize) {
        return methodSize + result.getSrcCodeSize() + inliner.getDeltaBytecode(result);
    }

    public int calcMethodSize(CheckResult result, int methodSize, InlineResult inline) {
        return methodSize + result.getSrcCodeSize() + inline.getDeltaBytecode();
    }

    public List getParentMethods(CheckResult result) {
        List parents = new LinkedList(result.getParentInlines());
        parents.add(result.getInvokedMethod());
        return parents;
    }

    public void setGraphModified(ControlFlowGraph graph) {
        graph.setModified(true);
        graph.getFeatures().removeFeature(Features.FEATURE_SSA);
        graph.getFeatures().removeFeature(Features.FEATURE_STACK_INFO);
    }

    public boolean changeToPublic(Collection changePublic) {

        for (Iterator it = changePublic.iterator(); it.hasNext();) {
            ModifierInfo mod = (ModifierInfo) it.next();

            // set private methods to final, just to make sure (fields won't be overloaded the same way a method is).
            if ( mod instanceof MethodInfo && mod.isPrivate() && !((MethodInfo)mod).getName().equals("<init>") ) {

                try {
                    setVirtual((MethodInfo)mod);
                } catch (GraphException e) {
                    return false;
                }

                mod.setFinal(true);
            }
            
            mod.setAccessType(ModifierInfo.ACC_PUBLIC);
        }

        return true;
    }

    /**
     * Set all invokes of a private method to invokevirtual so it can be set to public.
     * All methods of the same class (but no method outside the class) can invoke the private method,
     * so in every method in the class of the method all 'special' invokes need to be replaced with
     * 'virtual' invokes. The resulting code must be compiled successfully.
     *
     * @param method a private method for which all invokes are made virtual.
     * @throws GraphException if changing a graph fails.
     */
    private void setVirtual(MethodInfo method) throws GraphException {

        ClassInfo classInfo = method.getClassInfo();
        Collection methods = classInfo.getMethodInfos();

        for (Iterator it = methods.iterator(); it.hasNext();) {
            MethodInfo methodInfo = (MethodInfo) it.next();
            if ( methodInfo.isAbstract() ) {
                continue;
            }
            ControlFlowGraph graph = methodInfo.getMethodCode().getGraph();
            boolean modified = false;

            for (Iterator it2 = graph.getBlocks().iterator(); it2.hasNext();) {
                BasicBlock block = (BasicBlock) it2.next();
                CodeBlock code = block.getCodeBlock();

                for (int i = 0; i < code.size(); i++) {
                    Statement stmt = code.getStatement(i);
                    if ( stmt instanceof InvokeStmt ) {
                        InvokeStmt invoke = (InvokeStmt) stmt;

                        MethodInfo invoked = invoke.getMethodInfo();
                        if ( invoke.getInvokeType() != InvokeStmt.TYPE_SPECIAL ||
                                invoked == null || !method.isSameMethod(invoked) )
                        {
                            continue;
                        }

                        invoke.setInvokeType(InvokeStmt.TYPE_VIRTUAL);
                        modified = true;
                    }
                }
            }

            if ( modified ) {
                graph.setModified(true);
                methodInfo.getMethodCode().compileGraph();
            }

        }
    }

}
