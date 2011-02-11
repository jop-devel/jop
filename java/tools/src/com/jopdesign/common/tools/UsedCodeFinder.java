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
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import com.jopdesign.common.tools.ClassReferenceFinder.ReferenceResult;
import com.jopdesign.common.type.ArrayTypeInfo;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.ObjectTypeInfo;
import com.jopdesign.common.type.Signature;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class can be used to mark all used class members, to load missing classes (following only used code)
 * and to remove unused classes and class members.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class UsedCodeFinder {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".UsedCodeFinder");

    private static KeyManager.CustomKey keyUsed;
    
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
     * @return true if it has been marked used, else false.
     */
    public boolean isUsed(MemberInfo member) {
        Boolean used = (Boolean) member.getCustomValue(getUseMarker());
        // if it hasn't been marked, assume unused
        return used != null && used;
    }

    /**
     * Mark a member as used. Note that this does not recurse down or marks the container as used.
     *
     * @see #markUsedMembers(ClassInfo,boolean)
     * @see #markUsedMembers(MethodInfo)
     * @see #markUsedMembers(FieldInfo)
     * @param member the member to mark as used.
     * @return the old marker value.
     */
    public boolean setUsed(MemberInfo member) {
        Boolean used = (Boolean) member.setCustomValue(getUseMarker(), true);
        return used != null && used;
    }

    /**
     * Mark all used members, starting at all AppInfo roots.
     *
     * @see #removeUnusedMembers()
     * @see #markUsedMembers(ClassInfo,boolean)
     * @see #markUsedMembers(MethodInfo)
     * @see #markUsedMembers(FieldInfo)
     */
    public void markUsedMembers() {
        resetMarks();

        for (MethodInfo root : appInfo.getRootMethods()) {
            // this also marks the containing class as used, and includes the main method
            markUsedMembers(root);
        }
        if (ignoredClasses.size() > 0 ) {
            int num = ignoredClasses.size();
            logger.info("Ignored " + num + " referenced "+ (num == 1 ? "class: " : "classes: ") + ignoredClasses);
        }
    }

    public void markUsedMembers(ClassInfo rootClass, boolean visitMembers) {
        // has already been visited before, do not recurse down again.
        if (setUsed(rootClass)) return;

        // visit superclass and interfaces, attributes, but not methods or fields
        logger.debug("Visiting references of "+rootClass);
        ReferenceResult found = ClassReferenceFinder.findReferencedMembers(rootClass, visitMembers);
        visitReferences(found);

        // at least we need to visit the static initializer
        MethodInfo clinit = rootClass.getMethodInfo(ClinitOrder.clinitSig);
        if (clinit != null) {
            markUsedMembers(clinit);
        }

    }

    public void markUsedMembers(FieldInfo rootField) {
        // has already been visited before, do not recurse down again.
        if (setUsed(rootField)) return;

        // visit type info, attributes, constantValue
        logger.debug("Visiting references of "+rootField);
        ReferenceResult found = ClassReferenceFinder.findReferencedMembers(rootField);
        visitReferences(found);
    }

    public void markUsedMembers(MethodInfo rootMethod) {
        // has already been visited before, do not recurse down again.
        if (setUsed(rootMethod)) return;

        // visit parameters, attributes, instructions, tables, ..
        logger.debug("Visiting references of "+rootMethod);
        ReferenceResult found = ClassReferenceFinder.findReferencedMembers(rootMethod);
        visitReferences(found);
    }

    /**
     * Remove all unused classes, methods and fields.
     * <p>
     * Make sure you mark the used methods before, else everything will be removed!</p>
     *
     * @see #markUsedMembers()
     */
    public void removeUnusedMembers() {
        AppInfo appInfo = AppInfo.getSingleton();

        // we cannot modify the lists while iterating through it
        List<ClassInfo> unusedClasses = new LinkedList<ClassInfo>();
        List<FieldInfo> unusedFields = new LinkedList<FieldInfo>();
        List<MethodInfo> unusedMethods = new LinkedList<MethodInfo>();

        int fields = 0;
        int methods = 0;

        for (ClassInfo cls : appInfo.getClassInfos()) {
            if (!isUsed(cls)) {
                unusedClasses.add(cls);
                logger.debug("Removing unused class " +cls);
                continue;
            }

            unusedFields.clear();
            unusedMethods.clear();

            for (FieldInfo f : cls.getFields()) {
                if (!isUsed(f)) {
                    unusedFields.add(f);
                    logger.debug("Removing unused field "+f);
                    fields++;
                }
            }
            for (MethodInfo m : cls.getMethods()) {
                if (!isUsed(m)) {
                    unusedMethods.add(m);
                    logger.debug("Removing unused method "+m);
                    methods++;
                }
            }

            for (FieldInfo f : unusedFields) {
                cls.removeField(f.getShortName());
            }
            for (MethodInfo m : unusedMethods) {
                cls.removeMethod(m.getMemberSignature());
            }
        }

        appInfo.removeClasses(unusedClasses);

        int classes = unusedClasses.size();
        logger.info("Removed " + classes + (classes == 1 ? " class, " : " classes, ") +
                                 fields + (fields == 1 ? " field, " : " fields, ") +
                                 methods + (methods == 1 ? " method" : " methods"));
    }
    
    private void visitReferences(ReferenceResult refs) {
        for (String signature : refs.getMembers()) {
            // The signatures returned by the reference finder use an alternative syntax which is 
            // always unique, so if in doubt, we need to interpret it as a classname, not a fieldname
            Signature sig = Signature.parse(signature, false);
            
            // find/load the corresponding classInfo
            ClassInfo cls = getClassInfo(sig);
            // class has been excluded from loading, skip this class
            if (cls == null) {
                continue;
            }
                    
            // class is used, visit it if it has not yet been visited, but skip its class members
            markUsedMembers(cls,false);
            
            // check if this signature specifies a class member (or just a class, in this case we are done)
            if (sig.isMethodSignature()) {
                // It's a method! mark the method as used (implementations are marked later)
                MethodRef ref = appInfo.getMethodRef(sig);
                MethodInfo method = ref.getMethodInfo();

                // We mark the referenced class as used..
                ClassInfo refCls = ref.getClassRef().getClassInfo();
                if (refCls != null) {
                    markUsedMembers(refCls, false);
                }

                // Hm, in fact, we might not need to do this, since all possible implementations will
                // be marked later, and this might find some unused code, but we may want to keep the
                // referenced method so that we keep the declarations.
                if (method != null) {
                    markUsedMembers(method);
                }

            } else if (sig.hasMemberName()) {
                // It's a field! No need to look in subclasses, fields are not virtual
                FieldRef ref = appInfo.getFieldRef(sig);
                FieldInfo field = ref.getFieldInfo();
                if (field == null) {
                    throw new JavaClassFormatError("Referenced field " + signature +" not found in the class!");
                }
                markUsedMembers(field);
            }            
        }

        for (InvokeSite invokeSite : refs.getInvokeSites()) {
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

    private ClassInfo getClassInfo(Signature sig) {
        String className;

        if (sig.isArraySignature()) {
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

        // TODO we could load classes on the fly here instead of checking the callgraph, basically
        //  - use loadClass in getClassInfo()
        //  - load and visit all superclasses, mark implementation in superclass as used if inherited
        //  - visit all known subclasses, mark this method as used
        //  - when a class is loaded, visit all methods which are used in the superclasses
        return appInfo.findImplementations(invoke);
    }
}
