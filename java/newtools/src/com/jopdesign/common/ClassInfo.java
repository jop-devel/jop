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

import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class ClassInfo extends MemberInfo {

    private final ClassGen classGen;
    private final ConstantPoolGen cpg;

    private final Set<ClassInfo> subClasses;
    private final Set<ClassInfo> interfaces;
    private ClassInfo superClass;

    public ClassInfo(ClassGen classGen) {
        super(classGen);
        this.classGen = classGen;
        cpg = classGen.getConstantPool();

        superClass = null;
        subClasses = new HashSet<ClassInfo>();
        interfaces = new HashSet<ClassInfo>();
    }
    
    @Override
    public ClassInfo getClassInfo() {
        return this;
    }

    @Override
    public Signature getSignature() {
        return new Signature(classGen.getClassName(), null, null);
    }

    public ConstantInfo getConstantInfo(int i) {
        if ( i < 0 || i >= cpg.getSize() ) {
            return null;
        }
        Constant c = cpg.getConstant(i);
        return ConstantInfo.createFromConstant(cpg.getConstantPool(), c);
    }

    public Constant getConstant(int i) {
        return cpg.getConstant(i);
    }

    public ConstantPoolGen getConstantPoolGen() {
        return cpg;
    }

    /**
     * Replace a constant with a new constant value in the constant pool.
     * Be aware that this does not check for duplicate entries, and may create additional
     * new entries in the constant pool.
     *
     * @param i the index of the constant to replace.
     * @param constant the new value.
     */
    public void setConstantInfo(int i, ConstantInfo constant) {
        Constant c = constant.createConstant(cpg);
        cpg.setConstant(i, c);
    }

    /**
     * Add a constant in the constant pool or return the index of an existing entry.
     * To add individual constants, use the add-methods from {@link #getConstantPoolGen()}.
     *
     * @param constant the constant to add
     * @return the index of the constant entry in the constant pool.
     */
    public int addConstantInfo(ConstantInfo constant) {
        return constant.addConstant(cpg);
    }

    /**
     * Lookup the index of a constant in the constant pool.
     * To lookup individual constants, use the lookup-methods from {@link #getConstantPoolGen()}.
     *
     * @param constant the constant to look up
     * @return the index in the constant pool, or -1 if not found.
     */
    public int lookupConstantInfo(ConstantInfo constant) {
        return constant.lookupConstant(cpg);
    }

    public int getConstantPoolSize() {
        return cpg.getSize();
    }

    public ConstantInfo removeConstantInfo(int i) {
        if ( i < 0 || i >= cpg.getSize() ) {
            return null;
        }
        
        return null;
    }

    /**
     * Get the ClassInfo of the superClass if it is known, else return null.
     * @return the superclass ClassInfo or null if not loaded.
     */
    public ClassInfo getSuperClassInfo() {
        return superClass;
    }

    /**
     * Get a collection of all (loaded) interfaces this class directly implements.
     *
     * 
     * @return a collection of all known directly implemented classInfos.
     */
    public Collection<ClassInfo> getInterfaces() {
        return interfaces;
    }

    /**
     * Get a collection of all known direct subclasses of this class if this is a class,
     * or if this is an interface, get a collection of all known direct implementations
     * and all known direct extensions.
     *
     * @return a collection of all known direct subclasses and implementations of this class or interface.
     */
    public Collection<ClassInfo> getKnownSubClasses() {
        return subClasses;
    }

    /**
     * Check if all superclasses and implemented interfaces of this class or interface are known.
     * @return true if all superclasses and implemented interfaces are loaded.
     */
    public boolean isFullyKnown() {
        return false;
    }

    public boolean isInterface() {
        return classGen.isInterface();
    }

    public boolean isAbstract() {
        return classGen.isAbstract();
    }

    public void setAbstract(boolean val) {
        classGen.isAbstract(val);
    }

    public boolean isStrictFP() {
        return classGen.isStrictfp();
    }

    public void setStrictFP(boolean val) {
        classGen.isStrictfp(val);
    }

    public String getSuperClassName() {
        return classGen.getSuperclassName();
    }

    public String[] getInterfaceNames() {
        return classGen.getInterfaceNames();
    }

    public FieldInfo getFieldInfo(String name) {
        return null;
    }

    public MethodInfo getMethodInfo(Signature signature) {
        return getMethodInfo(signature.getMemberSignature());
    }

    public MethodInfo getMethodInfo(String memberSignature) {
        return null;
    }

    public MethodInfo[] getMethodByName(String name) {
        return new MethodInfo[]{};
    }

    public FieldInfo createField(String name, Type type) {
        return null;
    }

    public MethodInfo createMethod(Signature signature) {
        return null;
    }

    public MethodInfo copyMethod(Signature signature, String newName) {
        return null;
    }

    public FieldInfo copyField(String name, String newName) {
        return null;
    }

    public MethodInfo renameMethod(Signature signature, String newName) {
        return null;
    }

    public FieldInfo renameField(String name, String newName) {
        return null;
    }

    public FieldInfo removeField(String name) {
        return null;
    }

    public MethodInfo removeMethod(String signature) {
        return null;
    }

    public String getClassName() {
        return classGen.getClassName();
    }

    public Collection<FieldInfo> getFields() {
        return null;
    }

    public Collection<MethodInfo> getMethods() {
        return null;
    }

    @Override
    public String getModifierString() {
        String s = super.getModifierString();
        if ( isInterface() ) {
            s += "interface ";
        } else {
            s += "class ";
        }
        return s;
    }

    public ClassRef getClassRef() {
        return new ClassRef(this);
    }

    /**
     * Compile and return the BCEL JavaClass for this ClassInfo.
     *
     * @return a JavaClass for this ClassInfo.
     */
    public JavaClass getJavaClass() {
        return classGen.getJavaClass();
    }

    public int hashCode() {
        return classGen.getClassName().hashCode();
    }

    public boolean equals(Object o) {
        if ( !(o instanceof ClassInfo)) {
            return false;
        }
        return ((ClassInfo)o).getClassName().equals(getClassName());
    }
}
