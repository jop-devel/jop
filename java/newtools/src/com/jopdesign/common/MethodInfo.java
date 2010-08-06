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

import com.jopdesign.common.code.CodeRepresentation;
import com.jopdesign.common.graph.ClassHierarchyTraverser;
import com.jopdesign.common.graph.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
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

    public boolean rollbackCodeRep() {
        if ( codeRep == null ) {
            return false;
        }
        codeRep = null;
        return true;
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    public MethodRef getMethodRef() {
        return new MethodRef(this);
    }

    public String getMemberSignature() {
        return methodGen.getName() + methodGen.getSignature();
    }

    @Override
    public Signature getSignature() {
        return new Signature(getClassInfo().getClassName(), getName(), getDescriptor());
    }

    public MethodInfo getSuperMethod(boolean ignoreAccess) {
        // TODO check: if this is a private method and not ignoreAccess, always return null?

        ClassInfo superClass = getClassInfo().getSuperClassInfo();
        MethodInfo superMethod = null;
        while (superClass != null) {
            superMethod = superClass.getMethodInfo(getMemberSignature());
            if ( superMethod != null ) {
                break;
            }
            superClass = superClass.getSuperClassInfo();
        }
        if (superMethod != null && !ignoreAccess && superMethod.isPrivate()) {
            return null;
        }
        return superMethod;
    }

    public Collection<MethodInfo> findInterfaceMethods() {
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

        new ClassHierarchyTraverser(visitor, true).traverse(getClassInfo());

        return ifMethods;
    }
    
    public Collection<MethodInfo> findOverriders(boolean ignoreAccess) {
        final List<MethodInfo> overriders = new LinkedList<MethodInfo>();

        if (!ignoreAccess && isPrivate()) {
            return overriders;
        }

        ClassVisitor visitor = new ClassVisitor() {

            public boolean visitClass(ClassInfo classInfo) {
                MethodInfo overrider = classInfo.getMethodInfo(getMemberSignature());
                if ( overrider != null ) {
                    if ( overrider.isPrivate() && !isPrivate() ) {
                        // found an overriding method which is private .. this is interesting..
                        logger.warn("Found private method "+overrider.getMemberSignature()+" in "+
                                classInfo.getClassName()+" overriding non-private method in "+
                                getClassInfo().getClassName());
                    }
                    overriders.add(overrider);
                }
                return true;
            }

            public void finishClass(ClassInfo classInfo) {
            }
        };

        new ClassHierarchyTraverser(visitor, false).traverse(getClassInfo());

        return overriders;
    }

    /**
     * Should only be used by ClassInfo.
     * @return the internal methodGen.                           
     */
    protected MethodGen getMethodGen() {
        return methodGen;
    }

}
