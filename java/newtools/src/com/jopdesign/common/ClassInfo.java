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

import com.jopdesign.common.bcel.EnclosingMethod;
import com.jopdesign.common.graph.ClassHierarchyTraverser;
import com.jopdesign.common.graph.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantClassInfo;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
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
    protected final Set<ClassInfo> nestedClasses;
    protected ClassInfo enclosingClass;
    protected boolean aNestedClass;
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
        enclosingClass = null;
        aNestedClass = false;
        nestedClasses = new HashSet<ClassInfo>();
        fullyKnown = Ternary.UNKNOWN;

        updateInnerClassFlag();

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
    // Various getter and setter, modify class
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
    public String getSimpleName() {
        String name = getClassName();
        return name.substring(name.lastIndexOf('.')+1);
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

    /**
     * Get the fully qualified name of this class.
     * @return the FQN.
     */
    public String getClassName() {
        return classGen.getClassName();
    }

    public ClassRef getClassRef() {
        return new ClassRef(this);
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

    public int getMajor() {
        return classGen.getMajor();
    }

    public int getMinor() {
        return classGen.getMinor();
    }

    public void setMinor(int minor) {
        classGen.setMinor(minor);
    }

    public void setMajor(int major) {
        classGen.setMajor(major);
    }

    public void setAnnotation(boolean flag) {
        classGen.isAnnotation(flag);
    }

    public boolean isAnnotation() {
        return classGen.isAnnotation();
    }

    public void setEnum(boolean flag) {
        classGen.isEnum(flag);
    }

    public boolean isEnum() {
        return classGen.isEnum();
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
        if ( i < 1 || i >= cpg.getSize() ) {
            return null;
        }
        Constant c = cpg.getConstant(i);
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
    // Superclass and Interfaces
    //////////////////////////////////////////////////////////////////////////////

    public String getSuperClassName() {
        return classGen.getSuperclassName();
    }

    /**
     * Get the ClassInfo of the superClass if it is known, else return null.
     * @return the superclass ClassInfo or null if not loaded.
     */
    public ClassInfo getSuperClassInfo() {
        return superClass;
    }

    public boolean isInterface() {
        return classGen.isInterface();
    }

    /**
     * Set interface flag of this class.
     *
     * <p>This does not check if this is a valid operation, i.e. you need to check
     * for yourself if all methods are abstract and public and this class is not a superclass of a
     * normal class if you make this class an interface, ...</p>
     *
     * @param val the new value of the interface flag.
     */
    public void setInterface(boolean val) {
        classGen.isInterface(val);
    }

    public String[] getInterfaceNames() {
        return classGen.getInterfaceNames();
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

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public void addInterface(ClassInfo classInfo) {
        classGen.addInterface(classInfo.getClassName());
        classInfo.subClasses.add(this);
        if ( fullyKnown == Ternary.TRUE && isInterface() ) {
            if ( classInfo.fullyKnown == Ternary.UNKNOWN ) {
                classInfo.updateCompleteFlag(false);
            }
            fullyKnown = classInfo.fullyKnown;
        }
    }

    public void addInterface(ClassRef classRef) {
        ClassInfo cls = classRef.getClassInfo();
        if (cls != null) {
            addInterface(cls);
        } else {
            // adding unknown interface
            classGen.addInterface(classRef.getClassName());
            if ( fullyKnown != Ternary.UNKNOWN && isInterface() ) {
                fullyKnown = Ternary.FALSE;
            }
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    public void removeInterface(ClassInfo classInfo) {
        classGen.removeInterface(classInfo.getClassName());
        classInfo.subClasses.remove(this);
    }

    public void removeInterface(ClassRef classRef) {
        ClassInfo cls = classRef.getClassInfo();
        if (cls != null) {
            removeInterface(cls);
        } else {
            // removing unknown interface
            classGen.removeInterface(classRef.getClassName());
        }
    }


    //////////////////////////////////////////////////////////////////////////////
    // Inner-class stuff
    //////////////////////////////////////////////////////////////////////////////

    public boolean isNestedClass() {
        return aNestedClass;
    }

    /**
     * An inner class is a non-static nested class.
     * @return true if this a non-static nested class.
     */
    public boolean isInnerClass() {
        return aNestedClass && !isStatic();
    }

    public boolean isAnonymousInnerClass() {
        InnerClass i = getInnerClassAttribute(getClassName());
        return i != null && i.getInnerNameIndex() == 0;
    }

    /**
     * Check if this is a local class (i.e a class defined within a method, not a member of the outer class).
     * @return true if this class is not a member of a class.
     */
    public boolean isLocalInnerClass() {
        InnerClass i = getInnerClassAttribute(getClassName());
        return i != null && i.getOuterClassIndex() == 0;
    }

    /**
     * Check if the given classRef is a nested class (not necessarily of this class!),
     * using the InnerClasses attribute of this class if no ClassInfo is available for the reference.
     *
     * @param classRef the class to check using the InnerClasses attribute of this class.
     * @return true if the referenced class is a nested class or if this class knows the class as nested class.
     */
    public boolean isNestedClass(ClassRef classRef) {
        ClassInfo classInfo = classRef.getClassInfo();
        if ( classInfo != null ) {
            return classInfo.isNestedClass();
        }
        InnerClass ic = getInnerClassAttribute(classRef.getClassName());
        return ic != null;
    }

    /**
     * Check if the given class is an enclosing class of this class.
     * This requires the class hierarchy to be up-to-date and all outer classes must be loaded.
     *
     * @param enclosing the potential outer class.
     * @param membersOnly only return true if this class and all enclosing classes up to enclosing are member classes.
     * @return true if this class is the same as or a nested class of the given class.
     */
    public boolean isNestedClassOf(ClassInfo enclosing, boolean membersOnly) {
        return isNestedClassOf(enclosing.getClassName(), membersOnly);
    }

    /**
     * Check if the given class is an outer class of this class.
     * This requires the class hierarchy to be up-to-date and all outer classes must be loaded.
     *
     * @param enclosingClassName the fully qualified name of the potential outer class.
     * @param membersOnly only return true if this class and all enclosing classes up to outer are member classes.
     * @return true if this class is the same as or a nested class of the given class.
     */
    public boolean isNestedClassOf(String enclosingClassName, boolean membersOnly) {
        if ( !aNestedClass) {
            return false;
        }

        // We could even handle some cases (when membersOnly is true) even without knowing the
        // enclosing ClassInfos using the InnerClasses attribute, but to make life easier we simply
        // require that all enclosing classes are loaded and the class hierarchy is up-to-date.

        ClassInfo outer = this;
        while (outer != null) {
            if (outer.getClassName().equals(enclosingClassName)) {
                return true;
            }
            outer = outer.getEnclosingClassInfo();
            if (membersOnly && outer.isLocalInnerClass()) {
                return false;
            }
        }
        return false;
    }

    /**
     * Get the name of the immediatly enclosing class of this class, if this is a nested class
     * (member or local), or null if this is not a nested class.
     *
     * @see #getOuterClassName()
     * @return the immediatly enclosing class of this class, or null if this is a top-level class.
     */
    public String getEnclosingClassName() {
        if ( !aNestedClass ) {
            return null;
        }
        if (enclosingClass != null) {
            return enclosingClass.getClassName();
        }

        String name = getOuterClassName();
        if ( name == null ) {
            MethodRef ref = getEnclosingMethodRef();
            if (ref != null) {
                return ref.getClassName();
            } else {
                throw new JavaClassFormatError("Could not find enclosing class name for nested class " +getClassName());
            }
        } else {
            return name;
        }
    }

    /**
     * Get the immediatly enclosing class of this class.
     *
     * @return the immediatly enclosing class of this class, or null if this is a top-level class.
     */
    public ClassInfo getEnclosingClassInfo() {
        return enclosingClass;
    }

    /**
     * Get a collection of all known nested classes of this class.
     * This may return a subset of {@link #getDirectInnerClassNames()} if not all
     * directly nested classes are loaded.
     *
     * @return a set of all known nested classes (member and local classes).
     */
    public Set<ClassInfo> getDirectNestedClasses() {
        return nestedClasses;
    }

    /**
     * Get the name of the outer class of this class if this is a nested member class.
     *
     * @see #getEnclosingClassName()
     * @return the name of the outer class as defined in the InnerClasses attribute if this is a member
     *      nested class, else null.
     */
    public String getOuterClassName() {
        if ( !aNestedClass ) {
            return null;
        }
        InnerClass i = getInnerClassAttribute(getClassName());
        if (i == null) {
            throw new JavaClassFormatError("Someone removed the InnerClasses attribute of this nested class!");
        }
        return getOuterClassName(i);
    }

    /**
     * Get a list of fully qualified classnames of all direct inner classes of this class, including local classes,
     * using the InnerClasses attribute.
     * 
     * @return a collection of fully qualified classnames of the inner classes or an empty collection if this class
     *   has no inner classes.
     */
    public Collection<String> getDirectInnerClassNames() {
        InnerClasses ic = getInnerClassesAttribute();
        if ( ic == null ) {
            return new LinkedList<String>();
        }
        List<String> inner = new LinkedList<String>();
        for (InnerClass i : ic.getInnerClasses()) {
            String outerName = getOuterClassName(i);

            if (getInnerClassName(i).equals(getClassName())) {
                continue;
            }
            // if outer is null, inner is a local inner class of this class.
            if (outerName == null || getClassName().equals(outerName)) {
                inner.add(getInnerClassName(i));
            }
        }
        return inner;
    }

    /**
     * Find the class enclosing this class which is the same as or a superclass or an interface of
     * the given class. This does not check if this class is a superclass of the given class.
     *
     * @param classInfo the (sub)class containing this class.
     * @param membersOnly if true, only check outer classes of member inner classes.
     * @return the found class or null if none found.
     */
    public ClassInfo getEnclosingSuperClassOf(ClassInfo classInfo, boolean membersOnly) {
        if ( membersOnly && isLocalInnerClass() ) {
            return null;
        }
        ClassInfo outer = enclosingClass;
        while (outer != null) {
            if (outer.isInstanceOf(classInfo)) {
                return outer;
            }
            if ( membersOnly && outer.isLocalInnerClass() ) {
                return null;
            } else {
                outer = outer.getEnclosingClassInfo();
            }
        }
        return null;
    }

    public MethodRef getEnclosingMethodRef() {
        for (Attribute a : getAttributes()) {
            if ( a instanceof EnclosingMethod ) {
                return ((EnclosingMethod)a).getMethodRef();
            }
        }
        return null;
    }

    /*
     * Make this class a member nested class of the given class or move it to top-level
     *
     * @param enclosingClass the new outer class of this class, or null to make it a toplevel class.
     * @param innerName the simple name of this member class.
     */
    /*
    public void setEnclosingClass(ClassInfo enclosingClass, String innerName) throws AppInfoException {
        // TODO implement setOuterClass(), if needed. But this is not a simple task.
        // need to
        // - remove references to old outerClass from InnerClasses of this and all old outer classes
        // - create InnerClasses attribute if none exists
        // - add new entries to InnerClasses of this and all new outer classes.
        // - update class hierarchy infos and fullyKnown flags
        // - handle setEnclosingClass(null)
    }
    */

    /*
     * Make this class a local nested class of the given method.
     * @param methodInfo the enclosing method of this class.
     */
    /*
    public void setEnclosingMethod(MethodInfo methodInfo) {
    }
    */

    public InnerClasses getInnerClassesAttribute() {
        // we could keep a reference to the attribute in the class, but this should be sufficiently fast.
        for (Attribute a : classGen.getAttributes()) {
            if ( a instanceof InnerClasses ) {
                return (InnerClasses) a;
            }
        }
        return null;
    }

    public InnerClass getInnerClassAttribute(String innerClassName) {
        InnerClasses ic = getInnerClassesAttribute();
        if ( ic != null ) {
            for (InnerClass i : ic.getInnerClasses()) {
                if (getInnerClassName(i).equals(innerClassName)) {
                    return i;
                }
            }
        }
        return null;
    }

    public String getInnerClassName(InnerClass i) {
        return ((ConstantClassInfo) getConstantInfo(i.getInnerClassIndex())).getClassName();
    }

    public String getOuterClassName(InnerClass i) {
        int index = i.getOuterClassIndex();
        if ( index == 0 ) {
            return null;
        }
        return ((ConstantClassInfo) getConstantInfo(index)).getClassName();
    }

    public String getInnerName(InnerClass i) {
        int index = i.getInnerNameIndex();
        if ( index == 0 ) {
            return null;
        }
        return ((ConstantUtf8)getConstant(index)).getBytes();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Class-hierarchy lookups and helpers
    //////////////////////////////////////////////////////////////////////////////

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
     * Check if all superclasses and outer classes of this class or interface are loaded.
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
     * This does not check the implemented/extended interfaces.
     *
     * @see #isExtensionOf(ClassInfo)
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
     * Check if this class is an extension of the given class, i.e. if this is a class,
     * check if the given class is a superclass, if this is an interface, check if the given
     * class is an interface and if this is an extension of the given interface.
     *
     * @see #isInstanceOf(ClassInfo) 
     * @param classInfo the class to check.
     * @return true if the class is an extension of this class.
     */
    public boolean isExtensionOf(ClassInfo classInfo) {
        // if this is not an interface, can only extend (super-)classes
        if ( !isInterface() ) {
            // can only be a superclass if it is not an interface
            return !classInfo.isInterface() && classInfo.isSuperclassOf(this);
        }
        // if classInfo is not a superclass, this can only be an extension if this and classInfo are interfaces
        if ( !classInfo.isInterface() ) {
            return false;
        }
        // could use visitor+ClassHierarchyTraverser here to speed things up a little
        Set<ClassInfo> interfaces = getAncestors();
        return interfaces.contains(classInfo);
    }

    /**
     * Check if this class is an implementation of the given class, i.e. if this class
     * is a class, the given class is an interface, and this class implements the given class.
     * @param classInfo the interface to check.
     * @return true if this class implements the interface.
     */
    public boolean isImplementationOf(ClassInfo classInfo) {
        // can only implement if this is a class and other class is an interface
        if (isInterface() || !classInfo.isInterface()) {
            return false;
        }
        // could use visitor+ClassHierarchyTraverser here to speed things up a little
        Set<ClassInfo> interfaces = getAncestors();
        return interfaces.contains(classInfo);
    }

    /**
     * Check if this class is either an extension or an implementation of the given class or
     * interface.
     *
     * <p>Note that this is slightly different from {@code isExtensionOf() || isImplementationOf()}, because
     * an interface is an instance of java.lang.Object, but neither implements or extends java.lang.Object.</p>
     *
     * @see #isExtensionOf(ClassInfo)
     * @param classInfo the super class to check.
     * @return true if this class is is a subtype of the given class.
     */
    public boolean isInstanceOf(ClassInfo classInfo) {
        // classes can only be superclasses
        if ( !classInfo.isInterface() ) {
            return classInfo.isSuperclassOf(this);
        }
        // classInfo is an interface ..
        if ( isInterface() ) {
            return classInfo.isExtensionOf(this);
        } else {
            return classInfo.isImplementationOf(this);
        }
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
     * Check if this class inherits the given member.
     * If member is a class, it is only inherited if it is a member inner class of a
     * superclass of this class.
     *
     * @param member the member to inherit.
     * @return true if this class inherits it.
     */
    public boolean inherits(MemberInfo member) {

        ClassInfo cls;
        if ( member instanceof ClassInfo ) {
            ClassInfo innerClass = (ClassInfo) member;
            // only member inner classes are inherited
            if ( !innerClass.isNestedClass() ) {
                return false;
            }
            cls = innerClass.getEnclosingSuperClassOf(this, true);
        } else {
            cls = member.getClassInfo();
        }
        if ( cls == null ) {
            return false;
        }

        return canAccess(cls, member.getAccessType());
    }

    /**
     * Check if the given member can be accessed by this class.
     *
     * @param member the member to access.
     * @return true if this class is allowed to access the member.
     */
    public boolean canAccess(MemberInfo member) {
        if ( member instanceof ClassInfo ) {
            return canAccess((ClassInfo) member);
        }
        return canAccess(member.getClassInfo(), member.getAccessType());
    }

    public boolean canAccess(ClassInfo classInfo) {

        if (classInfo.isNestedClass()) {
            // now this is where the fun begins ..
            
        }

        switch (classInfo.getAccessType()) {
            case ACC_PUBLIC: break;
            case ACC_PROTECTED:
                // TODO implement

                // fallthrough
            case ACC_PACKAGE:
                if ( !classInfo.hasSamePackage(this) ) {
                    return false;
                }
                break;
            case ACC_PRIVATE:
                // private class can only be accessed by an outer class
                if ( !classInfo.isNestedClassOf(this, true) ) {
                    return false;
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid accesstype "+classInfo.getAccessType()+" of class "+
                        classInfo.getClassName());
        }
        return false;
    }

    /**
     * Check if a member of another class with the given accessType can be accessed by this class.
     *
     * @param cls the class containing the member to check.
     * @param accessType the accessType of the member to check, as returned by {@link MemberInfo#getAccessType()}.
     * @return true if this class is allowed to access members of the given accessType of the given class.
     */
    public boolean canAccess(ClassInfo cls, int accessType) {
        // first, check if we can access the class itself
        if ( !canAccess(cls) ) {
            return false;
        }

        // now check if we can access the member
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
                return this.equals(cls) || cls.isNestedClassOf(this, true);
        }
        return false;
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
            if (m.getSimpleName().equals(name)) {
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

    /**
     * Find a method of this class or in the superclasses of this class.
     * If no such method is found, look in the interfaces too and return the first found method.
     * 
     * @param signature the signature of the method to find. The classname in the signature is ignored.
     * @param ignoreAccess if true, also return non-accessible or static methods in superclasses.
     * @return the MethodInfo with the given signature in this class or its extended classes, or null if not found.
     */
    public MethodInfo getMethodInfoInherited(Signature signature, boolean ignoreAccess) {
        ClassInfo cls = this;
        while ( cls != null ) {
            MethodInfo m = cls.getMethodInfo(signature);
            if ( m != null ) {
                if ( ignoreAccess || inherits(m) ) {
                    return m;
                } else {
                    // if we find a method but we do not override this method, no need to look in
                    // superclasses, but we may find something in the interfaces
                    break;
                }
            }
            cls = cls.getSuperClassInfo();
        }

        // not very nice, but works: get all ancestors, look in interfaces
        for (ClassInfo i : getAncestors()) {
            if ( !i.isInterface() ) {
                continue;
            }
            MethodInfo m = i.getMethodInfo(signature);
            if ( m != null ) {
                // we always inherit from interfaces
                return m;
            }
        }

        return null;
    }

    public FieldInfo getFieldInfoInherited(String name, boolean ignoreAccess) {
        ClassInfo cls = this;
        while ( cls != null ) {
            FieldInfo f = cls.getFieldInfo(name);
            if ( f != null ) {
                if ( ignoreAccess || inherits(f) ) {
                    return f;
                } else {
                    break;
                }
            }
            cls = cls.getSuperClassInfo();
        }

        // not very nice, but works: get all ancestors, look in interfaces
        for (ClassInfo i : getAncestors()) {
            if ( !i.isInterface() ) {
                continue;
            }
            FieldInfo f = i.getFieldInfo(name);
            if ( f != null ) {
                return f;
            }
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

        for (int i = 1; i < cpg.getSize(); i++) {

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

    /**
     * Get the name of the outer class of this class as it is stored in the InnerClasses attribute.
     * Note that this does return null for non-member inner classes.
     *
     * @param ic the InnerClasses attribute of this class.
     * @param innerClass the name of the inner class to lookup
     * @return the name of the outer name if it is stored in InnerClass.
     */
    private String getOuterClassName(InnerClasses ic, String innerClass) {
        for (InnerClass i : ic.getInnerClasses()) {
            if ( getInnerClassName(i).equals(innerClass) ) {
                return getOuterClassName(i);
            }
        }
        return null;
    }

    private void updateInnerClassFlag() {
        // check if this class appears as inner class (iff this is an inner class, it must
        // appear in the attribute by definition)
        aNestedClass = getInnerClassAttribute(getClassName()) != null;
    }

    protected void resetHierarchyInfos() {
        superClass = null;
        subClasses.clear();
        enclosingClass = null;
        nestedClasses.clear();
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

        for (ClassInfo i : getInterfaces()) {
            i.subClasses.add(this);
        }

        // set the outer class
        enclosingClass = appInfo.getClassInfo(getEnclosingClassName());
        if ( enclosingClass != null ) {
            enclosingClass.nestedClasses.add(this);
        } else if ( aNestedClass ) {
            throw new AppInfoError("Enclosing class "+getEnclosingClassName()+" of class "+getClassName()
                        +" is not loaded, but unknown outer classes are not supported.");
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

        // We require that all enclosing classes are at least loaded.
        // On the other hand, we do not need them to be completely known, therefore
        // the enclosing classes do not affect the fullyKnown flag and do not need to be updated.

        if ( updateSubclasses ) {
            // we need to recurse down here since the flag might depend on other interfaces- and
            // outer-classes too
            for (ClassInfo c : subClasses) {
                c.updateCompleteFlag(true);
            }
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void removeFromClassHierarchy() {
        if ( superClass != null ) {
            superClass.subClasses.remove(this);
        }

        if ( enclosingClass != null ) {
            enclosingClass.nestedClasses.remove(this);
        }

        // all extensions and inner classes of this class will now be incomplete
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
            traverser.setVisitImplementations(false);
            traverser.setVisitInnerClasses(true);
            traverser.traverse(this);
        }

        // interface references are not stored; direct subclasses of a class are only classes, so
        // all their superclasses must be this class, so unset them
        if ( !isInterface() ) {
            for (ClassInfo c : subClasses) {
                c.superClass = null;
            }
        }

        for (ClassInfo c : nestedClasses) {
            c.enclosingClass = null;
        }

        resetHierarchyInfos();
    }
}