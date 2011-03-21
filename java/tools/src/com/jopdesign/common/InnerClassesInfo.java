/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.tools.ConstantPoolReferenceFinder;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.ConstantClassInfo;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InnerClassesInfo {

    private final ClassInfo classInfo;
    private final ClassGen classGen;

    private ClassInfo enclosingClass;
    private final Set<ClassInfo> nestedClasses;
    private boolean aNestedClass;

    public InnerClassesInfo(ClassInfo classInfo, ClassGen classGen) {
        this.classInfo = classInfo;
        this.classGen = classGen;

        aNestedClass = false;
        enclosingClass = null;
        nestedClasses = new HashSet<ClassInfo>();
        updateInnerClassFlag();
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public String getClassName() {
        return classInfo.getClassName();
    }

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
        return findInnerClass(ic, innerClassName);
    }

    public String getInnerClassName(InnerClass i) {
        return ((ConstantClassInfo) classInfo.getConstantInfo(i.getInnerClassIndex())).getClassName();
    }

    public String getOuterClassName(InnerClass i) {
        int index = i.getOuterClassIndex();
        if ( index == 0 ) {
            return null;
        }
        return ((ConstantClassInfo) classInfo.getConstantInfo(index)).getClassName();
    }

    /**
     * Get the simple name of a class if it is a non-anonymous class.
     * @param i the attribute corresponding to the nested class.
     * @return the member name of the nested class or null if anonymous.
     */
    public String getInnerName(InnerClass i) {
        int index = i.getInnerNameIndex();
        if ( index == 0 ) {
            return null;
        }
        return ((ConstantUtf8)classInfo.getConstant(index)).getBytes();
    }

    /**
     * Get the simple inner name of this class if this is an non-anonymous inner class.
     * @return return the simple inner name of this class or null if this is an anonymous class or not a nested class.
     */
    public String getInnerName() {
        InnerClass attribute = getInnerClassAttribute(classInfo.getClassName());
        if ( attribute == null ) {
            return null;
        }
        return getInnerName(attribute);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Inner-class access and analyze stuff
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Check if this class is a nested class.
     * @return true if this class is a nested class (member or inner class, anonymous or not).
     */
    public boolean isNestedClass() {
        return aNestedClass;
    }

    /**
     * An inner class is a non-static nested class.
     * @return true if this a non-static nested class.
     */
    public boolean isInnerClass() {
        return aNestedClass && !classInfo.isStatic();
    }

    /**
     * Check if this class is an anonymous inner class (i.e. a class defined within a method without a class name).
     * @return true if this class has no name.
     */
    public boolean isAnonymousInnerClass() {
        if (!aNestedClass) return false;
        InnerClass i = getInnerClassAttribute(getClassName());
        return i != null && i.getInnerNameIndex() == 0;
    }

    /**
     * Check if this is a local class (i.e a class defined within a method, not a member of the outer class).
     * @return true if this class is not a member of a class.
     */
    public boolean isLocalInnerClass() {
        if (!aNestedClass) return false;
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

        ClassInfo outer = classInfo;
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
            EnclosingMethod m = getEnclosingMethod();
            if (m != null) {
                return m.getClassName();
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
     * Get the top level enclosing class, or this class itself if this class is a top level class.
     *
     * @return the top level class for this class.
     */
    public ClassInfo getTopLevelClass() {
        ClassInfo top = classInfo;
        while (top.isNestedClass()) {
            top = top.getEnclosingClassInfo();
        }
        return top;
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
     * the given class. If the given class is a subclass of this class, this returns null.
     *
     * @param classInfo the (sub)class containing this class.
     * @param membersOnly if true, only check outer classes of member inner classes.
     * @return the found enclosing class or null if none found.
     */
    public ClassInfo getEnclosingSuperClassOf(ClassInfo classInfo, boolean membersOnly) {
        if ( membersOnly && isLocalInnerClass() ) {
            return null;
        }
        ClassInfo outer = enclosingClass;
        while (outer != null) {
            if (classInfo.isSubclassOf(outer)) {
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

    public EnclosingMethod getEnclosingMethod() {
        for (Attribute a : classInfo.getAttributes()) {
            if ( a instanceof EnclosingMethod) {
                return ((EnclosingMethod)a);
            }
        }
        return null;
    }

    public MethodRef getEnclosingMethodRef() {
        EnclosingMethod m = getEnclosingMethod();
        return m == null ? null : m.getMethodRef();
    }

    /*
     * Make this class a member nested class of the given class or move it to top-level
     *
     * @param enclosingClass the new outer class of this class, or null to make it a toplevel class.
     * @param innerName the simple name of this member class.
     */
    /*
    public void setEnclosingClass(ClassInfo enclosingClass, String innerName) throws AppInfoException {
        // implement setOuterClass(), if needed. But this is not a simple task.
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

    /*
     * Add or replace InnerClass entries of this class with the info of the given classes.
     * If no InnerClasses attribute exists, it is created. Enclosing classes are added if needed.
     *
     * @param nestedClasses a list of nested classes to add to the InnerClasses attribute.
     */
    /*
    public void addInnerClassRefs(Collection<ClassRef> nestedClasses) {
        if ( nestedClasses == null || nestedClasses.isEmpty() ) {
            return;
        }

        Map<String, InnerClass> classes = buildInnerClasses(nestedClasses);

        // create an InnerClasses attribute if it does not exist
        // add all InnerClasses to the existing InnerClasses (replace entries if existing)
    }
    */

    /**
     * Build a new InnerClasses Attribute containing entries for all inner classes referenced by the current
     * constantpool, using the current ClassInfos or the old InnerClasses attribute to build the table.
     *
     * @return a new InnerClasses attribute or null if this class does not reference any inner classes.
     */
    public InnerClasses buildInnerClassesAttribute() {

        ConstantPoolGen cpg = classGen.getConstantPool();

        // check+update InnerClasses attribute (and add referenced nested classes)
        List<ClassRef> referencedClasses = new LinkedList<ClassRef>();
        for (String name : ConstantPoolReferenceFinder.findReferencedClasses(classInfo)) {
            referencedClasses.add(classInfo.getAppInfo().getClassRef(name));
        }

        // find all referenced classes recursively, build InnerClass list from ClassInfo or old InnerClasses
        Collection<InnerClass> classes = buildInnerClasses(referencedClasses);

        if (classes.isEmpty()) {
            return null;
        }

        InnerClass[] ics = null;
        ics = classes.toArray(ics);

        int length = ics.length * 8 + 2;

        return new InnerClasses(cpg.addUtf8("InnerClasses"), length, ics, cpg.getConstantPool());
    }

    //////////////////////////////////////////////////////////////////////////////
    // private methods and maintenance code
    //////////////////////////////////////////////////////////////////////////////

    private InnerClass findInnerClass(InnerClasses ic, String innerClassName) {
        if ( ic != null ) {
            for (InnerClass i : ic.getInnerClasses()) {
                if (getInnerClassName(i).equals(innerClassName)) {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * Build a map of InnerClasses containing all entries for all inner classes in the 'classes'
     * parameter and all their enclosing inner classes.
     *
     * @param classes a collection of classes the result should contain.
     * @return the map of className->InnerClass containing all inner classes in the 'classes' parameter.
     */
    private Collection<InnerClass> buildInnerClasses(Collection<ClassRef> classes) {

        List<ClassRef> queue = new LinkedList<ClassRef>(classes);
        Set<String> visited = new HashSet<String>();
        List<InnerClass> inner = new LinkedList<InnerClass>();

        ConstantPoolGen cpg = classGen.getConstantPool();
        InnerClasses ic = getInnerClassesAttribute();

        while (!queue.isEmpty()) {
            ClassRef ref = queue.remove(0);
            ClassInfo cls = ref.getClassInfo();

            int innerClassIdx;
            int outerClassIdx = 0;
            int innerNameIdx = 0;
            int flags = 0;
            String enclosingClass = null;

            if ( cls != null ) {
                InnerClassesInfo info = cls.getInnerClassesInfo();

                // only nested classes have an entry
                if ( !info.isNestedClass() ) {
                    continue;
                }

                enclosingClass = info.getEnclosingClassName();

                innerClassIdx = cpg.addClass(ref.getClassName());

                if ( !info.isLocalInnerClass() ) {
                    // class is a member, add outer class reference
                    outerClassIdx = cpg.addClass(enclosingClass);
                }
                if ( !info.isAnonymousInnerClass() ) {
                    // class has a simple name
                    innerNameIdx = cpg.addUtf8(info.getInnerName());
                }

                flags = cls.getAccessFlags();

            } else {
                // unknown class, need to check the existing InnerClass array, if it exists
                InnerClass i = findInnerClass(ic, ref.getClassName());

                if ( i == null ) {
                    // class is not an innerclass according to our old InnerClasses table
                    continue;
                }

                innerClassIdx = i.getInnerClassIndex();
                outerClassIdx = i.getOuterClassIndex();
                innerNameIdx  = i.getInnerNameIndex();
                flags         = i.getInnerAccessFlags();
            }

            // add enclosing class to queue
            // Note: if this is a (known) local class, enclosingClass is set, but outerClassIdx is 0,
            // but we add the enclosing class anyway to the queue, because the EnclosingMethod attribute
            // will add a reference to the enclosing class anyway
            if ( enclosingClass != null && !visited.contains(enclosingClass) ) {
                queue.add( classInfo.getAppInfo().getClassRef(enclosingClass) );
            }

            visited.add(ref.getClassName());

            inner.add(new InnerClass(innerClassIdx, outerClassIdx, innerNameIdx, flags));
        }

        return inner;
    }

    private void updateInnerClassFlag() {
        // check if this class appears as inner class (iff this is an inner class, it must
        // appear in the attribute by definition)
        aNestedClass = getInnerClassAttribute(classInfo.getClassName()) != null;
    }

    protected void resetHierarchyInfos() {
        enclosingClass = null;
        nestedClasses.clear();
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void updateClassHierarchy() {
        if ( !aNestedClass ) {
            return;
        }
        enclosingClass = classInfo.getAppInfo().getClassInfo(getEnclosingClassName());
        if ( enclosingClass != null ) {
            enclosingClass.getInnerClassesInfo().nestedClasses.add(classInfo);
        } else {
            throw new AppInfoError("Enclosing class "+getEnclosingClassName()+" of class "+classInfo.getClassName()
                        +" is not loaded, but unknown outer classes are not supported.");
        }
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    protected void removeFromClassHierarchy() {
        if ( enclosingClass != null ) {
            enclosingClass.getInnerClassesInfo().nestedClasses.remove(classInfo);
        }

        for (ClassInfo c : nestedClasses) {
            c.getInnerClassesInfo().enclosingClass = null;
        }
    }
}
