/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.code;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.log4j.Logger;

/**
 * A class which represents an invocation.
 *
 * <p>Two invoke-sites are considered equal if the invoker methodInfo are {@link MethodInfo#equals(Object) equal},
 * and if they point to the same InstructionHandle.</p>
 * <p>
 * This class handles all the special cases of invocation target resolution. To find all implementations for 
 * virtual calls, see {@link AppInfo#findImplementations(InvokeSite)}
 * </p>
 * <p>Note that this class does not and must not cache the results of the invokee resolution, since the
 * invoke instruction in the InstructionHandle can be changed without invalidating the InvokeSite.
 * </p>
 *
 * @see MethodCode#getInvokeSite(InstructionHandle)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InvokeSite {

    public static final Logger logger = Logger.getLogger(LogConfig.LOG_CODE + ".InvokeSite");

    private final InstructionHandle instruction;
    private final MethodInfo invoker;
    private final int hash;

    /**
     * Create a new invoke site.
     * <p>
     * You should not use this constructor yourself, instead use {@link MethodCode#getInvokeSite(InstructionHandle)}.
     * </p>
     *
     * @param instruction the instruction handle containing the invoke instruction
     * @param invoker the method containing the invoke instruction
     */
    public InvokeSite(InstructionHandle instruction, MethodInfo invoker) {
        this.instruction = instruction;
        this.invoker = invoker;
        // Beware! we cannot use .getPosition() here since its value can and will change
        this.hash = 31 * invoker.hashCode() + instruction.hashCode();
    }

    public InstructionHandle getInstructionHandle() {
        return instruction;
    }

    public MethodInfo getInvoker() {
        return invoker;
    }

    /**
     * @return true if this is a virtual invocation, i.e. either an invokevirtual or invokeinterface.
     */
    public boolean isVirtual() {
        return isInvokeVirtual() || isInvokeInterface();
    }

    /**
     * @return true if this is a JVM Java implementation invocation, false if this is a normal invoke instruction.
     */
    public boolean isJVMCall() {
        Instruction instr = instruction.getInstruction();
        if (instr instanceof InvokeInstruction) {
            return false;
        }
        assert AppInfo.getSingleton().getProcessorModel().isImplementedInJava(invoker, instruction.getInstruction());
        return true;
    }

    public boolean isInvokeSpecial() {
        return instruction.getInstruction() instanceof INVOKESPECIAL;
    }

    /**
     * Check if this invoke is an invokespecial instruction and the method to invoke is not
     * the referenced method, but a method found in a superclass of the class where the invoker
     * method is defined (and the invocation does not call a constructor and the ACC_SUPER flag is set).
     * <p>
     * If this is the case method resolution works differently, i.e. the invoked method is not necessarily the
     * referenced method, but the method inherited to the superclass if the class where the invoker method is
     * defined (which might not be the class of the object for which the invoker is executed).
     * </p>
     * <p>Explanation can be found here:
     * http://weblog.ikvm.net/PermaLink.aspx?guid=99fcff6c-8ab7-4358-9467-ddf71dd20acd
     * </p>
     * <p>Imagine the following
     * <pre>
     * class A { public void method(){} }
     * class B extends A { }
     * class C extends B {
     *   public method(){
     *     super.method(); // refers to A.method()
     *   }
     * }
     * </pre>
     * If you replace class B with a class which defines method() without recompiling C,
     * from Java 1.1 on invokespecial in C.method() is defined to call B.method() now although it still refers
     * to A.method() (lookup starts in the superclass of the class which contains the invokespecial instruction).
     * </p> 
     *
     * @return true if this invoke should invoke a super method.
     */
    public boolean isInvokeSuper() {
        if (!isInvokeSpecial()) return false;
        InvokeInstruction i = (InvokeInstruction) instruction.getInstruction();
        return isSuperMethod(getReferencedMethod(invoker, i));
    }

    public boolean isInvokeStatic() {
        return instruction.getInstruction() instanceof INVOKESTATIC;
    }

    public boolean isInvokeVirtual() {
        return instruction.getInstruction() instanceof INVOKEVIRTUAL;
    }

    public boolean isInvokeInterface() {
        return instruction.getInstruction() instanceof INVOKEINTERFACE;
    }

    public InvokeInstruction getInvokeInstruction() {
        if (instruction.getInstruction() instanceof InvokeInstruction) {
            return (InvokeInstruction) instruction.getInstruction();
        }
        return null;
    }

    /**
     * Get a reference to the invoked method (for nonvirtual invokes) or to the referenced
     * method (for virtual invokes). If the instruction is handled in java, return a reference
     * to the method which implements the instruction.
     *
     * @see #getInvokeeRef(boolean)
     * @see AppInfo#findImplementations(InvokeSite)
     * @return a reference to the actually referenced method (might be different from the method
     *         referenced by the instruction).
     */
    public MethodRef getInvokeeRef() {
        return getInvokeeRef(true);
    }

    /**
     * Get the MethodRef to the referenced method. If the instruction is handled in java, return a reference
     * to the method which implements the instruction.
     * <p>
     * The MethodRef refers to the actual reference. {@link MethodRef#getMethodInfo()} resolves the actual
     * MethodInfo, which might be defined in a super class of the referenced method, if the method is inherited.
     * For invokespecial, the implementing method might even be defined in a subclass of the referenced method,
     * if resolveSuper is set to false, see {@link #isInvokeSuper()} for details.
     * </p>
     * <p>To find all possible implementations if the invocation is a virtual invoke (see {@link #isVirtual()}),
     * use {@link AppInfo#findImplementations(InvokeSite)}.</p>
     *
     * @see #isInvokeSuper()
     * @see AppInfo#findImplementations(InvokeSite)
     * @param resolveSuper if true, try to resolve the super method reference (see {@link #isInvokeSuper()}),
     *                     else return a reference to the method as it is defined by the instruction.
     * @return a method reference to the invokee method.
     */
    public MethodRef getInvokeeRef(boolean resolveSuper) {
        Instruction instr = instruction.getInstruction();
        AppInfo appInfo = AppInfo.getSingleton();
        if (instr instanceof InvokeInstruction) {
            MethodRef ref = getReferencedMethod(invoker, (InvokeInstruction) instr);
            // need to check isInvokeSpecial here, since it is not checked by isSuperMethod!
            if (resolveSuper && isInvokeSpecial() && isSuperMethod(ref)) {
                ref = resolveInvokeSuper(ref);
            }
            return ref;
        }
        if (appInfo.getProcessorModel().isImplementedInJava(invoker, instr)) {
            return appInfo.getProcessorModel().getJavaImplementation(appInfo, invoker, instr).getMethodRef();
        }
        throw new JavaClassFormatError("InvokeSite handle does not refer to an invoke instruction: "+toString());
    }

    /**
     * @param methodInfo a possible invokee
     * @return true if this invokeSite may invoke the method. Does not use the callgraph to check.
     *   For interface invoke sites this can return UNKNOWN if the class of the given method implementation
     *   does not implement the referenced interface.
     */
    public Ternary canInvoke(MethodInfo methodInfo) {
        assert methodInfo != null;

        MethodRef invokeeRef = getInvokeeRef();
        MethodInfo method = invokeeRef.getMethodInfo();

        if (methodInfo.equals(method)) {
            return Ternary.TRUE;
        }

        if (!isVirtual()) {
            // if it is non-virtual and method is null, it must be a different method
            // and therefore cannot be invoked
            return Ternary.FALSE;
        }

        if (method == null) {
            return Ternary.UNKNOWN;
        }

        if (!methodInfo.getClassInfo().isSubclassOf(invokeeRef.getClassInfo())) {
            if (isInvokeInterface() && !methodInfo.getClassInfo().isInterface()) {
                // for interface invokes, this is slightly different, since the class of the method
                // might not be the receiver and might not implement the referenced interface.. we can only
                // check the signature..
                if (!invokeeRef.getMethodSignature().equals(methodInfo.getMethodSignature())) {
                    return Ternary.FALSE;
                }
                return Ternary.UNKNOWN;
            }

            return Ternary.FALSE;
        }

        return Ternary.valueOf( methodInfo.overrides(method, true) );
    }

    /**
     * Create a string representation of this InvokeSite.
     * Note that the result is neither unique nor constant (since the position in the code can change).
     *
     * @return a readable representation of this callsite.
     */
    @Override
    public String toString() {
        return invoker.getFQMethodName() + ":" + instruction.getPosition();
    }

    /**
     * Two invoke-sites are considered equal if the invoker methodInfo are {@link MethodInfo#equals(Object) equal},
     * and if they point to the same InstructionHandle.
     * @param obj the object to compare.
     * @return true if they refer to the same invocation.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InvokeSite))    return false;
        InvokeSite is = (InvokeSite) obj;
        if (!instruction.equals(is.getInstructionHandle())) return false;
        // TODO performance optimization: if we assume that invokesites in different methods
        //      never share the same instruction handle, we could simply return true
        return invoker.equals(is.getInvoker());
    }

    @Override
    public int hashCode() {
        return hash;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Get a MethodRef for the referenced method in a given invoke instruction in the invoker method.
     * This does not resolve any super methods or devirtualize the call.
     * <p>
     * If you call {@link MethodRef#getMethodInfo()}, the method found in the referenced class
     * or in its superclasses will be returned. For InvokeSpecial, this might not be correct,
     * so use {@link #getInvokeeRef()} or {@link AppInfo#findImplementations(InvokeSite)} instead.</p>
     * <p>
     * Note: Since this could easily be used incorrectly, this method has been moved here from MethodInfo and
     * made private in favor of getInvokeeRef()
     * </p>
     *
     * @param invoker the method containing the instruction, used to resolve constantpool references.
     * @param instr the instruction to resolve
     * @return a method reference representing the invoke reference.
     */
    private static MethodRef getReferencedMethod(MethodInfo invoker, InvokeInstruction instr) {
        ConstantPoolGen cpg = invoker.getConstantPoolGen();

        String methodname = instr.getMethodName(cpg);
        String classname = invoker.getCode().getReferencedClassName(instr);
        MemberID memberID = new MemberID(classname, methodname, instr.getSignature(cpg));

        if ("<clinit>".equals(methodname)) {
            // in this case, we do not know if the class is an interface or not, since interfaces
            // can have <clinit> methods which are invoked by invokestatic
            return AppInfo.getSingleton().getMethodRef(memberID);
        }
        boolean isInterface = (instr instanceof INVOKEINTERFACE);
        return AppInfo.getSingleton().getMethodRef(memberID, isInterface);
    }

    /**
     * Check if this invokespecial is a super invoke. Does NOT check if the instruction is indeed an
     * special invoke, check this first!
     * See #isInvokeSuper() for more details.
     *
     * @param invokee the method referenced by the instruction.
     * @return true if this references to a super method
     */
    private boolean isSuperMethod(MethodRef invokee) {
        // This is the class where the invoker method is defined (not the class of the object instance!)
        ClassInfo cls = invoker.getClassInfo();

        if (!cls.hasSuperFlag()) return false;
        if ("<init>".equals(invokee.getName())) return false;

        // just to handle some special cases of unknown superclasses gracefully, without requiring a classInfo
        if (cls.getClassName().equals(invokee.getClassName())) {
            // this is an invoke within the same class, no super here
            return false;
        }
        if (cls.isRootClass()) {
            // trying to call a super-method of Object? Not likely, dude ..
            return false;
        }

        // do not need to check interfaces, since invokespecial must not call interface methods
        Ternary rs = cls.hasSuperClass(invokee.getClassName(), false);

        if (rs == Ternary.UNKNOWN) {
            if (invokee.getClassRef().getClassInfo() != null) {
                // class exists, but method does not exists, either an error or superclasses are missing
                throw new JavaClassFormatError("Invokespecial tries to call "+invokee+
                        " but this method has not been found");
            }
            // invokespecial to an unknown class, we cannot handle this safely
            throw new JavaClassFormatError("Could not determine if invokespecial is a super invoke for "+invokee);
        }

        return rs == Ternary.TRUE;
    }

    /**
     * Resolve the reference to the super method.
     * See #isInvokeSuper() for more details.
     *
     * @param ref the method referenced by the instruction
     * @return the reference to the method to invoke
     */
    private MethodRef resolveInvokeSuper(MethodRef ref) {
        String superClass = invoker.getClassInfo().getSuperClassName();
        // Restart the lookup in the super class of the invoker class
        MemberID superId = new MemberID(superClass, ref.getName(), ref.getDescriptor());
        // invokespecial is never used for interface invokes
        MethodRef superRef = AppInfo.getSingleton().getMethodRef(superId,false);

        if (ref.getMethodInfo() != null && !ref.getMethodInfo().equals(superRef.getMethodInfo())) {
            // Just give a warning, just in case there is some code out there which handles this incorrectly..
            // Warning level might be changed to debug someday..
            logger.warn("InvokeSpecial in "+invoker+" calls super method "+superRef+" but refers to "+
                         ref+" which is valid, but you might want to know that..");
        }

        return superRef;
    }
}
