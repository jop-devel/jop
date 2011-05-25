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

package com.jopdesign.common.tools;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.KeyManager;
import com.jopdesign.common.MemberInfo;
import com.jopdesign.common.MethodCode;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.type.ArrayTypeInfo;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.ObjectTypeInfo;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class can be used to mark all used class members, to load missing classes (following only used code)
 * and to remove unused classes and class members.
 *
 * TODO If option is enabled: Mark methods visited by constantpool reference as 'grey' and do not follow,
 *      if a gray method is visited by findImplementations, follow and mark
 *      In a second pass, visit all gray methods, try to make abstract, or if making abstract is not possible,
 *      mark used and visit
 *      Then remove all unused 
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class UsedCodeFinder {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".UsedCodeFinder");

    private static KeyManager.CustomKey keyUsed;

    /**
     * Marker used by this class: UNUSED if a member is not marked,
     * MARKED if a member is referred to by the code but it has not been visited recursively,
     * USED if a member is used and has been visited recursively.
     */
    public enum Mark { UNUSED, MARKED, USED }

    private final AppInfo appInfo;
    private Set<String> ignoredClasses;

    private static KeyManager.CustomKey getUseMarker() {
        if (keyUsed == null) {
            keyUsed = KeyManager.getSingleton().registerKey(KeyManager.KeyType.STRUCT, "UsedCodeFinder");
        }
        return keyUsed;
    }

    /**
     * Create a new code finder
     */
    public UsedCodeFinder() {
        this.appInfo = AppInfo.getSingleton();
        this.ignoredClasses = new HashSet<String>(1);
    }

    /**
     * @return true if we found some referenced classes which were excluded.
     */
    public boolean hasIgnoredClasses() {
        return !ignoredClasses.isEmpty();
    }

    /**
     * Reset all loaded classes and their members to 'unused'.
     */
    public void resetMarks() {
        KeyManager km = KeyManager.getSingleton();
        km.clearAllValues(getUseMarker());
        ignoredClasses.clear();
    }

    /**
     * Check if a MemberInfo (class, method, field) has been marked as used.
     * If a member has not been marked, we assume it is unused.
     *
     * @param member the member to check.
     * @return the marker value.
     */
    public Mark getMark(MemberInfo member) {
        Mark mark = (Mark) member.getCustomValue(getUseMarker());
        // if it hasn't been marked, assume unused
        return mark != null ? mark : Mark.UNUSED;
    }

    /**
     * Mark a member as used. Note that this does not recurse down or marks the container as used.
     *
     * @see #markUsedMembers(ClassInfo,boolean)
     * @see #markUsedMembers(MethodInfo)
     * @see #markUsedMembers(FieldInfo)
     * @param member the member to mark as used.
     * @param markUsed if true, mark as USED, not as MARKED.
     * @return the old marker value.
     */
    public Mark setMark(MemberInfo member, boolean markUsed) {
        if (!markUsed) {
            // If the member is already marked used, do not mark it as 'not visited'
            if (getMark(member) == Mark.USED) return Mark.USED;
        }
        Mark mark = (Mark) member.setCustomValue(getUseMarker(), markUsed ? Mark.USED : Mark.MARKED);
        return mark != null ? mark : Mark.UNUSED;
    }

    /**
     * Mark all used members, starting at all AppInfo roots.
     * <p>
     * To avoid marking unused Runnable.run methods, remove them from the callgraph roots first.
     * </p>
     *
     * @see #markUsedMembers(ClassInfo,boolean)
     * @see #markUsedMembers(MethodInfo)
     * @see #markUsedMembers(FieldInfo)
     */
    public void markUsedMembers() {
        resetMarks();

        // This contains all application and JVM root methods and -classes
        for (MethodInfo root : appInfo.getRootMethods()) {
            // this also marks the containing class as used, and includes the main method
            markUsedMembers(root);
        }
        // We do not need to visit all <clinit>, they are marked when the classes are visited
        // but we need to add all Runnable.run() methods as root methods
        for (MethodInfo run : appInfo.getThreadRootMethods(true)) {
            markUsedMembers(run);
        }

        if (ignoredClasses.size() > 0 ) {
            int num = ignoredClasses.size();
            logger.info("Ignored " + num + " referenced "+ (num == 1 ? "class: " : "classes: ") + ignoredClasses);
        }
    }

    public void markUsedMembers(ClassInfo rootClass, boolean visitMembers) {
        // has already been visited before, do not recurse down again.
        if (!visitMembers && setMark(rootClass,true)==Mark.USED) return;

        // visit superclass and interfaces, attributes, but not methods or fields
        if (logger.isTraceEnabled()) {
            logger.trace("Visiting references of "+rootClass);
        }
        Set<String> found = ConstantPoolReferenceFinder.findReferencedMembers(rootClass, false);
        visitReferences(found);

        if (visitMembers) {
            for (FieldInfo field : rootClass.getFields()) {
                markUsedMembers(field);
            }
            for (MethodInfo method : rootClass.getMethods()) {
                markUsedMembers(method);
            }
        } else {
            // at least we need to visit the static initializer
            MethodInfo clinit = rootClass.getMethodInfo(ClinitOrder.clinitSig);
            if (clinit != null) {
                markUsedMembers(clinit);
            }

            // TODO if this implements Runnable, we could also mark the run() method, but using the callgraph
            // to find used threads is a bit more flexible
        }

    }

    public void markUsedMembers(FieldInfo rootField) {
        // has already been visited before, do not recurse down again.
        if (setMark(rootField,true)==Mark.USED) return;

        // mark the class containing this method, so we do not need to worry about
        // marking the class when marking root fields.
        markUsedMembers(rootField.getClassInfo(), false);

        // visit type info, attributes, constantValue
        if (logger.isTraceEnabled()) {
            logger.trace("Visiting references of "+rootField);
        }
        Set<String> found = ConstantPoolReferenceFinder.findReferencedMembers(rootField);
        visitReferences(found);
    }

    public void markUsedMembers(MethodInfo rootMethod) {
        // has already been visited before, do not recurse down again.
        if (setMark(rootMethod,true)==Mark.USED) return;

        // mark the class containing this method, so we do not need to worry about
        // marking the class when marking root methods.
        markUsedMembers(rootMethod.getClassInfo(), false);

        // visit parameters, attributes, instructions, tables, ..
        if (logger.isTraceEnabled()) {
            logger.trace("Visiting references of "+rootMethod);
        }
        Set<String> found = ConstantPoolReferenceFinder.findReferencedMembers(rootMethod);
        visitReferences(found);

        if (rootMethod.hasCode()) {
            visitInvokeSites(rootMethod.getCode());
        }
    }

    private void visitReferences(Set<String> refs) {
        for (String id : refs) {
            // The member IDs returned by the reference finder use a syntax which is
            // always unique, so if in doubt, we need to interpret it as a classname, not a fieldname
            MemberID sig = MemberID.parse(id, false);
            
            // find/load the corresponding classInfo
            ClassInfo cls = getClassInfo(sig);
            // class has been excluded from loading, skip this class
            if (cls == null) {
                continue;
            }
                    
            // referenced class is used, visit it if it has not yet been visited, but skip its class members
            // Note that this class might be different than the class containing the class member
            markUsedMembers(cls,false);
            
            // check if this id specifies a class member (or just a class, in this case we are done)
            if (sig.hasMethodSignature()) {
                // It's a method! mark the method as used (implementations are marked later)
                MethodRef ref = appInfo.getMethodRef(sig);
                MethodInfo method = ref.getMethodInfo();

                // We need not go down recursively here, since all possible implementations will be marked later,
                // and we might find some unused code. But we may want to keep the referenced method so that
                // we keep the declarations. We mark it without following it and make it abstract later.
                if (method != null) {
                    setMark(method, false);
                }

            } else if (sig.hasMemberName()) {
                // It's a field! No need to look in subclasses, fields are not virtual
                FieldRef ref = appInfo.getFieldRef(sig);
                FieldInfo field = ref.getFieldInfo();

                if (field != null) {
                    markUsedMembers(field);
                }
            }
        }
    }

    private void visitInvokeSites(MethodCode code) {
        for (InvokeSite invokeSite : code.getInvokeSites()) {
            // find all implementations for each invoke, mark them as used.
            for (MethodInfo method : findMethods(invokeSite)) {
                markUsedMembers(method);
            }
        }
    }

    private void ignoreClass(String className) {
        logger.debug("Ignored referenced class " +className);
        ignoredClasses.add(className);
    }

    private ClassInfo getClassInfo(MemberID sig) {
        String className;

        if (sig.isArrayClass()) {
            ArrayTypeInfo at = ArrayTypeInfo.parse(sig.getClassName());
            if (at.getElementType() instanceof ObjectTypeInfo) {
                className = ((ObjectTypeInfo)at.getElementType()).getClassRef().getClassName();
            } else {
                return null;
            }
        } else {
            className = sig.getClassName();
        }

        ClassInfo classInfo = appInfo.getClassInfo(className);
        if (classInfo == null) {
            ignoreClass(className);
        }
        return classInfo;
    }

    private Collection<MethodInfo> findMethods(InvokeSite invoke) {
        // this checks the callgraph, if available, else the type graph

        // We do not need to use callstrings here: If a method can be reached over any callstring
        // we need to keep it anyway.

        // We could load classes on the fly here instead of checking the callgraph, basically
        //  - use loadClass in getClassInfo()
        //  - load and visit all superclasses, mark implementation in superclass as used if inherited
        //  - visit all known subclasses, mark this method as used
        //  - when a class is loaded, visit all methods which are used in the superclasses
        return appInfo.findImplementations(invoke);
    }
}
