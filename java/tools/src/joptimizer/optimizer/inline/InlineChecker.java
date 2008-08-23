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
 *
 */
package joptimizer.optimizer.inline;

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.GraphException;
import com.jopdesign.libgraph.cfg.block.BasicBlock;
import com.jopdesign.libgraph.cfg.statements.FieldStmt;
import com.jopdesign.libgraph.cfg.statements.Statement;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.struct.AppStruct;
import com.jopdesign.libgraph.struct.ClassElement;
import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.FieldInfo;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import joptimizer.config.ArchConfig;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class checks tests if inlining is possible for invokestatements.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class InlineChecker {

    private AppStruct appStruct;

    private String[] ignorePrefix;
    private int maxLocals;
    private int maxStackSize;
    private int maxCodesize;
    private int maxInlineSize;
    private boolean assumeDynLoading;
    private boolean useCheckCode;
    private boolean changeAccess;

    private static final Logger logger = Logger.getLogger(InlineChecker.class);

    public InlineChecker(AppStruct struct, ArchConfig config) {
        this.appStruct = struct;
        this.assumeDynLoading = false;
        this.useCheckCode = false;
        ignorePrefix = new String[0];
        maxLocals = config.getMaxLocalVars();
        maxStackSize = config.getMaxStackSize();
        maxCodesize = config.getMaxMethodSize();
        maxInlineSize = 0;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    public String[] getIgnorePrefix() {
        return ignorePrefix;
    }

    public void setIgnorePrefix(String[] ignorePrefix) {
        if ( ignorePrefix == null ) {
            this.ignorePrefix = new String[0];
        } else {
            this.ignorePrefix = ignorePrefix;
        }
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    public int getMaxCodesize() {
        return maxCodesize;
    }

    public void setMaxCodesize(int maxCodesize) {
        this.maxCodesize = maxCodesize;
    }

    public boolean doAssumeDynamicLoading() {
        return assumeDynLoading;
    }

    public boolean doUseCheckCode() {
        return useCheckCode;
    }

    public void setAssumeDynamicLoading(boolean assumeDynLoading) {
        this.assumeDynLoading = assumeDynLoading;
    }

    public void setUseCheckCode(boolean useCheckCode) {
        this.useCheckCode = useCheckCode;
    }

    public boolean doChangeAccess() {
        return changeAccess;
    }

    public void setChangeAccess(boolean changeAccess) {
        this.changeAccess = changeAccess;
    }

    /**
     * Get the maximum size for methods which will be inlined.
     * @return max size of methods to inline, or 0 if all methods will be inlined.
     */
    public int getMaxInlineSize() {
        return maxInlineSize;
    }

    /**
     * Set the maximum size of methods to inline.
     * @param maxInlineSize the max size of methods to inline, or 0 for all methods.
     */
    public void setMaxInlineSize(int maxInlineSize) {
        this.maxInlineSize = maxInlineSize;
    }

    /**
     * Check if a method call can be inlined, depending on the configuration and the class structs.
     * The invoked method must be resolved first, and it must be ensured that this method is the only (known)
     * method which can be called (p.e. by calling invoked.{@link com.jopdesign.libgraph.struct.MethodInfo#isOverwritten()})
     * as this is not checked here to allow for more sophisticated devirtualization techniques.
     * <p>
     * This depends on the flags and classinfos of the called method, and on the
     * configuration. This function does not check for resulting code sizes etc.
     * </p><p>
     * Here the correct method is determined and the safety criteria for inlining are checked as
     * described in 'Practial Techniques For Virtual Call Resolution In Java' by Vijay Sundaresan and
     * the Java Virtual Machine Specification Second Edition.
     *</p>
     *
     * @param caller the calling method
     * @param invoked the resolved, invoked method.
     * @param stmt the invoke stmt handle, must be a handle of an {@link InvokeStmt}.
     * @param localsOffset the variable offset to use for the inlined method.
     * @param parentInlines a set of {@link com.jopdesign.libgraph.struct.MethodInfo}s of already inlined method which contain this statement.
     * @return null if no inlining should be performed, else a checkresult.
     */
    public CheckResult checkInvocation(MethodInfo caller, MethodInfo invoked, StmtHandle stmt, int localsOffset,
                                       List parentInlines)
    {

        if ( invoked == null || caller == null || !(stmt.getStatement() instanceof InvokeStmt) ) {
            return null;
        }

        InvokeStmt invoke = (InvokeStmt) stmt.getStatement();

        // check for unsupported invokes, method type, abstract methods, excludes, recursion, ..
        if ( !checkPreliminaries(caller, invoke.getClassInfo(), invoked, parentInlines) ) {
            return null;
        }

        // check if dynamic classloading may cause troubles, do not inline if no checkcode is used in this case
        boolean unsafeInline = !( invoked.isStatic() || invoked.isPrivate() || invoked.isFinal() ||
                invoked.getClassInfo().isFinal() || !assumeDynLoading );
        if ( unsafeInline && !useCheckCode ) {
            return null;
        }

        CheckResult rs = null;

        // check if graph can be created without exceptions
        try {
            MethodCode code = invoked.getMethodCode();
            ControlFlowGraph srcGraph = code.getGraph();

            if ( !checkCodesize(code.getCodeSize(), srcGraph, localsOffset) ) {
                return null;
            }

            Set makePublic = checkGraph(caller, invoked, srcGraph);

            if ( makePublic != null ) {
                rs = new CheckResult(stmt, invoked, srcGraph, unsafeInline, code.getCodeSize(),
                    srcGraph.getVariableTable().size(), localsOffset);
                rs.setChangePublic(makePublic);
                rs.setParentInlines(parentInlines);
            }

        } catch (GraphException e) {
            logger.warn("Could not get graph for invoked method {" + invoked.getFQMethodName() + "}, skipping.");
            return null;
        }

        return rs;
    }

    private boolean checkCodesize(int codeSize, ControlFlowGraph srcGraph, int localsOffset) {
        if ( codeSize > maxCodesize || (maxInlineSize > 0 && codeSize > maxInlineSize) ) {
            return false;
        }
        if ( srcGraph.getVariableTable().size() + localsOffset > maxLocals ) {
            return false;
        }
        return true;
    }

    /**
     * check the graph of the invoked method if it contains instructions which prevent inlining.
     *
     * @param caller the method containing the invocation which should be inlined.
     * @param invoked the invoked method.
     * @param srcGraph the graph of the invoked method.
     * @return A set of ModifierInfos which need to be set to public (may be empty), or null if check failed.
     */
    private Set checkGraph(MethodInfo caller, MethodInfo invoked, ControlFlowGraph srcGraph)
    {
        Set changePublic = new HashSet();

        // exception handling of invoked method (currently) not supported
        if ( srcGraph.getExceptionTable().getExceptionHandlers().size() > 0 ) {
            return null;
        }

        // Go through code, check for access to fields and invocations
        for (Iterator it = srcGraph.getBlocks().iterator(); it.hasNext();) {
            BasicBlock block = (BasicBlock) it.next();
            for (Iterator it2 = block.getCodeBlock().getStatements().iterator(); it2.hasNext();) {
                Statement stmt = (Statement) it2.next();

                if ( stmt instanceof InvokeStmt ) {
                    InvokeStmt invoke = (InvokeStmt) stmt;
                    MethodInfo method = invoke.getMethodInfo();
                    if ( method == null ) {
                        // this is a method of an unknown class, just don't inline to be on the safe side
                        // TODO perform basic check on classnames if invoked method must already be public
                        return null;
                    }

                    // invokespecial is somewhat, well, special..
                    if ( invoke.getInvokeType() == InvokeStmt.TYPE_SPECIAL ) {
                        if ( !checkInvokeSpecial(caller, invoked, invoke, method, changePublic) ) {
                            return null;
                        }
                    }

                    // check if fields need to be set to public
                    if ( checkNeedsPublic(caller, invoked, invoke.getClassInfo(), method, changePublic) == 0 ) {
                        return null;
                    }
                }

                if ( stmt instanceof FieldStmt ) {
                    FieldInfo field = ((FieldStmt)stmt).getFieldInfo();
                    if ( field == null ) {
                        // this is a field of an unknown class, don't inline to be on the safe side
                        // TODO perform basic check on classnames if field is already public
                        return null;
                    }

                    // check if fields need to be set to public
                    if ( checkNeedsPublic(caller, invoked, field.getClassInfo(), field, changePublic) == 0 ) {
                        return null;
                    }
                }
            }
        }

        return changePublic;
    }

    /**
     * Check if an invokespecial can be inlined.
     *
     * @param caller the method containing the invocation to be checked.
     * @param invoked the invoked method.
     * @param invoke the special-invoke in the invoked method.
     * @param method the special-invoked method.
     * @param changeSet a set of ModifierInfos which need to be set to public, found methods will be added.
     * @return false, if the invokespecial prevents inlining.
     */
    private boolean checkInvokeSpecial(MethodInfo caller, MethodInfo invoked, InvokeStmt invoke,
                                       MethodInfo method, Set changeSet)
    {

        // only allow private methods (of same class) to be inlined, no initializations (maybe someday)
        // and no super methods
        if ( method == null || !method.isPrivate() ) {
            return false;
        }

        int rs = checkNeedsPublic(caller, invoked, invoke.getClassInfo(), method, changeSet);
        if ( rs == 2 ) {
            // TODO invokespecial should be replaced with invokevirtual, add to changeSet too?
            // changeSet.add(invoke);
        }
        return rs > 0;
    }

    /**
     * Check if access of a field or method must/can be set to public.
     *
     * @param caller the method containing the invocation
     * @param invoked the invoked method
     * @param classInfo the class accessed in the invoked method.
     * @param modifierInfo the modifier info of the accessed object.
     * @param changeSet a set of ModifierInfos which need to be set to public, found methods will be added.
     * @return 0 if inlining is not possible, 1 if inlining is possible, 2 if inlining is possible, but the changeSet was modified.
     */
    private int checkNeedsPublic(MethodInfo caller, MethodInfo invoked, ClassInfo classInfo,
                                     ClassElement modifierInfo, Set changeSet)
    {
        // don't inline if the original code contains access errors
        if ( !invoked.getClassInfo().canAccess(modifierInfo) ) {
            return 0;
        }

        // don't inline if the class is not accessible
        // TODO set invoked class to accessible if needed?
        if ( !caller.getClassInfo().canAccess(classInfo) ) {
            return 0;
        }

        if ( caller.getClassInfo().canAccess(modifierInfo, false) ) {
            return 1;
        }

        // need to be changed to public, check if this can be done
        if ( !changeAccess ) {
            return 0;
        }

        // for methods, the methods with same signature in all subclasses need to be checked.
        if ( modifierInfo instanceof MethodInfo ) {
            MethodInfo method = (MethodInfo) modifierInfo;

            // check if full class hierarchy is known, else the method may be overwritten if set to public by an unkonwn class.
            if ( assumeDynLoading && !modifierInfo.getClassInfo().isFinal() ) {
                // TODO check if useCheckCode is enabled; if so, continue but mark as unsafe 
                return 0;
            }

            // search all subclasses for same method
            List queue = new LinkedList(method.getClassInfo().getSubClasses());
            while ( !queue.isEmpty() ) {
                ClassInfo cls = (ClassInfo) queue.remove(0);

                MethodInfo submethod = cls.getMethodInfo(method.getName(), method.getSignature());
                if ( submethod != null ) {
                    // cannot set to public if another method with same signature is found and any of them are private
                    if ( method.isPrivate() || submethod.isPrivate() ) {
                        return 0;
                    }
                    // method is protected or package visible, submethod is not private
                    if ( !submethod.isPublic() ) {
                        changeSet.add(submethod);
                    }
                }

                queue.addAll(cls.getSubClasses());
            }

        }

        changeSet.add(modifierInfo);
        return 2;
    }

    /**
     * Check for some preliminary requirements (method unsupported, abstract method,
     * excluded packages, recursion, .. )
     *
     * @param caller the method containing the invocation
     * @param invokeClass the class of the invoked method
     * @param invoked the invoked method
     * @param parentInlines the set of previously inlined invokers
     * @return true if the basic requirements for inlining are fulfilled.
     */
    private boolean checkPreliminaries(MethodInfo caller, ClassInfo invokeClass, MethodInfo invoked, List parentInlines) {

        // invocation of synchronized or unknown method not supported
        if ( invoked.isSynchronized() || invoked.isAbstract() || invoked.isNative() ) {
            return false;
        }

        // check for recursions
        if ( parentInlines.contains(invoked) ) {
            return false;
        }

        // check excluded packages
        if ( !checkExcludes(invokeClass, invoked) ) {
            return false;
        }
        if ( !checkExcludes(caller.getClassInfo(), caller) ) {
            return false;
        }

        // Finally, check method access, do not inline illegal access
        return caller.getClassInfo().canAccess(invoked);
    }

    public boolean checkSize(CheckResult result, int methodSize, int deltaBytecode) {
        return methodSize + result.getSrcCodeSize() + deltaBytecode < maxCodesize; 
    }

    private boolean checkExcludes(ClassInfo classInfo, MethodInfo methodInfo) {

        String className = classInfo.getClassName();
        String callName = methodInfo.getClassInfo().getClassName();

        // NOTICE maybe separate configs for ignore from and ignore to
        for (int i = 0; i < ignorePrefix.length; i++) {
            if ( className.startsWith(ignorePrefix[i]) || callName.startsWith(ignorePrefix[i]) ) {
                return false;
            }
        }
        return true;
    }

}
