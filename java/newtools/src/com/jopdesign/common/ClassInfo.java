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

import com.jopdesign.common.graph.ClassHierarchyTraverser;
import com.jopdesign.common.graph.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
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
    private       ConstantPoolGen cpg;

    protected final Set<ClassInfo> subClasses;
    protected ClassInfo superClass;
    protected Ternary fullyKnown;

    private final Map<String, MethodInfo> methods;
    private final Map<String, FieldInfo> fields;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".ClassInfo");

    public ClassInfo(ClassGen classGen) {
        super(classGen);
        this.classGen = classGen;
        cpg = classGen.getConstantPool();

        superClass = null;
        subClasses = new HashSet<ClassInfo>();
        fullyKnown = Ternary.UNKNOWN;

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

    //////////////////////////////////////////////////////////////////////////////
    // Various getter and setter
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public ClassInfo getClassInfo() {
        return this;
    }

    @Override
    public Signature getSignature() {
        return new Signature(classGen.getClassName(), null, null);
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

    public String getClassName() {
        return classGen.getClassName();
    }

    public ClassRef getClassRef() {
        return new ClassRef(this);
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

    @Override
    public Attribute[] getAttributes() {
        return classGen.getAttributes();
    }

    @Override
    public void addAttribute(Attribute a) {
        classGen.addAttribute(a);
    }

    @Override
    public void removeAttribute(Attribute a) {
        classGen.removeAttribute(a);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Access to the constantpool, lookups and modification
    //////////////////////////////////////////////////////////////////////////////

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

    public ConstantInfo getConstantInfo(Constant c) {
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
     * @see #addConstantInfo(ConstantInfo)
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

    /**
     * Rebuild the constantpool from a new, empty constantpool.
     *
     * <p>This updates the indices of all references in the code of all methods of this class,
     * therefore do not call this method while modifying the code.</p>
     */
    public void rebuildConstantPool() {

        ConstantPoolGen newPool = new ConstantPoolGen();
        ConstantPoolRebuilder rebuilder = new ConstantPoolRebuilder(cpg, newPool);

        rebuilder.updateClassGen(classGen);

        for (MethodInfo m : methods.values()) {
            rebuilder.updateMethodGen(m.getMethodGen());
        }
        for (FieldInfo f : fields.values()) {
            rebuilder.updateFieldGen(f.getFieldGen());
        }

        // TODO update all attributes, update/remove customValues which depend on CP and call eventbroker  

        cpg = newPool;
    }


    //////////////////////////////////////////////////////////////////////////////
    // Class-hierarchy access
    //////////////////////////////////////////////////////////////////////////////

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


    //////////////////////////////////////////////////////////////////////////////
    // Class-hierarchy lookups and helpers
    //////////////////////////////////////////////////////////////////////////////

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


    //////////////////////////////////////////////////////////////////////////////
    // Various helper, access checks
    //////////////////////////////////////////////////////////////////////////////

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
                // fallthrough
            case ACC_PACKAGE:
                return this.hasSamePackage(cls);
            case ACC_PRIVATE:
                return this.equals(cls) || cls.isInnerclassOf(this);
        }
        return false;
    }


    //////////////////////////////////////////////////////////////////////////////
    // Inner-class stuff
    //////////////////////////////////////////////////////////////////////////////

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

    /**
     * Check if the given class is an outer class of this class.
     *
     * @param outer the potential outer class.
     * @return true if this class is an inner class of the given class.
     */
    public boolean isInnerclassOf(ClassInfo outer) {
        return getClassName().startsWith(outer.getClassName()+"$");
    }


    //////////////////////////////////////////////////////////////////////////////
    // Access to fields and methods, lookups
    //////////////////////////////////////////////////////////////////////////////

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

    /**
     * Return a collection of all fields of this class.
     * You really should not modify this list directly.
     *
     * @return a collection of all fields.
     */
    public Collection<FieldInfo> getFields() {
        return Collections.unmodifiableCollection(fields.values());
    }

    /**
     * Return a collection of all methods of this class.
     * You really should not modify this list directly.
     *
     * @return a collection of all methods.
     */
    public Collection<MethodInfo> getMethods() {
        return Collections.unmodifiableCollection(methods.values());
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
     * Get the index of the given method in the class method array, or -1 if not found.
     *
     * @param memberSignature name and descriptor of the method to find.
     * @return the index in the methods array or -1 if not found.
     */
    public int lookupMethodInfo(String memberSignature) {
        Method[] methods = classGen.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            String s = Signature.getMemberSignature(m.getName(), m.getSignature());
            if ( s.equals(memberSignature) ) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the index of the given field in the class field array, or -1 if not found.
     *
     * @param fieldName the name of the field to found.
     * @return the index in the fields array or -1 if not found.
     */
    public int lookupFieldInfo(String fieldName) {
        Field[] fields = classGen.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if ( f.getName().equals(fieldName) ) {
                return i;
            }
        }
        return -1;
    }


    //////////////////////////////////////////////////////////////////////////////
    // Modify fields and methods (create, copy, rename, remove)
    //////////////////////////////////////////////////////////////////////////////

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
        classGen.addField(field.getField());

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
        classGen.addMethod(method.getMethod(false));

        // TODO call manager eventhandler

        return method;
    }

    public MethodInfo copyMethod(String memberSignature, String newName) {
        MethodInfo method = methods.get(memberSignature);
        if ( method == null ) {
            return null;
        }
        method.compileCodeRep();
        MethodGen methodGen = new MethodGen(method.compileMethod(), getClassName(), cpg);
        methodGen.setName(newName);

        MethodInfo newMethod = new MethodInfo(this, methodGen);
        
        // TODO copy all the attribute stuff?, call manager eventhandler

        methods.put(newMethod.getMemberSignature(), newMethod);
        classGen.addMethod(newMethod.getMethod(false));

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
        classGen.addField(newField.getField());

        return newField;
    }

    public MethodInfo renameMethod(String memberSignature, String newName) {
        MethodInfo method = methods.remove(memberSignature);
        if ( method == null ) {
            return null;
        }
        method.getMethodGen().setName(newName);

        methods.put(method.getMemberSignature(), method);
        int i = lookupMethodInfo(memberSignature);
        if ( i == -1 ) {
            // This should never happen
            throw new JavaClassFormatError("Renaming method "+memberSignature+" in " +getClassName()+ " to " + newName
                    +", but old method was not found in classGen!");
        }
        classGen.setMethodAt(method.getMethod(false), i);

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
        int i = lookupFieldInfo(name);
        if ( i == -1 ) {
            // This should never happen
            throw new JavaClassFormatError("Renaming field "+name+" in " +getClassName()+ " to " + newName
                    +", but old field was not found in classGen!");
        }
        // Damn, BCEL does not have a setFieldAt method
        classGen.removeField(classGen.getFields()[i]);
        classGen.addField(field.getField());


        // TODO call manager eventhandler

        return field;
    }

    public FieldInfo removeField(String name) {
        FieldInfo fieldInfo = fields.remove(name);
        if ( fieldInfo == null ) {
            return null;
        }

        int i = lookupFieldInfo(name);
        if ( i == -1 ) {
            // this should never happen
            throw new JavaClassFormatError("Removing field "+name+" in " +getClassName()
                    +", but field was not found in classGen!");
        }
        classGen.removeField(classGen.getFields()[i]);

        // TODO call manager eventhandler

        return fieldInfo;
    }

    public MethodInfo removeMethod(String memberSignature) {
        MethodInfo methodInfo = methods.remove(memberSignature);
        if ( methodInfo == null ) {
            return null;
        }

        int i = lookupMethodInfo(memberSignature);
        if ( i == -1 ) {
            // this should never happen
            throw new JavaClassFormatError("Removing method "+memberSignature+" in " +getClassName()
                    +", but method was not found in classGen!");
        }
        classGen.removeMethod(classGen.getMethodAt(i));

        // TODO call manager eventhandler

        return methodInfo;
    }


    //////////////////////////////////////////////////////////////////////////////
    // BCEL stuff
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Commit all modifications to this ClassInfo and return a BCEL JavaClass for this ClassInfo.
     *
     * @see MethodInfo#compileCodeRep()
     * @see #getJavaClass(boolean)
     * @return a JavaClass representing this ClassInfo.
     */
    public JavaClass compileJavaClass() {

        // TODO We could keep a modified flag in both MethodInfo and FieldInfo
        // (maybe even ClassInfo), and update only what is needed here
        // could make class-writing,.. faster, but makes code more complex

        Field[] fList = classGen.getFields();
        if (fList.length != fields.size() ) {
            // should never happen
            throw new JavaClassFormatError("Number of fields in classGen of " + getClassName() +
                    " differs from number of FieldInfos!");
        }
        for (Field f : fList) {
            classGen.replaceField(f, fields.get(f.getName()).getField());
        }

        Method[] mList = classGen.getMethods();
        if (mList.length != methods.size()) {
            // should never happen
            throw new JavaClassFormatError("Number of methods in classGen of " + getClassName() +
                    " differs from number of MethodInfos!");
        }
        for (int i = 0; i < mList.length; i++) {
            MethodInfo method = methods.get(Signature.getMemberSignature(mList[i].getName(),
                                                                         mList[i].getSignature()));
            classGen.setMethodAt(method.compileMethod(), i);
        }

        // TODO call manager eventhandler

        return classGen.getJavaClass();
    }

    /**
     * Compile and return a BCEL JavaClass for this ClassInfo.
     *
     * <p>If compile is false, then the JavaClass does not contain any modifications not yet
     * commited to the internal BCEL ClassGen (e.g. it does not cleanup the constantpool, does not contain
     * modifications to methods/fields/code, ..).</p>
     *
     * @see #compileJavaClass()
     * @param compile if true, this does the same as {@link #compileJavaClass()}
     * @return a JavaClass for this ClassInfo.
     */
    public JavaClass getJavaClass(boolean compile) {
        if ( compile ) {
            // using the compile flag here primarily as a reminder to the API user to compile first
            return compileJavaClass();
        }
        return classGen.getJavaClass();
    }


    //////////////////////////////////////////////////////////////////////////////
    // hashCode, equals
    //////////////////////////////////////////////////////////////////////////////

    public int hashCode() {
        return classGen.getClassName().hashCode();
    }

    public boolean equals(Object o) {
        if ( !(o instanceof ClassInfo)) {
            return false;
        }
        return ((ClassInfo)o).getClassName().equals(getClassName());
    }


    //////////////////////////////////////////////////////////////////////////////
    // Internal affairs, class hierarchy management; To be used only be AppInfo
    //////////////////////////////////////////////////////////////////////////////

    protected void resetHierarchyInfos() {
        superClass = null;
        subClasses.clear();
        fullyKnown = Ternary.UNKNOWN;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void updateClassHierarchy() {
        AppInfo appInfo = AppInfo.getSingleton();
        if ( "java.lang.Object".equals(getClassName()) ) {
            superClass = null;
        } else {
            superClass = appInfo.getClassInfo(classGen.getSuperclassName());
        }
        if ( superClass != null ) {
            superClass.subClasses.add(this);
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void updateCompleteFlag(boolean updateSubclasses) {

        if ( isInterface() ) {
            // need to check all extended interfaces
            Set<ClassInfo> interfaces = getInterfaces();
            if ( interfaces.size() < classGen.getInterfaceNames().length ) {
                // some interfaces are not loaded
                fullyKnown = Ternary.FALSE;
            } else {
                boolean known = true;
                // if this interface extends no other interface, 'known' will stay true
                // else we check if all extended interfaces are known
                for (ClassInfo i : interfaces) {
                    // update interfaces recursively first
                    if ( i.fullyKnown == Ternary.UNKNOWN ) {
                        i.updateCompleteFlag(false);
                    }
                    known &= i.fullyKnown == Ternary.TRUE;
                }
                fullyKnown = Ternary.valueOf(known);
            }

        } else if ( superClass == null ) {
            fullyKnown = Ternary.valueOf("java.lang.Object".equals(getClassName()));
        } else {
            // if superclass is unknown, update recursively first
            if ( superClass.fullyKnown == Ternary.UNKNOWN ) {
                superClass.updateCompleteFlag(false);
            }
            fullyKnown = superClass.fullyKnown;
        }

        if ( updateSubclasses ) {
            for (ClassInfo c : subClasses) {
                // we need to recurse down here since we cannot set the fullyKnown flag
                // of interfaces directly
                c.updateCompleteFlag(true);
            }
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void removeFromClassHierarchy() {
        if ( superClass != null ) {
            superClass.subClasses.remove(this);
        }

        // all extensions of this will now be incomplete
        if ( fullyKnown == Ternary.TRUE ) {
            ClassVisitor visitor = new ClassVisitor() {

                public boolean visitClass(ClassInfo classInfo) {
                    classInfo.fullyKnown = Ternary.FALSE;
                    return true;
                }

                public void finishClass(ClassInfo classInfo) {
                }
            };
            ClassHierarchyTraverser traverser = new ClassHierarchyTraverser(visitor, false);
            traverser.setExtensionsOnly(true);
            traverser.traverse(this);
        }

        // interface references are not stored; direct subclasses of a class are only classes, so
        // all their superclasses must be this class, so unset them
        if ( !isInterface() ) {
            for (ClassInfo c : subClasses) {
                c.superClass = null;
            }
        }

        resetHierarchyInfos();
    }
}
