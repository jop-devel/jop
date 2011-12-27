/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.jcopter.inline;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.ClassMemberInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MemberInfo.AccessType;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.graphutils.ClassHierarchyTraverser;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.graphutils.EmptyClassVisitor;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.StackHelper;
import com.jopdesign.common.type.TypeHelper;
import com.jopdesign.common.type.ValueInfo;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.ValueMapAnalysis;
import com.jopdesign.jcopter.inline.InlineConfig.JVMInline;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConstantPushInstruction;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.Type;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class provides methods to check if invokesites can be inlined and also to perform
 * the necessary preparations to other methods (access change, renaming,..) to allow inlining.
 * <p>
 * This checker does not require the invoke site to refer to an invoke instruction, so that JVM calls
 * can also be inlined.
 * </p>
 * <p>
 * This class does not cache its results or provide a container class for all results since this should
 * be handled by the inlining algorithm.
 * </p>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InlineHelper {

    private static final Logger logger = Logger.getLogger(JCopter.LOG_INLINE+".InlineHelper");

    private enum CheckResult {
        SKIP, OK, NEEDS_PUBLIC, NEEDS_PUBLIC_RENAME;

        public boolean needsPublic() {
            return this == NEEDS_PUBLIC || this == NEEDS_PUBLIC_RENAME;
        }
    }

    private final JCopter jcopter;
    private final InlineConfig inlineConfig;

    // max size of methods to inline
    private int maxInlineSize;
    // max depth for recursive inlining
    private int maxRecursiveInlining;

    // max size of code in bytes; can be different from what is allowed by the target!
    private int maxCodesize;
    // max number of locals slots
    private int maxLocals;
    // max stack size in slots
    private int maxStacksize;

    public InlineHelper(JCopter jcopter, InlineConfig inlineConfig) {
        this.jcopter = jcopter;
        this.inlineConfig = inlineConfig;

        // TODO get this from config

        maxInlineSize = 0;
        maxRecursiveInlining = 0;

        ProcessorModel pm = AppInfo.getSingleton().getProcessorModel();

        maxCodesize = pm.getMaxMethodSize();
        maxLocals = pm.getMaxLocals();
        maxStacksize = pm.getMaxStackSize();
    }

    /**
     * Devirtualize an invocation.
     * <p>
     * Since this uses the callgraph if available, the callstring must match the state of the callgraph,
     * i.e. if an invocation in the callstring has been inlined and the callgraph has been updated to reflect
     * the new invoke, the inlined invocation must be removed from the callstring too.
     * Contrariwise, if an invoke has been inlined but the callgraph has not yet been updated, the callstring
     * must also contain the inlined invoke. Also the callstring does not need to start at the method to optimize.
     * This is different from what {@link #canInline(CallString, InvokeSite, MethodInfo)} expects.
     * </p>
     *
     * @see #canInline(CallString, InvokeSite, MethodInfo)
     * @param invokers the callstring of the invocation to devirtualize. The last entry must be the invoke site to
     *                 devirtualize. The first first entry does not need to be the method into which inlining
     *                 is performed.
     * @return the method info to call if unique, else null.
     */
    public MethodInfo devirtualize(CallString invokers) {
        AppInfo appInfo = AppInfo.getSingleton();

        Set<MethodInfo> methods = appInfo.findImplementations(invokers);
        if (methods.size() == 1) {
            return methods.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Devirtualize an invocation.
     * <p>
     * Since this uses the callgraph, the callstring must match the state of the callgraph,
     * i.e. if an invocation in the callstring has been inlined and the callgraph has been updated to reflect
     * the new invoke, the inlined invocation must be removed from the callstring too.
     * Contrariwise, if an invoke has been inlined but the callgraph has not yet been updated, the callstring
     * must also contain the inlined invoke. Also the callstring does not need to start at the method to optimize.
     * This is different from what {@link #canInline(CallString, InvokeSite, MethodInfo)} expects.
     * </p>
     *
     * @see #canInline(CallString, InvokeSite, MethodInfo)
     * @param callgraph the callgraph to use for devirtualization.
     * @param invokers the callstring of the invocation to devirtualize. The last entry must be the invoke site to
     *                 devirtualize. The first first entry does not need to be the method into which inlining
     *                 is performed.
     * @return the method info to call if unique, else null.
     */
    public MethodInfo devirtualize(CallGraph callgraph, CallString invokers) {
        // we only use the callgraph, methods not in the graph are not devirtualized.
        Set<MethodInfo> methods = callgraph.findImplementations(invokers);
        if (methods.size() == 1) {
            return methods.iterator().next();
        } else {
            return null;
        }
    }




    /**
     * Perform an initial test if the invokesite can be replaced by the given method code
     * depending on the configuration and the class infos.
     * <p>
     * The invoked method must be resolved first and it must be ensured that this method is the only (known)
     * method which can be called (e.g. by using {@link #devirtualize(CallString)}) as this is not checked
     * here to allow for different devirtualization techniques.
     * </p><p>
     * This depends on the flags and classinfos of the called method, and on the
     * configuration. This function does not check for resulting code sizes etc.
     * </p><p>
     * The safety criteria for inlining are checked as
     * described in 'Practial Techniques For Virtual Call Resolution In Java' by Vijay Sundaresan.
     *</p>
     * <p>
     * To check if inlining is actually possible under the target size restrictions, you also need to check
     * {@link #checkConstraints(MethodInfo, InvokeSite, MethodInfo, int, int, int)}.
     * To determine which code to generate, you might want to check
     * {@link #needsEmptyStack(InvokeSite, MethodInfo)} and {@link #needsNullpointerCheck(CallString, MethodInfo,boolean)}.
     * If an invokesite is inlined, {@link #prepareInlining(MethodInfo, MethodInfo)} must be called before inlining.
     * </p>
     * <p>
     * The given callstring is not used to check the callgraph or any other analysis result, but to check
     * for recursive inlining, so inlined invokes should not be removed from the callstring even if the callgraphs
     * and analyses have been updated, and the callstring must start at the method to optimize.
     * This is different from what {@link #devirtualize(CallString)} expects.
     * </p>
     *
     * @see #devirtualize(CallString)
     * @see #checkConstraints(MethodInfo, InvokeSite, MethodInfo, int, int, int)
     * @see #needsEmptyStack(InvokeSite, MethodInfo)
     * @see #needsNullpointerCheck(CallString, MethodInfo, boolean)
     * @see #prepareInlining(MethodInfo, MethodInfo)
     * @param invokers a callstring leading to the invokee. The first entry in the callstring must be the method into
     *                 which the other methods are recursively inlined, the last entry in the list must be invokesite
     *                 of the invokee. This is needed to check to avoid endless inlining of recursive methods, inlined invokes
     *                 should therefore not be removed from this callstring. The first invokesite may differ from the
     *                 actual invokesite to inline (i.e. it can be the "original" invokesite).
     * @param invokeSite the actual invokesite to inline
     * @param invokee the devirtualized invokee.
     * @return true if all initial tests succeed.
     */
    public boolean canInline(CallString invokers, InvokeSite invokeSite, MethodInfo invokee) {

        if ( !checkJVMCall(invokers.top(), invokee) ) {
            return false;
        }

        // check for unsupported invokes, method type, abstract methods, excludes, recursion, ..
        if ( !checkPreliminaries(invokers, invokeSite, invokee) ) {
            return false;
        }

        // check if dynamic classloading may cause troubles, do not inline in this case
        boolean safeInline = invokee.isStatic() || invokee.isPrivate() || invokee.isFinal() ||
                invokee.getClassInfo().isFinal();

        // TODO check DFA results? If we have a result, and if classes are only added, not replaced,
        //      we could still safely inline
        if ( !safeInline && jcopter.getJConfig().doAssumeIncompleteAppInfo()) {
            return false;
        }

        // check if we need to modify something and if so if this is possible/allowed.
        return checkCode(invokeSite.getInvoker(), invokee);
    }

    /**
     * Check if an exception must be generated if the 'this' reference is null.
     * This test can return false if
     * <ul><li>There is no 'this' reference</li>
     * <li>The DFA analysis showed that the reference is never null</li>
     * <li>The inlined code will always generate an exception anyway</li>
     * <li>Generating checks has been disabled by configuration</li>
     * </ul>
     * <p>
     * The callstring does not need to start or to end at the method to optimize. However since the callstring is
     * used to check the DFA results if available, the callstring must match what the DFA expects, i.e. if
     * the DFA-results and -callstrings are updated during inlining, this callstring must not include inlined
     * invokes. Contrariwise if the DFA results are not updated during inline, the callstring must contain already
     * inlined invokes.
     * </p>
     *
     * @param callString The callstring including the invokesite of the invokee. The top invokesite does not need to
     *                   refer to an invoke instruction, and the referenced invoker method does not need to
     *                   be the method containing the invoke to inline (e.g. if the invoke to inline has
     *                   been inlined itself). However the callstring needs to match what the DFA expects.
     * @param invokee the devirtualized invokee.
     * @param analyzeCode if false, skip checking the code of the invokee.
     * @return true if a nullpointer check code should be generated.
     */
    public boolean needsNullpointerCheck(CallString callString, MethodInfo invokee, boolean analyzeCode) {
        if (inlineConfig.skipNullpointerChecks()) return false;

        InvokeSite invokeSite = callString.top();

        // check if we have a 'this' reference anyway
        if (invokeSite.isInvokeStatic() || invokeSite.isJVMCall()) {
            return false;
        }

        // TODO check the DFA results if available
        if (jcopter.useDFA()) {
            
        } else if ("<init>".equals(invokee.getShortName())) {
            // this is a slight hack .. constructors cannot be called explicitly by the programmer
            // and javac always calls constructors on a new object, that is never null, so we can skip
            // the NP check in this case (and hope that compilers for languages other than Java do the same..)
            return false;
        }

        if (!analyzeCode) {
            return true;
        }

        // check if the code will always throw an exception anyway (without producing any side effects before throwing)

        ValueMapAnalysis analysis = new ValueMapAnalysis(invokee);
        analysis.loadParameters();

        InstructionList list = invokee.getCode().getInstructionList(true, false);
        for (InstructionHandle ih : list.getInstructionHandles()) {
            Instruction instr = ih.getInstruction();

            if (instr instanceof ConstantPushInstruction ||
                instr instanceof LocalVariableInstruction)
            {
                analysis.transfer(instr);
            } else if (instr instanceof GETFIELD ||
                       instr instanceof PUTFIELD ||
                       instr instanceof INVOKEVIRTUAL ||
                       instr instanceof INVOKEINTERFACE ||
                       instr instanceof INVOKESPECIAL)
            {
                int down = instr.consumeStack(invokee.getConstantPoolGen());
                ValueInfo value = analysis.getValueTable().top(down);
                // check if we use the 'this' reference, in this case the inlined code will throw an NP exception
                // the same way as the inlined invoke
                if (value.isThisReference()) {
                    return false;
                }

                break;
            } else {
                // we ignore all other instructions (for now..)
                break;
            }
        }


        return true;
    }

    /**
     * Check if we need to save the stack before executing the inlined code. This is necessary if the
     * inlined code contains exception handlers (e.g. due to synchronize statements).
     * <p>
     * If this returns true, the stack must contain only the parameters of the invokee prior to the invokesite.
     * This does not check if the stack is actually empty in the caller to allow for checking for recursive inlining
     * without the need to generate code (if a method A calls B and B requires an empty stack, A will also require
     * an empty stack if A is inlined and B is inlined into A).
     * </p><p>
     * Note that the result of this method can change if inlining is performed on the invoked method
     * before inlining it (i.e. when methods which require an empty stack get inlined in the invoked method).
     * </p>
     *
     * @param invokeSite The invoke site. Does not need to refer to an invoke instruction, and does not need to
     *                   refer to the method which will actually contain the invocation (in case of recursive inlining).
     * @param invokee the devirtualized invokee.
     * @return true if the stack needs to be empty except for the parameters of the invokee before the invoke site
     *         (even if the stack of the caller is already empty at the invoke site).
     */
    public boolean needsEmptyStack(InvokeSite invokeSite, MethodInfo invokee) {
        if (invokee.getCode().getExceptionHandlers().length > 0) {
            return false;
        }
        return invokee.isSynchronized();
    }

    /**
     * Check if inlining is possible under the target code restrictions. This check depends on the caller code,
     * so this check can turn to false if other invokes in the caller are inlined.
     * <p>
     * This does not check the application code size, this must be done by the optimization.
     * </p>
     *
     * @param invoker the method into which the invokee is inlined.
     * @param invokeSite The invoke site. Does not need to refer to an invoke instruction, and does not need to
     *                   refer to the method which will actually contain the invocation (in case of recursive inlining).
     * @param invokee the devirtualized invokee.
     * @param deltaCode size of code inserted or removed from the caller in addition to the inlined method.
     *                  This can include nullpointer checks, other invokes which will be inlined in the caller but have
     *                  not yet been inlined or code to save the stack. This does not include the current invoke instruction
     *                  or the code to be inlined (i.e. if only the invokesite is replaced by the invokee code, this is 0).
     * @param numLocals the number of (live) local variable slots in the caller at the invokesite. This code
     *                  does not check if a local variable assignment is actually possible, i.e. it might be possible
     *                  that although enough slots are available, inlining is not possible due to fragmentation of the
     *                  unused slots in the caller if the callee uses double or long values, so you either need to
     *                  check this yourself or simply pass largest live slot number. Pass 0 to skip this test.
     * @param stackSize Used slots on the stack before the invoke site.
     * @return true if no target size constraints are violated.
     */
    public boolean checkConstraints(MethodInfo invoker, InvokeSite invokeSite, MethodInfo invokee, int deltaCode, int numLocals, int stackSize) {

        MethodCode code = invokee.getCode();
        // TODO needed to make sure maxLocals and maxStack is uptodate.. Maybe we can skip this and require the analysis to do that?
        code.compile();

        int codeSize = invoker.getCode().getNumberOfBytes() + code.getNumberOfBytes() + deltaCode;

        if ( (maxCodesize > 0 && codeSize > maxCodesize) ) {
            return false;
        }
        // check if we have enough local variables available
        if ( maxLocals > 0 && numLocals + code.getMaxLocals() > maxLocals ) {
            return false;
        }
        if (maxStacksize > 0 && stackSize + code.getMaxStack() > maxStacksize) {
            return false;
        }
        return true;
    }


    /**
     * Prepare the invokee for inlining into the invokesite, by widening access restrictions or renaming
     * methods to prevent incorrect method resolving.
     * <p>
     * This may change the code of the invokee, so this needs to be done before inlining the code.
     * The CFG of the invokee will be removed.
     * </p><p>
     * This code assumes that {@link #canInline(CallString, InvokeSite, MethodInfo)} returned true for this invoke.
     * </p>
     *
     * @param invoker the method where the code will be inlined to.
     * @param invokee the method to inline.
     */
    public void prepareInlining(MethodInfo invoker, MethodInfo invokee) {

        MethodCode code = invokee.getCode();
        InstructionList il = code.getInstructionList();

        for (InstructionHandle ih : il.getInstructionHandles()) {
            Instruction instr = ih.getInstruction();

            if ( instr instanceof InvokeInstruction) {
                InvokeSite invokeSite = code.getInvokeSite(ih);

                MethodRef ref = invokeSite.getInvokeeRef();
                MethodInfo method = ref.getMethodInfo();

                // we already checked that everything can be resolved
                // nothing special to do for invokespecial here (checkInvokeSpecial only skips, no special return codes)

                // check what we need to do
                CheckResult rs = checkNeedsPublic(invoker, invokee, ref.getClassInfo(), method);

                if (rs == CheckResult.NEEDS_PUBLIC) {
                    makePublic(method);
                }

                if (rs == CheckResult.NEEDS_PUBLIC_RENAME) {

                    // TODO generate a new name, rename method

                    if (method.isPrivate()) {
                        // TODO check the class for invokers, change to invokevirtual

                    } else {
                        // if the method is package visible, we need to rename all overriding methods
                        // too (but not methods from subclasses in different packages which do not override this)
                        // TODO update overriding methods

                        // TODO need to update all possible call sites

                    }

                    makePublic(method);

                    throw new AppInfoError("Implement me!");
                }

            }
            else if ( instr instanceof FieldInstruction ) {
                FieldRef ref = code.getFieldRef(ih);
                FieldInfo field = ref.getFieldInfo();

                // we already checked that everything can be resolved

                // check if fields need to be set to public
                CheckResult rs = checkNeedsPublic(invoker, invokee, ref.getClassInfo(), field);

                if (rs == CheckResult.NEEDS_PUBLIC) {
                    makePublic(field);
                }
                if (rs == CheckResult.NEEDS_PUBLIC_RENAME) {
                    throw new AppInfoError("Invalid returncode: renaming of fields not required");
                }
            }

        }
    }

    private void makePublic(ClassMemberInfo member) {

        boolean wasPrivate = member.isPrivate();

        // If we need to make it public, check if we need to make the class and all enclosing classes public too
        ClassInfo cls = member.getClassInfo();
        while (cls != null) {
            if (!cls.isPublic()) {
                cls.setAccessType(AccessType.ACC_PUBLIC);
            }
            cls = cls.getEnclosingClassInfo();
        }

        if (wasPrivate && member instanceof MethodInfo) {
            // we are done here. if the method was private, there are no conflicting
            // methods or we needed to rename it anyway.
            member.setAccessType(AccessType.ACC_PUBLIC);
            return;
        }

        // if we make a non-private method or any field public, need to go down to find all overriding
        // members and make them public too
        final MemberID memberID = member.getMemberID();

        ClassVisitor visitor = new EmptyClassVisitor() {
            @Override
            public boolean visitClass(ClassInfo classInfo) {
                ClassMemberInfo m = classInfo.getMemberInfo(memberID);
                if (m == null) {
                    return true;
                }
                if (m.isPublic()) {
                    // we do not need to go further down if we find a public member
                    return false;
                }
                m.setAccessType(AccessType.ACC_PUBLIC);
                return true;
            }
        };
        new ClassHierarchyTraverser(visitor).traverseDown(member.getClassInfo());

    }

    /**
     * Check for some preliminary requirements (method unsupported, abstract method,
     * excluded packages, recursion, .. )
     *
     *
     * @param invokers a callstring leading to the invokee. The first entry in the callstring must be the method into
     *                 which the other methods are recursively inlined, the last entry in the list must be invokesite
     *                 of the invokee. This is needed to check to avoid endless inlining of recursive methods.
     * @param invokeSite the actual invokesite to inline.
     * @param invokee the devirtualized invokee.
     * @return true if the basic requirements for inlining are fulfilled.
     */
    private boolean checkPreliminaries(CallString invokers, InvokeSite invokeSite, MethodInfo invokee) {

        MethodInfo invoker = invokeSite.getInvoker();

        // invocation of synchronized or unknown method not supported
        // TODO support synchronized methods, simply insert monitorenter and monitorexit (and exception handler)
        if ( invokee.isSynchronized() || invokee.isAbstract() || invokee.isNative() ) {
            return false;
        }

        // check if the invokee is a native method
        if (AppInfo.getSingleton().isNative(invokee.getClassInfo().getClassName())) {
            return false;
        }

        // check for recursions, we do not inline recursive methods
        if ( invokers.contains(invokee) ) {
            return false;
        }
        // check for max recursive inlining depth
        if (maxRecursiveInlining > 0 && invokers.length() > maxRecursiveInlining) {
            return false;
        }

        // check size of invokee.. (we could check JVM size instead of target size to speed things up a bit?)
        if (maxInlineSize > 0 && invokee.getCode().getNumberOfBytes() > maxInlineSize) {
            return false;
        }

        // check excluded packages
        // invoker method
        if ( inlineConfig.doExcludeInvoker(invoker) ) {
            return false;
        }
        // invoked method implementation
        // TODO we do not check the referenced receiver classname.. should we?
        if ( inlineConfig.doExcludeInvokee(invokee) ) {
            return false;
        }

        // We do not inline the WCA target method, else we would run into problems because the wca target
        // might be removed and will not be in the appinfo callgraph anymore
        if (jcopter.useWCA()) {
            for (MethodInfo method : jcopter.getJConfig().getWCATargets()) {
                if (method.equals(invokee)) {
                    return false;
                }
            }
        }

        // do not inline library code into application code (and we do not inline within the library neither..)
        if ( !inlineConfig.doInlineLibraries() && (AppInfo.getSingleton().isLibrary(invokee.getClassName())
                                                || AppInfo.getSingleton().isLibrary(invoker.getClassName())) )
        {
            return false;
        }

        // Finally, check method access, do not inline illegal access
        return invoker.canAccess(invokee);
    }

    private boolean checkJVMCall(InvokeSite invokeSite, MethodInfo invokee) {
        // We do not inline JVM calls for now, since they use magic type conversions which will upset the verifier.

        if (!invokeSite.isJVMCall()) {
            return true;
        }

        if (inlineConfig.doInlineJVMCalls() == JVMInline.NONE) {
            return false;
        }
        if (inlineConfig.doInlineJVMCalls() == JVMInline.ALL) {
            return true;
        }

        // Check if params and return type match what is expected on the stack due to the instruction to replace
        ConstantPoolGen cpg = invokeSite.getInvoker().getConstantPoolGen();
        Instruction instr = invokeSite.getInstructionHandle().getInstruction();

        if (!TypeHelper.canAssign( StackHelper.consumeStack(cpg, instr), invokee.getArgumentTypes())) {
            return false;
        }
        if (!invokee.getType().equals(Type.VOID)) {
            if (!TypeHelper.canAssign( new Type[]{invokee.getType()}, StackHelper.produceStack(cpg, instr) )) {
                return false;
            }
        }

        return true;
    }

    /**
     * check the code of the invoked method if it contains instructions which prevent inlining.
     *
     * @param invoker the method into which the invokee will be inlined.
     * @param invokee the invoked method.
     * @return true if the code can be inlined and {@link #prepareInlining(MethodInfo, MethodInfo)} will succeed.
     */
    private boolean checkCode(MethodInfo invoker, MethodInfo invokee)
    {
        MethodCode code = invokee.getCode();

        // Go through code, check for access to fields and invocations
        for (InstructionHandle ih : code.getInstructionList(true, false).getInstructionHandles()) {
            Instruction instr = ih.getInstruction();

            if ( instr instanceof InvokeInstruction) {
                InvokeSite invokeSite = code.getInvokeSite(ih);

                MethodRef ref = invokeSite.getInvokeeRef();
                MethodInfo method = ref.getMethodInfo();

                if ( method == null ) {
                    // this is a method of an unknown class, just don't inline to be on the safe side
                    // TODO perform basic check on classnames if invoked method must already be public?
                    return false;
                }

                // invokespecial is somewhat, well, special..
                if ( invokeSite.isInvokeSpecial() ) {
                    if ( checkInvokeSpecial(invoker, invokee, invokeSite, ref.getClassInfo(), method) == CheckResult.SKIP ) {
                        return false;
                    }
                } else {
                    // check if fields need to be set to public
                    if ( checkNeedsPublic(invoker, invokee, ref.getClassInfo(), method) == CheckResult.SKIP ) {
                        return false;
                    }
                }
            }
            else if ( instr instanceof FieldInstruction ) {
                FieldRef ref = code.getFieldRef(ih);
                FieldInfo field = ref.getFieldInfo();

                if ( field == null ) {
                    // this is a field of an unknown class, don't inline to be on the safe side
                    // TODO perform basic check on classnames if field is already public?
                    return false;
                }

                // check if fields need to be set to public
                if ( checkNeedsPublic(invoker, invokee, ref.getClassInfo(), field) == CheckResult.SKIP ) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if an invokespecial can be inlined.
     *
     * @param invoker the method containing the invocation to be checked.
     * @param invokee the method to inline.
     * @param invokeSite the invokespecial invokesite in the invokee.
     * @param classInfo the class of the special-invoked method
     * @param method the special-invoked method.
     * @return a CheckResult status defining what should be done about the member access.
     */
    private CheckResult checkInvokeSpecial(MethodInfo invoker, MethodInfo invokee, InvokeSite invokeSite,
                                           ClassInfo classInfo, MethodInfo method)
    {
        // TODO we could skip inlining <init> and stuff like that, but why bother?
        if ("<init>".equals(method.getShortName())) {
            // constructors are never resolved as super methods
            return checkNeedsPublic(invoker, invokee, classInfo, method);
        }

        // We simply assume that the class hierarchy does not change and no new methods override the invoked methods..
        if (invoker.getClassInfo().isExtensionOf(classInfo) &&
            !method.equals(invoker.getSuperMethod(true, true)))
        {
            // We could only inline if we remove the ACC_SUPER flag of the invoker class..
            return CheckResult.SKIP;
        }

        return checkNeedsPublic(invoker, invokee, classInfo, method);
    }

    /**
     * Check if access of a field or method must/can be set to public.
     *
     * @param invoker the method containing the invocation to be checked.
     * @param invoked the invoked method to inline.
     * @param classInfo the class accessed in the invoked method.
     * @param memberInfo the modifier info of the accessed object.
     * @return a CheckResult status defining what should be done about the member access.
     */
    private CheckResult checkNeedsPublic(MethodInfo invoker, MethodInfo invoked, ClassInfo classInfo,
                                         ClassMemberInfo memberInfo)
    {
        // don't inline if the original code contains access errors
        if ( !invoked.canAccess(memberInfo) ) {
            return CheckResult.SKIP;
        }

        // if the invoker can access the member, everything is fine
        if ( invoker.canAccess(memberInfo) ) {
            return CheckResult.OK;
        }

        // need to be changed to public, check if this can be done
        if ( !inlineConfig.allowChangeAccess() ) {
            return CheckResult.SKIP;
        }

        // for methods, the methods with same signature in all subclasses need to be checked.
        // fields are not virtually resolved 
        if ( memberInfo instanceof MethodInfo ) {
            MethodInfo method = (MethodInfo) memberInfo;

            // check if full class hierarchy is known, else the method may be overwritten if set to public by an unkonwn class.
            if ( jcopter.getJConfig().doAssumeIncompleteAppInfo() && !memberInfo.getClassInfo().isFinal() ) {
                return CheckResult.SKIP;
            }

            // search all subclasses for same method
            List<ClassInfo> queue = new LinkedList<ClassInfo>(method.getClassInfo().getDirectSubclasses());
            while ( !queue.isEmpty() ) {
                ClassInfo cls = queue.remove(0);

                MethodInfo subMethod = cls.getMethodInfo(method.getMethodSignature());
                if ( subMethod != null ) {
                    // We can simply make private methods public, but we must not change call sites to invokevirtual
                    if (method.getAccessType() == AccessType.ACC_PACKAGE &&
                        !subMethod.overrides(method,false))
                    {
                        // Need to rename method and all its overriding methods, but not the not-overriding subMethod (or vice-versa)
                        return inlineConfig.allowRename() ? CheckResult.NEEDS_PUBLIC_RENAME : CheckResult.SKIP;
                    }
                }
            }

        }

        return CheckResult.NEEDS_PUBLIC;
    }

}
