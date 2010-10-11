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

package com.jopdesign.common;

import com.jopdesign.common.bcel.ParameterAnnotationAttribute;
import com.jopdesign.common.code.CodeRepresentation;
import com.jopdesign.common.graph.ClassHierarchyTraverser;
import com.jopdesign.common.graph.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class MethodInfo extends ClassMemberInfo {

    private final MethodGen methodGen;
    private final Descriptor descriptor;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT+".MethodInfo");
    private static final Logger codeLogger = Logger.getLogger(LogConfig.LOG_CODE+".MethodInfo");

    private CodeRepresentation codeRep;

    public MethodInfo(ClassInfo classInfo, MethodGen methodGen) {
        super(classInfo, methodGen);
        this.methodGen = methodGen;
        descriptor = Descriptor.parse(methodGen.getSignature());
    }

    public boolean isAbstract() {
        return methodGen.isAbstract();
    }

    public void setAbstract(boolean val) {
        methodGen.isAbstract(val);
    }

    public boolean isSynchronized() {
        return methodGen.isSynchronized();
    }

    public void setSynchronized(boolean val) {
        methodGen.isSynchronized(val);
    }

    public boolean isNative() {
        return methodGen.isNative();
    }

    public void setNative(boolean val) {
        methodGen.isNative(val);
    }

    public boolean isStrictFP() {
        return methodGen.isStrictfp();
    }

    public void setStrictFP(boolean val) {
        methodGen.isStrictfp(val);
    }

    public Attribute[] getCodeAttributes() {
        return methodGen.getCodeAttributes();
    }

    public String[] getArgumentNames() {
        return methodGen.getArgumentNames();
    }

    public void setArgumentNames(String[] arg_names) {
        methodGen.setArgumentNames(arg_names);
    }

    public void setArgumentName(int i, String name) {
        methodGen.setArgumentName(i, name);
    }

    public Type getArgumentType(int i) {
        return methodGen.getArgumentType(i);
    }

    public int getMaxStack() {
        return methodGen.getMaxStack();
    }

    public int getMaxLocals() {
        return methodGen.getMaxLocals();
    }

    public CodeExceptionGen[] getExceptionHandlers() {
        return methodGen.getExceptionHandlers();
    }

    public LineNumberGen[] getLineNumbers() {
        return methodGen.getLineNumbers();
    }

    public LocalVariableGen addLocalVariable(String name, Type type, int slot, InstructionHandle start, InstructionHandle end) {
        return methodGen.addLocalVariable(name, type, slot, start, end);
    }

    public LocalVariableGen addLocalVariable(String name, Type type, InstructionHandle start, InstructionHandle end) {
        return methodGen.addLocalVariable(name, type, start, end);
    }

    public void removeLocalVariable(LocalVariableGen l) {
        methodGen.removeLocalVariable(l);
    }

    public void removeLocalVariables() {
        methodGen.removeLocalVariables();
    }

    public LocalVariableGen[] getLocalVariables() {
        return methodGen.getLocalVariables();
    }

    public LineNumberGen addLineNumber(InstructionHandle ih, int src_line) {
        return methodGen.addLineNumber(ih, src_line);
    }

    public void removeLineNumber(LineNumberGen l) {
        methodGen.removeLineNumber(l);
    }

    public void removeLineNumbers() {
        methodGen.removeLineNumbers();
    }

    public CodeExceptionGen addExceptionHandler(InstructionHandle start_pc, InstructionHandle end_pc, InstructionHandle handler_pc, ObjectType catch_type) {
        return methodGen.addExceptionHandler(start_pc, end_pc, handler_pc, catch_type);
    }

    public void removeExceptionHandler(CodeExceptionGen c) {
        methodGen.removeExceptionHandler(c);
    }

    public void removeExceptionHandlers() {
        methodGen.removeExceptionHandlers();
    }

    public void addException(String class_name) {
        methodGen.addException(class_name);
    }

    public void removeException(String c) {
        methodGen.removeException(c);
    }

    public void removeExceptions() {
        methodGen.removeExceptions();
    }

    public String[] getExceptions() {
        return methodGen.getExceptions();
    }

    public void addCodeAttribute(Attribute a) {
        methodGen.addCodeAttribute(a);
    }

    public void removeCodeAttribute(Attribute a) {
        methodGen.removeCodeAttribute(a);
    }

    public void removeCodeAttributes() {
        methodGen.removeCodeAttributes();
    }

    public ParameterAnnotationAttribute getParameterAnnotation(boolean visible) {
        for (Attribute a : getAttributes()) {
            if ( a instanceof ParameterAnnotationAttribute ) {
                if ( ((ParameterAnnotationAttribute)a).isVisible() == visible ) {
                    return (ParameterAnnotationAttribute) a;
                }
            }
        }
        return null;
    }
    /**
     * Get a BCEL method for this methodInfo.
     *
     * @param compile if true, this does the same as {@link #compileMethod()}.
     * @return a method for this methodInfo.
     */
    public Method getMethod(boolean compile) {
        if ( compile ) {
            // we use the compile flag primarily as a reminder to the API user to compile first
            return compileMethod();
        }
        return methodGen.getMethod();
    }

    /**
     * Compile all changes and update maxStack and maxLocals, and
     * return a new BCEL method.
     * 
     * @return an updated method for this methodInfo.
     */
    public Method compileMethod() {
        compileCodeRep();
        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        return methodGen.getMethod();
    }

    public void setMethodCode(MethodGen method) {
        codeRep = null;

        // TODO copy all code relevant infos from method, excluding name, params and access flags
        methodGen.setInstructionList(method.getInstructionList());
    }

    public InstructionList getInstructionList() {
        compileCodeRep();
        return methodGen.getInstructionList();
    }

    public void setInstructionList(InstructionList il) {
        methodGen.setInstructionList(il);
        codeRep = null;
    }

    public void removeNOPs() {
        methodGen.removeNOPs();
    }

    public <T extends CodeRepresentation> T getCode(T codeRep) {
        if ( this.codeRep == null ) {
            this.codeRep = codeRep;
            codeRep.load(this);
            return codeRep;
        }

        if ( codeRep.getClass().isInstance(this.codeRep) &&
             codeRep.isSameType(this.codeRep) )
        {
            // this.codeRep is same type and already contains the current code.
            // cast is checked above
            //noinspection unchecked
            return (T) this.codeRep;
        }

        // this.codeRep is set but different
        compileCodeRep();
        this.codeRep = codeRep;
        codeRep.load(this);
        return codeRep;
    }

    public boolean hasCodeRep() {
        return codeRep != null;
    }

    public void compileCodeRep() {
        if ( codeRep != null ) {
            codeRep.compile(this);
        }
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    public MethodRef getMethodRef() {
        return new MethodRef(this);
    }

    @Override
    public String getMemberSignature() {
        return methodGen.getName() + methodGen.getSignature();
    }

    @Override
    public Signature getSignature() {
        return new Signature(getClassInfo().getClassName(), getSimpleName(), getDescriptor());
    }

    /**
     * Check if this method is the same as or overrides a given method.
     * 
     * @param superMethod the superMethod to check.
     * @param checkSignature if true, check if the given method has the same signature and is defined in a subclass
     *  of this method's class.
     * @return true if this method overrides the given method and can access the method.
     */
    public boolean overrides(MethodInfo superMethod, boolean checkSignature) {

        // A static method may hide a static or instance method, but does not override it.
        if ( isStatic() ) {
            return false;
        }
        if ( superMethod.isStatic() ) {
            logger.warn("Instance method " +getSignature()+" overrides static method "+superMethod.getSignature());
        }

        if (checkSignature) {
            if ( !getMemberSignature().equals(superMethod.getMemberSignature()) ) {
                return false;
            }
            if ( !getClassInfo().isInstanceOf(superMethod.getClassInfo()) ) {
                return false;
            }
        }
        
        if ( superMethod.equals(this) ) {
            return true;
        }

        return getClassInfo().canAccess(superMethod);
    }

    /**
     * Get the super method for this method, if there is any.
     *
     * @param checkAccess if false, also return hidden methods and methods which cannot be accessed by this method's class.
     * @return the super method or null if none found.
     */
    public MethodInfo getSuperMethod(boolean checkAccess) {
        
        if ( checkAccess && (isPrivate() || isStatic()) ) {
            return null;
        }

        ClassInfo superClass = getClassInfo().getSuperClassInfo();
        if ( superClass != null ) {
            MethodInfo inherited = superClass.getMethodInfoInherited(getSignature(), checkAccess);
            return checkAccess || overrides(inherited, false) ? inherited : null;
        } else {
            return null;
        }
    }

    /**
     * Get all methods definitions from all interfaces implemented by this method.
     *
     * @return a list of all interface methods implemented by this method.
     */
    public Collection<MethodInfo> getInterfaceMethods() {
        final List<MethodInfo> ifMethods = new LinkedList<MethodInfo>();

        ClassVisitor visitor = new ClassVisitor() {

            public boolean visitClass(ClassInfo classInfo) {
                if ( !classInfo.isInterface() ) {
                    return false;
                }
                MethodInfo ifMethod = classInfo.getMethodInfo(getMemberSignature());
                if ( ifMethod != null ) {
                    ifMethods.add(ifMethod);
                }
                return true;
            }

            public void finishClass(ClassInfo classInfo) {
            }
        };

        new ClassHierarchyTraverser(visitor).traverseUp(getClassInfo());

        return ifMethods;
    }

    /**
     * Find all methods which override/implement this method.
     * Instance method are overridden by other instance methods and hidden by static methods.
     * 
     * @param checkAccess if false, find methods with the same signature in subclasses even if they
     *        do not override this method (i.e. private or static methods).
     * @return a list of all overriding methods.
     */
    public Collection<MethodInfo> getOverriders(final boolean checkAccess) {
        final List<MethodInfo> overriders = new LinkedList<MethodInfo>();

        if (checkAccess && (isPrivate() || isStatic())) {
            return overriders;
        }

        ClassVisitor visitor = new ClassVisitor() {

            public boolean visitClass(ClassInfo classInfo) {
                MethodInfo overrider = classInfo.getMethodInfo(getMemberSignature());
                if ( overrider != null ) {
                    if ( overrider.isPrivate() ) {
                        // found an overriding method which is private .. this is interesting..
                        logger.error("Found private method "+overrider.getMemberSignature()+" in "+
                                classInfo.getClassName()+" overriding non-private method in "+
                                getClassInfo().getClassName());
                    }

                    // If a subclass contains a static method or a package visible method with same
                    // signature, but is in a different package, it does NOT override this method.
                    if ( !checkAccess || overrider.overrides(MethodInfo.this, false) ) {
                        overriders.add(overrider);
                    }

                }
                return true;
            }
            public void finishClass(ClassInfo classInfo) {
            }
        };

        new ClassHierarchyTraverser(visitor).traverseDown(getClassInfo());

        return overriders;
    }

    /**
     * Get all non-abstract methods (including this method if it is not abstract) overriding this method.
     * @param checkAccess if false, find all non-abstract methods with same signature even if they do not
     *        override this method.
     * @return a collection of all implementations of this method.
     */
    public Collection<MethodInfo> getImplementations(final boolean checkAccess) {
        final List<MethodInfo> implementations = new LinkedList<MethodInfo>();

        if (!isAbstract()) {
            implementations.add(this);
        }

        if (checkAccess && (isPrivate() || isStatic())) {
            return implementations;
        }

        ClassVisitor visitor = new ClassVisitor() {

            public boolean visitClass(ClassInfo classInfo) {
                MethodInfo m = classInfo.getMethodInfo(getMemberSignature());
                if ( m != null ) {
                    if ( m.isPrivate() && !isPrivate() ) {
                        // found an overriding method which is private .. this is interesting..
                        logger.error("Found private method "+m.getMemberSignature()+" in "+
                                classInfo.getClassName()+" overriding non-private method in "+
                                getClassInfo().getClassName());
                    }
                    if ( !m.isAbstract() && (!checkAccess || m.overrides(MethodInfo.this,false)) ) {
                        implementations.add(m);
                    }
                }
                return true;
            }

            public void finishClass(ClassInfo classInfo) {
            }
        };

        new ClassHierarchyTraverser(visitor).traverseDown(getClassInfo());

        return implementations;
    }

    /**
     * Get a collection of classes local to this method.
     * @return a collection of local classes, or an empty collection of this method does not have local classes.
     */
    public Collection<ClassInfo> getLocalClasses() {
        List<ClassInfo> classes = new LinkedList<ClassInfo>();

        for (ClassInfo nested : getClassInfo().getDirectNestedClasses()) {
            if (nested.isLocalInnerClass() && this.equals(nested.getEnclosingMethodRef().getMethodInfo())) {
                classes.add(nested);
            }
        }

        return classes;
    }

    /**
     * Should only be used by ClassInfo.
     * @return the internal methodGen.                           
     */
    protected MethodGen getMethodGen() {
        return methodGen;
    }

}
