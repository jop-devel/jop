/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct;

import com.jopdesign.libgraph.struct.type.MethodSignature;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ClassInfo implements ModifierInfo {

    private static final int STATUS_CREATED = 0;
    private static final int STATUS_INCOMPLETE = 1;
    private static final int STATUS_LOADED = 2;

    private int initialized;
    private AppStruct appStruct;

    private ClassInfo superClass;
    private Set subClasses;
    private Map methods;
    private Map fields;
    private Set interfaces;

    /**
     * a set of all classes which hold any sort of reference to his class.
     * Needed for some sort of 'reference-counting' when cleaning up unused classes.
     */
    private Set references;

    private static Logger logger = Logger.getLogger(ClassInfo.class);

    public ClassInfo(AppStruct appStruct) {
        this.appStruct = appStruct;

        initialized = STATUS_CREATED;
        subClasses = new HashSet();
        references = new HashSet();
        methods = new HashMap();
        fields = new HashMap();
    }

    public boolean isInitialized() {
        return initialized == STATUS_LOADED;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    public ClassInfo getSuperClass() {
        return superClass;
    }

    public Set getSubClasses() {
        return subClasses;
    }

    /**
     * return a list of classInfos which are interfaces this class implements.
     * @return a set of ClassInfo.
     */
    public Set getInterfaces() {
        return interfaces;
    }

    /**
     * Get the fully qualified name of this class.
     * @return the FQN of this class.
     */
    public abstract String getClassName();

    /**
     * Get the fully qualified name of the superclass.
     * @return the FQN of the superclass, or null if this is Object.
     */
    public abstract String getSuperClassName();

    public abstract boolean isInterface();

    public abstract ConstantPoolInfo getConstantPoolInfo();

    /**
     * write this class to a java class file.
     *
     * @param filename the filename of the new file.
     * @throws IOException if the file could not be stored.
     */
    public abstract void writeClassFile(String filename) throws IOException;

    /**
     * find a method by a full method name as created by {@link com.jopdesign.libgraph.struct.type.MethodSignature#createFullName(String,String)} )
     * @param method method name and signature.
     * @return the method if found or else null.
     */
    public MethodInfo getMethodInfo(String method) {
        return (MethodInfo) methods.get(method);
    }

    /**
     * Get a method by name and signature.
     * @param methodName the name of the method.
     * @param signature the signature of the method.
     * @return the method if found, else null.
     * @see #getMethodInfo(String)
     */
    public MethodInfo getMethodInfo(String methodName, String signature) {
        return getMethodInfo(MethodSignature.createFullName(methodName, signature));
    }

    /**
     * get a collection of all methodInfo object for this class.
     * Do not modifiy this collection.
     * @return a collection of MethodInfo objects.
     */
    public Collection getMethodInfos() {
        return methods.values();
    }

    public Collection getFieldInfos() {
        return Collections.unmodifiableCollection(fields.values());
    }
    
    /**
     * Get a method by resolving it in this class and all super-classes.
     * The classInfo if the returned method references to the class where the method is defined,
     * not necessarily this class (i.e. the method is found in a superclass and not overwritten in this
     * class).
     *
     * @param methodName the name of the method.
     * @param signature the signature of the method.
     * @return the method if found, else null.
     * @see #getMethodInfo(String)
     * @see #getInheritedMethodInfo(String, boolean, boolean)
     */
    public MethodInfo getVirtualMethodInfo(String methodName, String signature) {
        return getVirtualMethodInfo(MethodSignature.createFullName(methodName, signature));
    }

    /**
     * Get a method by resolving it in this class and all super-classes.
     * The classInfo if the returned method references to the class where the method is defined,
     * not necessarily this class (i.e. the method is found in a superclass and not overwritten in this
     * class).
     *
     * @param method the name of the method with its signature.
     * @return the method if found, else null.
     * @see #getMethodInfo(String)
     * @see #getInheritedMethodInfo(String, boolean, boolean)
     */
    public MethodInfo getVirtualMethodInfo(String method) {
        return getInheritedMethodInfo(method, false, true);
    }

    /**
     * search for method by methodname in this class and all superclasses.
     *
     * @param methodname full method name as created by {@link com.jopdesign.libgraph.struct.type.MethodSignature#createFullName(String,String)} ).
     * @param superOnly search only in superclasses, not in this class itself.
     * @param inheritedOnly search only for super-methods which can be overwritten (ie. not private/static/final..) 
     * @return the found method or null if not found.
     */
    public MethodInfo getInheritedMethodInfo(String methodname, boolean superOnly, boolean inheritedOnly) {

        MethodInfo method = superOnly ? null : getMethodInfo(methodname);

        // search in superclasses as well
        ClassInfo sup = superClass;
        while (method == null && sup != null) {
            method = sup.getMethodInfo(methodname);

            // check if this method can be overwritten.
            if (inheritedOnly && method != null) {
                if (method.isPrivate() || method.isStatic()) {
                    method = null;
                }
            }

            sup = sup.getSuperClass();
        }

        return method;
    }

    public FieldInfo getFieldInfo(String name) {
        return (FieldInfo) fields.get(name);
    }

    public FieldInfo getVirtualFieldInfo(String name) {
        return getInheritedFieldInfo(name, false, true);
    }

    public FieldInfo getInheritedFieldInfo(String name, boolean superOnly, boolean inheritedOnly) {

        FieldInfo field = superOnly ? null : getFieldInfo(name);

        ClassInfo sup = superClass;
        while ( field == null && sup != null ) {
            field = sup.getFieldInfo(name);

            if ( inheritedOnly && field != null ) {
                if ( field.isPrivate() || field.isStatic() ) {
                    field = null;
                }
            }

            sup = sup.getSuperClass();
        }

        return field;
    }

    /**
     * (re)load all class informations from javaClass.
     * @return true if references have changed.
     */
    public boolean reload() {
        boolean refChanged = false;

        boolean complete = true;

        // get super class (null if this is java.lang.Object)
        String superClassName = getSuperClassName();
        if ( superClassName != null ) {
            superClass = appStruct.getClassInfo(superClassName);
            if ( superClass != null ) {
                refChanged = superClass.addSubClass(this) || refChanged;
                refChanged = references.add(superClass) || refChanged;
            } else {
                complete = false;
                logger.warn("Could not find superclass {" + superClassName +
                        "} for class {" + getClassName() + "}.");
            }
        }

        interfaces = loadInterfaces();

        // Let this be a sub-class to all implemented interfaces
        for (Iterator it = interfaces.iterator(); it.hasNext();) {
            ClassInfo ifc = (ClassInfo) it.next();
            refChanged = ifc.addSubClass(this) || refChanged;
            refChanged = references.add(ifc) || refChanged;
        }

        loadFieldInfos();

        loadMethodInfos();

        initialized = complete ? STATUS_LOADED : STATUS_INCOMPLETE;

        return refChanged;
    }

    /**
     * add a subclass classinfo to this classinfo.
     * @param classInfo the classinfo of the subclass.
     * @return true if the references have changed.
     */
    public boolean addSubClass(ClassInfo classInfo) {
        subClasses.add(classInfo);

        // TODO redundant, but easier to implement..
        return references.add(classInfo);
    }

    public void addReference(ClassInfo classInfo) {
        references.add(classInfo);
    }

    public int getReferenceCount() {
        return references.size();
    }

    protected void addMethodInfo(MethodInfo method) {
        methods.put(MethodSignature.createFullName(method.getName(), method.getSignature()), method);
    }

    protected void addFieldInfo(FieldInfo field) {
        fields.put(field.getName(), field);
    }

    public int getAccessType() {
        if ( isPublic() ) {
            return ACC_PUBLIC;
        }
        if ( isPrivate() ) {
            return ACC_PRIVATE;
        }
        if ( isProtected() ) {
            return ACC_PROTECTED;
        }
        return ACC_PACKAGE;
    }

    public String getModifierString() {
        StringBuffer out = new StringBuffer();

        if ( isPrivate() ) {
            out.append("private ");
        }
        if ( isProtected() ) {
            out.append("protected ");
        }
        if ( isPublic() ) {
            out.append("public ");
        }
        if ( isSynchronized() ) {
            out.append("synchronized ");
        }
        if ( isStatic() ) {
            out.append("static ");
        }
        if ( isFinal() ) {
            out.append("final ");
        }
        if ( isInterface() ) {
            out.append("interface ");
        } else {
            out.append("class");
        }
        return out.toString();
    }

    protected abstract Set loadInterfaces();

    protected abstract void loadMethodInfos();

    protected abstract void loadFieldInfos();
}
