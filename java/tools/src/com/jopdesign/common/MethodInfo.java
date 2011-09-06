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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.graphutils.ClassHierarchyTraverser;
import com.jopdesign.common.graphutils.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.misc.Ternary;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
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
    private MethodCode methodCode;

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT+".MethodInfo");

    public MethodInfo(ClassInfo classInfo, MethodGen methodGen) {
        super(classInfo,
              new MemberID(classInfo.getClassName(), methodGen.getName(), methodGen.getSignature()),
              methodGen);
        this.methodGen = methodGen;
        updateMethodCode();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Lots of wrapper stuff for MethodGen.
    // You do not get direct access to my private methodGen. Nix.
    //////////////////////////////////////////////////////////////////////////////

    public boolean isAbstract() {
        return methodGen.isAbstract();
    }

    public void setAbstract(boolean val) {
        methodGen.isAbstract(val);
        updateMethodCode();
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
        updateMethodCode();
    }

    public boolean isStrictFP() {
        return methodGen.isStrictfp();
    }

    public void setStrictFP(boolean val) {
        methodGen.isStrictfp(val);
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

    public Type[] getArgumentTypes() {
        return methodGen.getArgumentTypes();
    }

    public Type getArgumentType(int i) {
        return methodGen.getArgumentType(i);
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

    //////////////////////////////////////////////////////////////////////////////
    // Code access and Control Flow Graph stuff
    //////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if this method is neither abstract nor native.
     */
    public boolean hasCode() {
        return methodCode != null;
    }

    public MethodCode getCode() {
        return methodCode;
    }

    /**
     * Compile the code and return a new BCEL method.
     * <p>
     * This function should also clean up internal resources of MethodInfo and MethodCode.
     * </p>
     * @see MethodCode#compile()
     * @return a new BCEL method class containing all changes to the code.
     */
    public Method compile() {
        if (hasCode()) {
            methodCode.compile();
        }
        // TODO: to reduce memory consumption, dispose instruction handlers and compile to Method instead of MethodGen?
        return methodGen.getMethod();
    }

    /**
     * Get a BCEL method for this methodInfo.
     *
     * @param compile if true, this does the same as {@link #compile()}.
     * @return a method for this methodInfo.
     */
    public Method getMethod(boolean compile) {
        if ( compile ) {
            // we use the compile flag primarily as a reminder to the API user to compile first
            return compile();
        }
        return methodGen.getMethod();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Interface implementations, name and signature
    //////////////////////////////////////////////////////////////////////////////

    /**
     * This is the same as {@link #getMemberID()}.toString().
     * @return classname and method signature of this method.
     */
    public String getFQMethodName() {
        return getClassInfo().getClassName() + "." + getMethodSignature();
    }

    public MethodRef getMethodRef() {
        return new MethodRef(this);
    }

    /**
     * Get the signature of this method (i.e. its simple name and the descriptor).
     * @return the signature of this method without the class part.
     */
    public String getMethodSignature() {
        return methodGen.getName() + methodGen.getSignature();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Helper methods to find implementations and super methods
    //////////////////////////////////////////////////////////////////////////////



    /**
     * Check if this method is the same as or overrides a given method.
     * 
     * @param superMethod the superMethod to check.
     * @param checkSignature if true, check if the given method has the same signature and is defined in a subclass
     *  of this method's class.
     * @return true if this method overrides the given method and can access the method.
     */
    public boolean overrides(MethodInfo superMethod, boolean checkSignature) {
        return overrides(superMethod.getMethodRef(), checkSignature);
    }

    /**
     * @param interfaceMethod A method within an interface.
     * @return true if this method implements the interface method, even if the class does not implement the interface.
     */
    public boolean implementsMethod(MethodRef interfaceMethod) {
        if (interfaceMethod.isInterfaceMethod() != Ternary.TRUE) return false;
        if (!getMethodSignature().equals(interfaceMethod.getMethodSignature())) {
            return false;
        }
        // no need for access checks, interfaces are always public.
        return true;
    }

    /**
     * Check if this method is the same as or overrides a given method.
     * <p>
     * This checks the class of the reference if checkSignature is true, so even if the reference resolves
     * to this method, this returns false if the reference refers to a subclass of this method's class.
     * </p>
     * <p>This might not work as expected for interface methods. To check if this method implements
     * an interface method even if the class of this method does not implement the interface, use
     * {@link #implementsMethod(MethodRef)} instead.</p>
     *
     * @param superMethod the superMethod to check, must refer to a known class.
     * @param checkSignature if true, check if the given method has the same signature and if the reference refers to
     *        a superclass of this method's class. If this is false, it is assumed that the signatures match and this
     *        method's class is a subclass of the referred class.
     * @return true if this method overrides the given method and can access the method.
     */
    public boolean overrides(MethodRef superMethod, boolean checkSignature) {

        ClassInfo superClass = superMethod.getClassInfo();
        if (superClass == null) {
            // No need to check if the classname is equal to this method's class, in this case we would have a ClassInfo
            throw new AppInfoError("Trying to lookup unknown class for " + superMethod+", not supported.");
        }

        if (superClass.equals(getClassInfo())) {
            // refers to same class.. Must be the same method if the signature matches
            if ( checkSignature && !getMethodSignature().equals(superMethod.getMethodSignature()) ) {
                return false;
            }
            return true;
        }

        // A static method may hide a static or instance method, but does not override it.
        if ( isStatic() ) {
            return false;
        }

        MethodInfo sm = superMethod.getMethodInfo();

        // special case: check if this method is the method which is inherited to the referenced class
        if ( this.equals(sm) ) {
            return true;
        }

        if (checkSignature) {
            if ( !getMethodSignature().equals(superMethod.getMethodSignature()) ) {
                return false;
            }
            if ( !getClassInfo().isSubclassOf(superClass) ) {
                return false;
            }
        }

        if (sm == null) {
            throw new AppInfoError("Trying to check unknown method "+superMethod+", this is not supported.");
        }

        if ( sm.isStatic() ) {
            logger.warn("Instance method " + getMemberID()+" overrides static method "+sm.getMemberID());
        }
        
        return getClassInfo().canAccess(sm);
    }

    /**
     * Get the super method for this method, if there is any.
     *
     * @param nonAbstractOnly if true, return the method from the lowest superclass
     * @param checkAccess if false, also return hidden methods and methods which cannot be accessed by this method's class.
     * @return the super method or null if none found.
     */
    public MethodInfo getSuperMethod(boolean nonAbstractOnly, boolean checkAccess) {
        
        if ( checkAccess && (isPrivate() || isStatic()) ) {
            return null;
        }

        ClassInfo superClass = getClassInfo().getSuperClassInfo();
        if ( superClass != null ) {
            MethodInfo inherited = superClass.getMethodInfoInherited(getMemberID(), checkAccess);
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
                MethodInfo ifMethod = classInfo.getMethodInfo(getMethodSignature());
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
                MethodInfo overrider = classInfo.getMethodInfo(getMethodSignature());
                if ( overrider != null ) {
                    if ( overrider.isPrivate() ) {
                        // found an overriding method which is private .. this is interesting..
                        logger.error("Found private method "+overrider.getMethodSignature()+" in "+
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
     * @see AppInfo#findImplementations(CallString)
     * @see AppInfo#findImplementations(MethodRef)
     * @param checkAccess if false, find all non-abstract methods with same signature even if they do not
     *        override this method.
     * @return a collection of all implementations of this method.
     */
    public List<MethodInfo> getImplementations(final boolean checkAccess) {
        final List<MethodInfo> implementations = new LinkedList<MethodInfo>();

        if (checkAccess && (isPrivate() || isStatic())) {
            if (isAbstract()) {
                throw new JavaClassFormatError("Method is private or static but abstract!: "+toString());
            }
            implementations.add(this);
            return implementations;
        }

        if ("<init>".equals(getShortName())) {
            if (isAbstract()) {
                throw new JavaClassFormatError("Found abstract constructor, this isn't right..: "+toString());
            }
            implementations.add(this);
            return implementations;
        }

        ClassVisitor visitor = new ClassVisitor() {

            public boolean visitClass(ClassInfo classInfo) {
                MethodInfo m = classInfo.getMethodInfo(getMethodSignature());
                if ( m != null ) {
                    if ( m.isPrivate() && !isPrivate() ) {
                        // found an overriding method which is private .. this is interesting..
                        logger.error("Found private method "+m.getMethodSignature()+" in "+
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

    //////////////////////////////////////////////////////////////////////////////
    // Internal stuff
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Should only be used by ClassInfo!
     *
     * @return the internal methodGen.                           
     */
    protected MethodGen getInternalMethodGen() {
        return methodGen;
    }

    private void updateMethodCode() {
        if (!isAbstract() && !isNative()) {
            if (methodCode == null) {
                if (methodGen.getInstructionList() == null) {
                    methodGen.setInstructionList(new InstructionList());
                }
                methodCode = new MethodCode(this);
            }
        } else {
            if (methodCode != null) {
                methodGen.setInstructionList(null);
                methodGen.removeCodeAttributes();
                methodGen.removeLineNumbers();
                methodGen.removeExceptionHandlers();
                methodGen.removeLocalVariables();
                methodCode = null;
            }
        }
    }

}
