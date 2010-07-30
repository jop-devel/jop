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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class ClassInfo extends MemberInfo {

    private final ClassGen classGen;
    private final ConstantPoolGen cpg;
    private final Set<Integer> removeIndices;

    private final Set<ClassInfo> subClasses;
    private final Set<ClassInfo> interfaces;
    private ClassInfo superClass;

    private final Map<String, MethodInfo> methods;
    private final Map<String, FieldInfo> fields;

    private final Map<AppInfo.CustomKey,Map<Integer, Object>> cpCustomValues;

    public ClassInfo(ClassGen classGen) {
        super(classGen);
        this.classGen = classGen;
        cpg = classGen.getConstantPool();
        removeIndices = new HashSet<Integer>();

        superClass = null;
        subClasses = new HashSet<ClassInfo>();
        interfaces = new HashSet<ClassInfo>();

        methods = new HashMap<String, MethodInfo>();
        fields = new HashMap<String, FieldInfo>();

        cpCustomValues = new HashMap<AppInfo.CustomKey, Map<Integer, Object>>();
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
     * The index will not be marked for removal even if the index was previously marked for removal.
     *
     * @see #addConstantInfo(ConstantInfo)
     * @param i the index of the constant to replace.
     * @param constant the new value.
     * @return true if this index has been previously marked for removal.
     */
    public boolean setConstantInfo(int i, ConstantInfo constant) {
        Constant c = constant.createConstant(cpg);
        cpg.setConstant(i, c);
        return removeIndices.remove(i);
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

    public Object getConstantPoolCustomValue(AppInfo.CustomKey key, int index) {
        Map values = cpCustomValues.get(key);
        if ( values == null ) {
            return null;
        }
        return values.get(index);
    }

    public Object setConstantPoolCustomValue(AppInfo.CustomKey key, int index, Object value) {
        if ( value == null ) {
            return removeConstantPoolCustomValue(key, index);
        }
        Map<Integer,Object> values = cpCustomValues.get(key);
        if ( values == null ) {
            values = new HashMap<Integer, Object>();
            cpCustomValues.put(key, values);
        }
        return values.put(index, value);
    }

    public Object removeConstantPoolCustomValue(AppInfo.CustomKey key, int index) {
        Map<Integer,Object> values = cpCustomValues.get(key);
        if ( values == null ) {
            return null;
        }
        Object value = values.remove(index);
        if ( values.size() == 0 ) {
            cpCustomValues.remove(key);
        }
        return value;
    }

    /**
     * Clear all constant pool custom values for the given key.
     * @param key the key of the values to to clear.
     */
    public void removeConstantPoolCustomValue(AppInfo.CustomKey key) {
        cpCustomValues.remove(key);
    }

    /**
     * Mark a constant to be removed by {@link #cleanupConstantPool()}.
     * Note that the entry will not be removed immediatly, the index of other constants
     * will be changed until {@link #cleanupConstantPool()} is called. Lookup and get methods
     * will still return the constant at this index.
     *
     * @see #cleanupConstantPool() 
     * @param i constant index to be removed.
     * @return true if the index i is valid and has not yet been marked for removal.
     */
    public boolean removeConstant(int i) {
        if ( i < 0 || i >= cpg.getSize() ) {
            return false;
        }
        return removeIndices.add(i);
    }

    /**
     * Rebuild the constantpool and remove all entries marked for removal by {@link #removeConstant(int)}
     * as well as all duplicate entries.
     *
     * <p>This also updates the indices of all references in the code of all methods of this class,
     * therefore do not call this method while modifying the code.</p>
     *
     * @see #removeConstant(int)
     * @return true if the constantpool has been changed
     */
    public boolean cleanupConstantPool() {
        ConstantPoolGen newPool = new ConstantPoolGen();

        // map old index -> new index
        int[] idxMap = new int[cpg.getSize()];
        boolean changed = false;



        if ( !changed ) {
            return false;
        }

        // TODO update all usages of this constantpool

        classGen.setConstantPool(newPool);
        return true;
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
     * Commit all modifications to this ClassInfo and return a BCEL JavaClass for this ClassInfo.
     *
     * <p>This cleans up the constant pool and makes sure all known modifications to the ClassInfo,
     * the constantpool, the fields or the methods are commited to BCEL.</p>
     *
     * @see #cleanupConstantPool()
     * @see MethodInfo#compileCodeRep()
     * @see #getJavaClass()
     * @return a JavaClass representing this ClassInfo.
     */
    public JavaClass compileJavaClass() {

        for (MethodInfo mi : methods.values()) {
            mi.compileCodeRep();
        }

        cleanupConstantPool();

        return classGen.getJavaClass();
    }

    /**
     * Compile and return a BCEL JavaClass for this ClassInfo.
     *
     * <p>The JavaClass does not contain any modifications not yet commited to the internal BCEL
     * ClassGen (e.g. it does not cleanup the constantpool, ..).</p>
     *
     * @see #compileJavaClass() 
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


    protected void resetHierarchyInfos() {

    }

    protected void updateClassHierarchy() {

    }

    protected void updateCompleteFlag(boolean updateSubclasses) {
        
    }

    protected void removeFromClassHierarchy() {

    }
}
