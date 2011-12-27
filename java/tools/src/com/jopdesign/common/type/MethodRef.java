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

package com.jopdesign.common.type;

import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.Ternary;

/**
 * A container of a class reference.
 * Holds either a ClassInfo object or a classname with some infos if the
 * classInfo has not been loaded for some reason.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodRef {

    // Implementation Notice:
    // At least one of classRef and methodInfo is not null.
    // If methodInfo is null, methodName and descriptor must be set.
    // MethodInfo can be set lazily by getMethodInfo(). If methodInfo should be checked,
    // if (getMethodInfo()==null) must be used, but if only a check is needed if the other fields
    // are not null, if (methodInfo==null) is sufficient.

    private final ClassRef classRef;
    private MethodInfo methodInfo;

    private String methodName;
    private Descriptor descriptor;

    public MethodRef(ClassRef classRef, String methodName, Descriptor descriptor) {
        this.classRef = classRef;

        this.methodName = methodName;
        this.descriptor = descriptor;
        methodInfo = null;
    }

    public MethodRef(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.classRef = null;
    }

    public MethodRef(ClassRef classRef, MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
        this.classRef = classRef;
    }

    /**
     * Get the referenced method info. If the method is inherited, get the inherited methodInfo,
     * if the method is only defined by an implemented interface, get the interface methodInfo.
     *
     * @see ClassInfo#getMethodInfoInherited(MemberID, boolean)
     * @return the referenced method.
     */
    public MethodInfo getMethodInfo() {
        if (methodInfo == null) {
            // We could cache somehow if lookup fails, so we do not need to try again, but then how do we
            // know when to try again (e.g. after AppInfo has been updated)
            ClassInfo classInfo = classRef.getClassInfo();
            if ( classInfo != null ) {
                methodInfo = classInfo.getMethodInfoInherited(new MemberID(methodName, descriptor), true);
                /*
                if ( methodInfo != null && methodInfo.getClassName().equals(classRef.getClassName())) {
                    // method is defined in the given class
                    classRef = null;
                }
                */
            }

        }
        return methodInfo;
    }

    public ClassRef getClassRef() {
        return classRef != null ? classRef : methodInfo.getClassInfo().getClassRef();
    }

    public ClassInfo getClassInfo() {
        return classRef != null ? classRef.getClassInfo() : methodInfo.getClassInfo();
    }

    public Descriptor getDescriptor() {
        return methodInfo != null ? methodInfo.getDescriptor() : descriptor;
    }

    public MemberID getMemberID() {
        if (classRef != null && methodInfo != null && !classRef.getClassName().equals(methodInfo.getClassName())) {
            // we have a MethodRef to a method which is inherited
            return new MemberID(classRef.getClassName(), methodInfo.getShortName(), methodInfo.getDescriptor());
        }
        //noinspection ConstantConditions
        return methodInfo != null ? methodInfo.getMemberID()
                : new MemberID(classRef.getClassName(), methodName, descriptor);
    }

    public Ternary isInterfaceMethod() {
        // We do not use getMethodInfo() here, else we would run into problems because this method
        // is needed during classloading, and resolving the method does not work until classloading is completed.
        if ( methodInfo != null ) {
            return Ternary.valueOf(methodInfo.getClassInfo().isInterface());
        }
        return classRef.isInterface();
    }

    public Ternary exists() {
        if ( getMethodInfo() != null ) return Ternary.TRUE;
        if ( classRef.getClassInfo() != null ) {
            return classRef.getClassInfo().getMethodInfo(getMethodSignature()) != null ?
                    Ternary.TRUE : Ternary.FALSE;
        }
        return Ternary.UNKNOWN;
    }

    public String getName() {
        return methodInfo != null ? methodInfo.getShortName() : methodName;
    }

    public String getClassName() {
        return classRef != null ? classRef.getClassName() : methodInfo.getClassInfo().getClassName();
    }

    public String getMethodSignature() {
        if ( methodInfo != null ) {
            return methodInfo.getMethodSignature();
        }
        return methodName + descriptor;
    }

    public String getFQMethodName() {
        return getClassName() + "." + getName();
    }

    @Override
    public int hashCode() {
        int hash = getClassName().hashCode();
        hash = 31 * hash + getMethodSignature().hashCode();
        return hash;
    }

    /**
     * Check if this MethodRef refers to the same method as the given object.
     *
     * <p>Note that an unresolved method reference and a MethodInfo reference are equal
     * if they refer to the same method, but a MethodInfo object is never equal to a MethodRef.</p>
     *
     * @param obj the object to test.
     * @return true if the object is a methodref and refers to the same method.
     */
    @Override
    public boolean equals(Object obj) {
        // we might even allow equality for MethodInfo.getMethodRef(), but then we should
        // also test for MethodRef objects in MethodInfo.equals(), which is somehow creepy.. 
        if (!(obj instanceof MethodRef)) {
            return false;
        }
        MethodRef ref = (MethodRef) obj;
        return getClassName().equals(ref.getClassName()) && getMethodSignature().equals(ref.getMethodSignature());
    }

    @Override
    public String toString() {
        return getMemberID().toString();
    }
}
