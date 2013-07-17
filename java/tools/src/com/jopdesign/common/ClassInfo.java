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

import com.jopdesign.common.graphutils.ClassHierarchyTraverser;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.common.tools.ConstantPoolReferenceFinder;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.SourceFile;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jopdesign.common.misc.MiscUtils.inArray;

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
    private InnerClassesInfo innerClasses;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".ClassInfo");

    public ClassInfo(ClassGen classGen) {
        super(classGen, new MemberID(classGen.getClassName()));
        this.classGen = classGen;
        cpg = classGen.getConstantPool();

        superClass = null;
        subClasses = new LinkedHashSet<ClassInfo>();
        fullyKnown = Ternary.UNKNOWN;

        innerClasses = new InnerClassesInfo(this, classGen);

        Method[] cgMethods = classGen.getMethods();
        Field[] cgFields = classGen.getFields();
        methods = new LinkedHashMap<String, MethodInfo>(cgMethods.length);
        fields = new LinkedHashMap<String, FieldInfo>(cgFields.length);

        // we create all FieldInfos and MethodInfos now and save a lot of trouble later
        for (Method m : cgMethods) {
            MethodInfo method = new MethodInfo(this, new MethodGen(m, classGen.getClassName(), cpg));
            methods.put(method.getMethodSignature(), method);
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
    public String getShortName() {
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

    /**
     * Check if the super flag is set. This defines the behaviour of invokespecial and should
     * be set in every class generated by a modern compiler.
     * @return true if the ACC_SUPER flag is set.
     */
    public boolean hasSuperFlag() {
        return (classGen.getAccessFlags() & Constants.ACC_SUPER) != 0;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Attributes access and lookups
    //////////////////////////////////////////////////////////////////////////////

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

    public String getSourceFileName() {
        for (Attribute a : getAttributes()) {
            if ( a instanceof SourceFile ) {
                return ((SourceFile)a).getSourceFileName();
            }
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Inner-class stuff; some delegates to InnerClassesInfo
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Get the InnerClassesInfo which contains infos about the inner classes, the enclosing classes
     * and the InnerClasses attribute.
     * @return the InnerClasses attribute manager (even if this class is not an inner class).
     */
    public InnerClassesInfo getInnerClassesInfo() {
        return innerClasses;
    }

    /**
     * @see InnerClassesInfo#isNestedClass()
     * @return true if this class is a nested class (a member- or inner class).
     */
    public boolean isNestedClass() {
        return innerClasses.isNestedClass();
    }

    /**
     * @see InnerClassesInfo#getEnclosingClassInfo()
     * @return the ClassInfo which encloses this class, if it is loaded and if this class is a nested class, else null.
     */
    public ClassInfo getEnclosingClassInfo() {
        return innerClasses.getEnclosingClassInfo();
    }

    /**
     * @see InnerClassesInfo#getEnclosingMethodRef()
     * @return the method reference to the method which encloses this class, or null if this is not a local class.
     */
    public MethodRef getEnclosingMethodRef() {
        return innerClasses.getEnclosingMethodRef();
    }

    /**
     * @see InnerClassesInfo#getDirectNestedClasses()
     * @return a set of all classes directly nested in this class.
     */
    public Set<ClassInfo> getDirectNestedClasses() {
        return innerClasses.getDirectNestedClasses();
    }

    /**
     * @see InnerClassesInfo#getTopLevelClass()
     * @return the top level class enclosing this class or this class if this is a top level class.
     */
    public ClassInfo getTopLevelClass() {
        return innerClasses.getTopLevelClass();
    }

    /**
     * Check if this class is defined within a method.
     * @see InnerClassesInfo#isLocalInnerClass()
     * @return true if this class is not a member of the class.
     */
    public boolean isLocalInnerClass() {
        return innerClasses.isLocalInnerClass();
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


    //////////////////////////////////////////////////////////////////////////////
    // Superclass and Interfaces
    //////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if this class represents java.lang.Object
     */
    public boolean isRootClass() {
        return "java.lang.Object".equals(getClassName());
    }

    public String getSuperClassName() {
        return classGen.getSuperclassName();
    }

    /**
     * Get the ClassInfo of the superClass if it is known and if this is not java.lang.Object, else return null.
     * @return the superclass ClassInfo or null if not loaded or if this class is java.lang.Object.
     */
    public ClassInfo getSuperClassInfo() {
        return superClass;
    }

    /**
     * check if a given class is a superclass of this class. This check does not require the given
     * superclass to exist in AppInfo. If the given classname is the name of this class, this method
     * returns false.
     *
     * @param className the name of the superclass.
     * @param checkInterfaces if true, check the interfaces of this class too.
     * @return true if a superclass by this name is found, false if all superclasses have been checked
     *         and no superclass has been found by that name, or unknown if the full superclass hierarchy is
     *         not known.
     */
    public Ternary hasSuperClass(String className, boolean checkInterfaces) {
        // some simple special cases, to get them out of the way..
        if (getClassName().equals(className)) return Ternary.FALSE;
        if (this.isRootClass()) return Ternary.FALSE;

        LinkedList<ClassInfo> list = new LinkedList<ClassInfo>();
        list.add(this);
        boolean unsafe = false;
        while (!list.isEmpty()) {
            ClassInfo cls = list.removeFirst();

            // check if we find a superclass name match
            if (cls.getSuperClassName().equals(className)) return Ternary.TRUE;
            if (checkInterfaces) {
                if ( inArray(cls.getInterfaceNames(), className) ) return Ternary.TRUE;
            }

            // push all superclasses to the queue, if they exist
            // no need to visit Object, if it did not match, we will not find a matching superclass of Object..
            if (!"java.lang.Object".equals(cls.getSuperClassName())) {
                ClassInfo superClass = cls.getSuperClassInfo();
                // cls never is java.lang.Object, since we checked this at the beginning, so
                // if superclass is null (and not Object) we might have an unsafe result
                if (superClass == null) {
                    unsafe = true;
                } else {
                    list.add(superClass);
                }
            }
            if (checkInterfaces) {
                Set<ClassInfo> ifs = cls.getInterfaces();
                if (ifs.size() != cls.getInterfaceNames().length) {
                    unsafe = true;
                }
                list.addAll(ifs);
            }
        }
        return unsafe ? Ternary.UNKNOWN : Ternary.FALSE;
    }

    public boolean isInterface() {
        return classGen.isInterface();
    }

    /**
     * Set interface flag of this class.
     *
     * <p>This does not check if this is a valid operation, i.e. you need to check
     * for yourself if all methods are abstract and public and this class is not a superclass of a
     * normal class if you make this class an interface, ... This also does not update the
     * class hierarchy, you should call {@link AppInfo#reloadClassHierarchy()} later.</p>
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
        Set<ClassInfo> interfaces = new LinkedHashSet<ClassInfo>(names.length);
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
        Set<ClassInfo> sc = new LinkedHashSet<ClassInfo>();
        List<ClassInfo> queue = new LinkedList<ClassInfo>();

        sc.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            ClassInfo cls = queue.remove(0);

            ClassInfo superClass = cls.getSuperClassInfo();
            if ( superClass != null && sc.add(superClass) ) {
                queue.add(superClass);
            }
            for (ClassInfo i : cls.getInterfaces()) {
                if ( sc.add(i) ) {
                    queue.add(i);
                }
            }
        }

        return sc;
    }

    /**
     * Check if the given class is the same as this class or a subclass of this class.
     * This does not check the implemented interfaces. For interfaces this will always return
     * false, even if the given class implements the given interface.
     *
     * @see #isSubclassOf(ClassInfo)
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
     * @see #isSubclassOf(ClassInfo)
     * @see #isImplementationOf(ClassInfo)
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
     * an interface is an instance of java.lang.Object, but it neither implements or extends java.lang.Object.</p>
     *
     * @see #isExtensionOf(ClassInfo)
     * @param classInfo the super class to check.
     * @return true if this class is is a subtype of the given class.
     */
    public boolean isSubclassOf(ClassInfo classInfo) {
        // classes can only be superclasses
        if ( !classInfo.isInterface() ) {
            return classInfo.isSuperclassOf(this);
        }
        // classInfo is an interface ..
        if ( isInterface() ) {
            return this.isExtensionOf(classInfo);
        } else {
            return this.isImplementationOf(classInfo);
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
     * Check if this class inherits the given nested class.
     * @param classInfo the nested class to check.
     * @return true if the class is inherited by this class.
     */
    public boolean inherits(ClassInfo classInfo) {
        ClassInfo superClass = classInfo.getInnerClassesInfo().getEnclosingSuperClassOf(this, true);
        if (superClass == null) {
            return false;
        }
        // canAccess checks if all enclosing classes can be accessed too
        return canAccess(classInfo);
    }

    /**
     * Check if this class inherits the given class member.
     *
     * @param member the member to inherit.
     * @param checkInstanceOf if true, check if the member is defined in a superclass or interface of this class,
     *        else assume that this has already been checked.
     * @return true if this class inherits it.
     */
    public boolean inherits(ClassMemberInfo member, boolean checkInstanceOf) {
        ClassInfo cls = member.getClassInfo();
        if ( checkInstanceOf && !isSubclassOf(cls) ) {
            return false;
        }
        return canAccess(cls, member.getAccessType());
    }


    //////////////////////////////////////////////////////////////////////////////
    // Access to fields and methods, lookups
    //////////////////////////////////////////////////////////////////////////////

    public ClassMemberInfo getMemberInfo(MemberID memberID) {
        return getMemberInfo(memberID.hasMethodSignature() ? memberID.getMethodSignature() : memberID.getMemberName());
    }

    /**
     * @param memberSignature either a field name or a method signature (short name and descriptor).
     * @return the member info for this signature or null if not found.
     */
    public ClassMemberInfo getMemberInfo(String memberSignature) {
        MethodInfo method = methods.get(memberSignature);
        if (method != null) {
            return method;
        }
        return fields.get(memberSignature);
    }

    public FieldInfo getFieldInfo(MemberID memberID) {
        return getFieldInfo(memberID.getMemberName());
    }

    public FieldInfo getFieldInfo(String name) {
        return fields.get(name);
    }

    public MethodInfo getMethodInfo(MemberID memberID) {
        if (memberID.hasMethodSignature()) {
            return getMethodInfo(memberID.getMethodSignature());
        }
        Set<MethodInfo> methods = getMethodByName(memberID.getMemberName());
        if (methods.size() == 1) {
            return methods.iterator().next();
        }
        // not found or not unique
        return null;
    }

    public Set<MethodInfo> getMethodInfos(MemberID memberID) {
        if (memberID.hasMethodSignature()) {
            MethodInfo method = getMethodInfo(memberID.getMethodSignature());
            return method != null ? Collections.singleton(method) : Collections.<MethodInfo>emptySet();
        }
        return getMethodByName(memberID.getMemberName());
    }

    /**
     * Get the method with the given member signature (e.g. {@code "foo(I)V"}).
     *
     * @param methodSignature the signature of the method without the classname.
     * @return the method or null if it does not exist.
     */
    public MethodInfo getMethodInfo(String methodSignature) {
        return methods.get(methodSignature);
    }

    public Set<MethodInfo> getMethodByName(String name) {
        Set<MethodInfo> mList = new LinkedHashSet<MethodInfo>();
        for (MethodInfo m : methods.values()) {
            if (m.getShortName().equals(name)) {
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

    public Collection<String> getMethodSignatures() {
        return Collections.unmodifiableCollection(methods.keySet());
    }

    /**
     * Find a method of this class or in the superclasses of this class.
     * If no such method is found, look in the interfaces too and return the first found method.
     * This method therefore always returns an inherited non-abstract method if it exists, even if the method
     * is also defined in an implemented interface.
     * 
     * @param memberID the memberID of the method to find. The classname in the memberID is ignored.
     * @param checkAccess if false, also return non-accessible or static methods in superclasses.
     * @return the MethodInfo with the given memberID in this class or its extended classes, or null if not found.
     */
    public MethodInfo getMethodInfoInherited(MemberID memberID, boolean checkAccess) {
        return getMethodInfoInherited(memberID.getMethodSignature(), checkAccess);
    }

    /**
     * Find a method of this class or in the superclasses of this class.
     * If no such method is found, look in the interfaces too and return the first found method.
     * This method therefore always returns an inherited non-abstract method if it exists, even if the method
     * is also defined in an implemented interface.
     *
     * @param methodSignature the signature of the method to find.
     * @param checkAccess if false, also return non-accessible or static methods in superclasses.
     * @return the MethodInfo with the given signature in this class or its extended classes, or null if not found.
     */
    public MethodInfo getMethodInfoInherited(String methodSignature, boolean checkAccess) {
        // first, lets look at all superclasses, so that we find the implementation first if the method
        // is also defined in an interface
        ClassInfo cls = this;
        while ( cls != null ) {
            MethodInfo m = cls.getMethodInfo(methodSignature);
            if ( m != null ) {
                if ( !checkAccess || inherits(m, false) ) {
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
            if (!i.isInterface()) continue;

            MethodInfo m = i.getMethodInfo(methodSignature);
            if ( m != null ) {
                // we always inherit from interfaces, no need to check
                return m;
            }
        }

        return null;
    }

    public FieldInfo getFieldInfoInherited(String name, boolean checkAccess) {
        ClassInfo cls = this;
        while ( cls != null ) {
            FieldInfo f = cls.getFieldInfo(name);
            if ( f != null ) {
                if ( !checkAccess || inherits(f,false) ) {
                    return f;
                } else {
                    break;
                }
            }
            cls = cls.getSuperClassInfo();
        }

        // not very nice, but works: get all ancestors, look in interfaces
        for (ClassInfo i : getAncestors()) {
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
            String s = MemberID.getMethodSignature(m.getName(), m.getSignature());
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
     * Create a new non-static, abstract, package-visible method with the given name and descriptor.
     *
     * @param memberID the membername and descriptor of the method (classname is ignored).
     * @param argNames the names of the parameters
     * @return the new method or an existing method with that memberID.
     */
    public MethodInfo createMethod(MemberID memberID, String[] argNames) {
        return createMethod(memberID, argNames, null);
    }

    /**
     * Create a new non-static, package-visible method with the given name and descriptor.
     *
     * @param memberID the membername and descriptor of the method (classname is ignored).
     * @param argNames the names of the parameters
     * @param code an InstructionList to set to the method as code, or if null, create an abstract method.
     * @return the new method or an existing method with that memberID.
     */
    public MethodInfo createMethod(MemberID memberID, String[] argNames, InstructionList code) {
        MethodInfo method = methods.get(memberID.getMethodSignature());
        if ( method != null ) {
            method.setAbstract(code == null);
            if (code != null) {
                method.getCode().setInstructionList(code);
            }
            return method;
        }

        Descriptor desc = memberID.getDescriptor();
        int flags = (code == null) ? Constants.ACC_ABSTRACT : 0;

        method = new MethodInfo(this, new MethodGen(flags, desc.getType(), desc.getArgumentTypes(), argNames,
                memberID.getMemberName(), classGen.getClassName(), code, cpg));

        methods.put(memberID.getMethodSignature(), method);
        classGen.addMethod(method.getMethod(false));

        // TODO call manager eventhandler

        return method;
    }

    public MethodInfo copyMethod(String memberSignature, String newName) {
        MethodInfo method = methods.get(memberSignature);
        if ( method == null ) {
            return null;
        }
        MethodGen methodGen = new MethodGen(method.compile(), getClassName(), cpg);
        methodGen.setName(newName);

        MethodInfo newMethod = new MethodInfo(this, methodGen);

        // TODO copy all the attribute stuff, call manager eventhandler

        methods.put(newMethod.getMethodSignature(), newMethod);
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

        // TODO copy all the attribute stuff, call manager eventhandler

        fields.put(newName, newField);
        classGen.addField(newField.getField());

        return newField;
    }

    public MethodInfo renameMethod(String memberSignature, String newName) {
        MethodInfo method = methods.remove(memberSignature);
        if ( method == null ) {
            return null;
        }
        method.getInternalMethodGen().setName(newName);

        methods.put(method.getMethodSignature(), method);
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
        field.getInternalFieldGen().setName(newName);

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

        for (AppEventHandler e : getAppInfo().getEventHandlers()) {
            e.onRemoveField(fieldInfo);
        }

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

        for (AppEventHandler e : getAppInfo().getEventHandlers()) {
            e.onRemoveMethod(methodInfo);
        }

        return methodInfo;
    }


    //////////////////////////////////////////////////////////////////////////////
    // BCEL stuff
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Rebuild the constantpool from a new, empty constantpool.
     *
     * <p>This updates the indices of all references in the code of all methods of this class,
     * therefore do not call this method while modifying the code.</p>
     * <p>
     * Note that the ConstantPoolRebuilder implements ClassVisitor, so you can also use
     * {@link AppInfo#iterate(ClassVisitor)} to apply it to all classes.
     * </p>
     *
     * @param rebuilder the builder to use to rebuild the pool.
     */
    public void rebuildConstantPool(ConstantPoolRebuilder rebuilder) {

        // this will compile the classInfo
        Set<Integer> usedIndices = ConstantPoolReferenceFinder.findPoolReferences(this, true);
        cpg = rebuilder.createNewConstantPool(cpg, usedIndices);

        rebuilder.updateClassGen(this, classGen);

        for (MethodInfo m : methods.values()) {
            rebuilder.updateMethodGen(m, m.getInternalMethodGen());
        }
        for (FieldInfo f : fields.values()) {
            rebuilder.updateFieldGen(f, f.getInternalFieldGen());
        }
    }

    /**
     * Rebuild the InnerClasses attribute of this class.
     * Add or update all references to nested classes found in this class in the InnerClasses attribute.
     */
    public void rebuildInnerClasses() {

        InnerClasses oldIC = innerClasses.getInnerClassesAttribute();
        InnerClasses newIC = innerClasses.buildInnerClassesAttribute();

        if (oldIC != null) classGen.removeAttribute(oldIC);
        if (newIC != null) classGen.addAttribute(newIC);
    }

    /**
     * Commit all modifications to this ClassInfo and return a BCEL JavaClass for this ClassInfo.
     * <p>
     * You may want to call {@link #rebuildConstantPool(ConstantPoolRebuilder)} and {@link #rebuildInnerClasses()} first if needed.
     * </p>
     * @see MethodInfo#compile()
     * @see #rebuildInnerClasses()
     * @see #rebuildConstantPool(ConstantPoolRebuilder) 
     * @see #getJavaClass()
     * @return a JavaClass representing this ClassInfo.
     */
    public JavaClass compile() {

        // We could keep a modified flag in both MethodInfo and FieldInfo
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
            MethodInfo method = methods.get(MemberID.getMethodSignature(mList[i].getName(),
                                                                         mList[i].getSignature()));
            classGen.setMethodAt(method.compile(), i);
        }

        // TODO call manager eventhandler

        return classGen.getJavaClass();
    }

    /**
     * Create and return a BCEL JavaClass for this ClassInfo.
     *
     * <p>The returned JavaClass does not contain any modifications not yet
     * commited to the internal BCEL ClassGen (e.g. it does not contain
     * modifications to methods/fields/code).</p>
     *
     * @see #compile()
     * @return a JavaClass for this ClassInfo.
     */
    public JavaClass getJavaClass() {
        return classGen.getJavaClass();
    }


    //////////////////////////////////////////////////////////////////////////////
    // hashCode, equals
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return classGen.getClassName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ClassInfo && ((ClassInfo) o).getClassName().equals(getClassName());
    }


    //////////////////////////////////////////////////////////////////////////////
    // Internal affairs, class hierarchy management; To be used only be AppInfo
    //////////////////////////////////////////////////////////////////////////////

    protected void resetHierarchyInfos() {
        superClass = null;
        subClasses.clear();
        fullyKnown = Ternary.UNKNOWN;
        innerClasses.resetHierarchyInfos();
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void updateClassHierarchy() {
        AppInfo appInfo = AppInfo.getSingleton();

        if ( isRootClass() ) {
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
        innerClasses.updateClassHierarchy();
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
            fullyKnown = Ternary.valueOf(isRootClass());
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


        // direct subclasses of an interface can be other interfaces, which have java.lang.Object as superclass,
        // or implementing classes, which have a class as superclass, so no need to update them if this is an interface
        if ( !isInterface() ) {
            // direct subclasses of a class are only classes, so
            // all their superclasses must be this class, so unset them
            for (ClassInfo c : subClasses) {
                c.superClass = null;
            }
        }

        innerClasses.removeFromClassHierarchy();
    }

    protected void finishRemoveFromHierarchy() {

        // all extensions and inner classes of this class will now be incomplete
        if ( fullyKnown == Ternary.TRUE ) {
            ClassVisitor visitor = new ClassVisitor() {

                @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
                public boolean visitClass(ClassInfo classInfo) {
                    classInfo.fullyKnown = Ternary.FALSE;
                    return true;
                }

                public void finishClass(ClassInfo classInfo) {
                }
            };
            ClassHierarchyTraverser traverser = new ClassHierarchyTraverser(visitor);
            traverser.setVisitSubclasses(true, false);
            // we do not support nested classes without enclosing class, so no need to visit them
            // as they are removed too
            traverser.setVisitInnerClasses(false);
            traverser.traverseDown(this);
        }
    }
}