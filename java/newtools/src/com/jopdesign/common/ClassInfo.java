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

import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

    protected final Set<ClassInfo> subClasses;
    protected ClassInfo superClass;
    protected Ternary fullyKnown;

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
        fullyKnown = Ternary.UNKNOWN;

        cpCustomValues = new HashMap<AppInfo.CustomKey, Map<Integer, Object>>();

        Method[] cgMethods = classGen.getMethods();
        Field[] cgFields = classGen.getFields();
        methods = new HashMap<String, MethodInfo>(cgMethods.length);
        fields = new HashMap<String, FieldInfo>(cgFields.length);

        // we create all FieldInfos and MethodInfos now and save a lot of trouble later
        for (Method m : cgMethods) {
            MethodInfo method = new MethodInfo(this, new MethodGen(m, classGen.getClassName(), cpg));
            methods.put(method.getMemberSignature(), method);
        }
        for (Field f : cgFields) {
            FieldInfo field = new FieldInfo(this, new FieldGen(f, cpg));
            fields.put(f.getName(), field);
        }
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
        if ( c == null ) {
            return null;
        }
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

        if ( removeIndices.isEmpty() ) {
            return false;
        }

        ConstantPoolGen newPool = new ConstantPoolGen();

        // map old index -> new index
        int[] idxMap = new int[cpg.getSize()];

        for (int i = 0; i < idxMap.length; i++) {
            if ( removeIndices.contains(i) ) {
                idxMap[i] = -1;
                continue;
            }
            Constant c = cpg.getConstant(i);
            if ( c == null ) {
                continue;
            }
            idxMap[i] = newPool.addConstant(c, cpg);
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
     * Get a set of all (loaded) interfaces this class directly implements.
     *
     * @return a set of all known directly implemented classInfos.
     */
    public Set<ClassInfo> getInterfaces() {
        String[] names = classGen.getInterfaceNames();
        Set<ClassInfo> interfaces = new HashSet<ClassInfo>(names.length);
        for (String name : names) {
            ClassInfo cls = getAppInfo().getClassInfo(name);
            if (cls != null) {
                interfaces.add(cls);
            }
        }
        return interfaces;
    }

    /**
     * Get a set of all superclasses (including this class) and all implemented/extended interfaces.
     *
     * @return a set of all superclasses and all interfaces of this class.
     */
    public Set<ClassInfo> getAncestors() {
        Set<ClassInfo> sc = new HashSet<ClassInfo>();
        List<ClassInfo> queue = new LinkedList<ClassInfo>();

        queue.add(this);
        while (!queue.isEmpty()) {
            ClassInfo cls = queue.remove(0);
            sc.add(cls);

            ClassInfo superClass = cls.getSuperClassInfo();
            if ( superClass != null && !sc.contains(superClass) ) {
                queue.add(cls);
            }
            for (ClassInfo i : cls.getInterfaces()) {
                if ( !sc.contains(i) ) {
                    queue.add(i);
                }
            }
        }

        return sc;
    }

    /**
     * Get a set of all known direct subclasses of this class if this is a class,
     * or if this is an interface, get a collection of all known direct implementations
     * and all known direct extensions.
     *
     * @return a set of all known direct subclasses and implementations of this class or interface.
     */
    public Set<ClassInfo> getDirectSubclasses() {
        return subClasses;
    }

    /**
     * Check if all superclasses of this class or interface are loaded.
     * If checkInterfaces is true or if this is class is an interface, also check if all extended/implemented
     * interfaces are loaded.
     *
     * @param checkInterfaces If this is a class, if true check interfaces too, else only the superclasses are checked.
     *                        If this is an interface, the interfaces of this interface are always checked.
     * @return true if all superclasses/interfaces are loaded or if this is 'java.lang.Object'.
     */
    public boolean isFullyKnown(boolean checkInterfaces) {
        if (checkInterfaces || isInterface()) {
            Set<ClassInfo> interfaces = getInterfaces();
            if (interfaces.size() < classGen.getInterfaces().length ) {
                return false;
            }
            for (ClassInfo i : interfaces) {
                if (!i.isFullyKnown(true)) {
                    return false;
                }
            }
        }
        if (fullyKnown == Ternary.TRUE) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (fullyKnown == Ternary.UNKNOWN && "java.lang.Object".equals(classGen.getClassName())) {
            return true;
        }
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
        return fields.get(name);
    }


    public MethodInfo getMethodInfo(Signature signature) {
        return getMethodInfo(signature.getMemberSignature());
    }

    public MethodInfo getMethodInfo(String memberSignature) {
        return methods.get(memberSignature);
    }

    public Set<MethodInfo> getMethodByName(String name) {
        Set<MethodInfo> mList = new HashSet<MethodInfo>();
        for (MethodInfo m : methods.values()) {
            if (m.getName().equals(name)) {
                mList.add(m);
            }
        }
        return mList;
    }

    public MethodInfo getMethodInfoVirtual(Signature signature, boolean ignoreAccess) {
        ClassInfo cls = this;
        while ( cls != null ) {
            MethodInfo m = cls.getMethodInfo(signature);
            if ( m != null ) {
                if ( ignoreAccess || canAccess(m) ) {
                    return m;
                } else {
                    return null;
                }
            }
            cls = cls.getSuperClassInfo();
        }
        return null;
    }

    public FieldInfo getFieldVirtual(String name, boolean ignoreAccess) {
        ClassInfo cls = this;
        while ( cls != null ) {
            FieldInfo f = cls.getFieldInfo(name);
            if ( f != null ) {
                if ( ignoreAccess || canAccess(f) ) {
                    return f;
                } else {
                    return null;
                }
            }
            cls = cls.getSuperClassInfo();
        }
        return null;
    }

    /**
     * Get a reference to the outer class if this is an inner class, else return
     * null.
     *
     * @return a classRef to the parent class or null if this is not an inner class.
     */
    public ClassRef getOuterClass() {
        int idx = classGen.getClassName().lastIndexOf('$');
        if ( idx == -1 ) {
            return null;
        }
        return getAppInfo().getClassRef(classGen.getClassName().substring(0, idx));
    }

    public String getPackageName() {
        String clsName = getClassName();
        int index = clsName.lastIndexOf('.');
        if ( index > 0 ) {
            return clsName.substring(0, index);
        } else {
            return "";
        }
    }

    public boolean hasSamePackage(ClassInfo classInfo) {
        return getPackageName().equals(classInfo.getPackageName());
    }

    /**
     * Check if the given class is an outer class of this class.
     *
     * @param outer the potential outer class.
     * @return true if this class is an inner class of the given class.
     */
    public boolean isInnerclassOf(ClassInfo outer) {
        return getClassName().startsWith(outer.getClassName()+"$");
    }

    /**
     * Check if the given class is the same as this class or a subclass of this class.
     *
     * @param classInfo the possible subclass of this class.
     * @return true if the given class is this class or a superclass of this class.
     */
    public boolean isSuperclassOf(ClassInfo classInfo) {
        ClassInfo cls = classInfo;
        while ( cls != null ) {
            if ( this.equals(cls) ) {
                return true;
            }
            cls = cls.getSuperClassInfo();
        }
        return false;
    }

    /**
     * Check if the given class is an extension of this class, i.e. if this is a class,
     * check if the given class is a subclass, if this is an interface, check if the given
     * class is an interface and if this is an extension of the given interface.
     *
     * @param classInfo the class to check.
     * @return true if the class is an extension of this class.
     */
    public boolean isExtensionOf(ClassInfo classInfo) {
        if ( !isInterface() ) {
            return isSuperclassOf(classInfo);
        }
        if ( !classInfo.isInterface() ) {
            return false;
        }
        Set<ClassInfo> interfaces = classInfo.getAncestors();
        return interfaces.contains(this);
    }

    /**
     * Check if this class is either an extension or an implementation of the given class or
     * interface.
     *
     * @param classInfo the super class to check.
     * @return true if this class is is a subtype of the given class.
     */
    public boolean isSubtypeOf(ClassInfo classInfo) {
        if ( !classInfo.isInterface() ) {
            return classInfo.isSuperclassOf(this);
        }
        // if classInfo is an interface..
        Set<ClassInfo> supers = getAncestors();
        return supers.contains(classInfo);
    }

    /**
     * Check if the given member can be accessed by this class.
     *
     * @param member the member to access.
     * @return true if this class is allowed to access the member.
     */
    public boolean canAccess(MemberInfo member) {
        return canAccess(member.getClassInfo(), member.getAccessType());
    }

    /**
     * Check if a member of another class with the given accessType can be accessed by this class.
     *
     * @param cls the class containing the member to check.
     * @param accessType the accessType of the member to check, as returned by {@link MemberInfo#getAccessType()}.
     * @return true if this class is allowed to access members of the given accessType of the given class.
     */
    public boolean canAccess(ClassInfo cls, int accessType) {
        switch (accessType) {
            case ACC_PUBLIC:
                return true;
            case ACC_PROTECTED:
                if ( cls.isSuperclassOf(this) ) {
                    return true;
                }
            case ACC_PACKAGE:
                return this.hasSamePackage(cls);
            case ACC_PRIVATE:
                return this.equals(cls) || cls.isInnerclassOf(this);
        }
        return false;
    }

    /**
     * Create a new non-static, package-visible field with the given name and type.
     * If a field by that name exists, the existing field is returned.
     *
     * @param name the name of the new field
     * @param type the type of the field
     * @return the new field or an existing field with that name.
     */
    public FieldInfo createField(String name, Type type) {
        FieldInfo field = fields.get(name);
        if ( field != null ) {
            return field;
        }
        field = new FieldInfo(this, new FieldGen(0, type, name, cpg));
        fields.put(name,field);
        // TODO call manager eventhandler
        return field;
    }

    /**
     * Create a new non-static, non-abstract, package-visible method with the given name and descriptor.
     *
     * @param signature the membername and descriptor of the method (classname is ignored).
     * @param argNames the names of the parameters
     * @return the new method or an existing method with that signature.
     */
    public MethodInfo createMethod(Signature signature, String[] argNames) {
        MethodInfo method = methods.get(signature.getMemberSignature());
        if ( method != null ) {
            return method;
        }
        Descriptor desc = signature.getMemberDescriptor();
        method = new MethodInfo(this, new MethodGen(0, desc.getType(), desc.getArgumentTypes(), argNames,
                signature.getMemberName(), classGen.getClassName(), null, cpg));
        methods.put(signature.getMemberSignature(), method);
        // TODO call manager eventhandler
        return method;
    }

    public MethodInfo copyMethod(String memberSignature, String newName) {
        MethodInfo method = methods.get(memberSignature);
        if ( method == null ) {
            return null;
        }
        method.compileCodeRep();
        MethodGen methodGen = new MethodGen(method.getMethod(), getClassName(), cpg);
        methodGen.setName(newName);

        MethodInfo newMethod = new MethodInfo(this, methodGen);
        
        // TODO copy all the attribute stuff?, call manager eventhandler

        methods.put(newMethod.getMemberSignature(), newMethod);
        return newMethod;
    }

    public FieldInfo copyField(String name, String newName) {
        FieldInfo field = fields.get(name);
        if ( field == null ) {
            return null;
        }
        FieldGen fieldGen = new FieldGen(field.getField(), cpg);
        fieldGen.setName(newName);

        FieldInfo newField = new FieldInfo(this, fieldGen);

        // TODO copy all the attribute stuff?, call manager eventhandler

        fields.put(newName, newField);
        return newField;
    }

    public MethodInfo renameMethod(String memberSignature, String newName) {
        MethodInfo method = methods.remove(memberSignature);
        if ( method == null ) {
            return null;
        }
        method.getMethodGen().setName(newName);
        methods.put(method.getMemberSignature(), method);
        // TODO call manager eventhandler
        return method;
    }

    public FieldInfo renameField(String name, String newName) {
        FieldInfo field = fields.remove(name);
        if ( field == null ) {
            return null;
        }
        field.getFieldGen().setName(newName);
        fields.put(newName, field);
        // TODO call manager eventhandler
        return field;
    }

    public FieldInfo removeField(String name) {
        // TODO call manager eventhandler
        return fields.remove(name);
    }

    public MethodInfo removeMethod(Signature signature) {
        // TODO call manager eventhandler
        return methods.remove(signature.getMemberSignature());
    }

    public String getClassName() {
        return classGen.getClassName();
    }

    /**
     * Return a collection of all fields of this class.
     * You really should not modify this list directly.
     *
     * @return a collection of all fields.
     */
    public Collection<FieldInfo> getFields() {
        return fields.values();
    }

    /**
     * Return a collection of all methods of this class.
     * You really should not modify this list directly.
     *
     * @return a collection of all methods.
     */
    public Collection<MethodInfo> getMethods() {
        return methods.values();
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

        // remove everything from constantpool marked for removal
        cleanupConstantPool();

        // TODO add/update all attributes, methodInfos and fieldInfos to classGen
        
        return classGen.getJavaClass();
    }

    /**
     * Compile and return a BCEL JavaClass for this ClassInfo.
     *
     * <p>The JavaClass does not contain any modifications not yet commited to the internal BCEL
     * ClassGen (e.g. it does not cleanup the constantpool, add new methods, ..).</p>
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
        superClass = null;
        subClasses.clear();
        fullyKnown = Ternary.UNKNOWN;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void updateClassHierarchy() {
        AppInfo appInfo = AppInfo.getSingleton();
        superClass = appInfo.getClassInfo(classGen.getSuperclassName());
        if ( superClass != null ) {
            superClass.subClasses.add(this);
        }
    }

    protected void updateCompleteFlag(boolean updateSubclasses) {

        

        if ( updateSubclasses ) {

        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void removeFromClassHierarchy() {
        if ( superClass != null ) {
            superClass.subClasses.remove(this);
        }
        // interface references are not stored, direct subclasses of a class are only classes, so
        // all their superclasses must be this class
        if ( !isInterface() ) {
            for (ClassInfo c : subClasses) {
                c.superClass = null;
            }
        }
        // all extensions of this are now incomplete


        resetHierarchyInfos();
    }
}
