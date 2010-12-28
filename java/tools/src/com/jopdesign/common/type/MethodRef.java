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

    private final ClassRef classRef;
    private final MethodInfo methodInfo;

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

    public MethodInfo getMethodInfo() {
        // TODO if null, try getting from AppInfo
        return methodInfo;
    }

    public ClassRef getClassRef() {
        return methodInfo != null ? methodInfo.getClassInfo().getClassRef() : classRef;
    }

    public Descriptor getDescriptor() {
        return methodInfo != null ? methodInfo.getDescriptor() : descriptor;
    }

    public Signature getSignature() {
        return methodInfo != null ? methodInfo.getSignature()
                : new Signature(classRef.getClassName(), methodName, descriptor);
    }

    public Ternary isInterfaceMethod() {
        if ( methodInfo != null ) {
            return Ternary.valueOf(methodInfo.getClassInfo().isInterface());
        }
        return classRef.isInterface();
    }

    public Ternary exists() {
        if ( methodInfo != null ) return Ternary.TRUE;
        if ( classRef.getClassInfo() != null ) {
            return classRef.getClassInfo().getMethodInfo(getMemberSignature()) != null ?
                    Ternary.TRUE : Ternary.FALSE;
        }
        return Ternary.UNKNOWN;
    }

    public String getName() {
        return methodInfo != null ? methodInfo.getShortName() : methodName;
    }

    public String getClassName() {
        return methodInfo != null ? methodInfo.getClassInfo().getClassName() : classRef.getClassName();
    }

    public String getMemberSignature() {
        if ( methodInfo != null ) {
            return methodInfo.getMemberSignature();
        }
        return methodName + descriptor;
    }

    public String getFQMethodName() {
        return getClassName() + "." + getName();
    }

    @Override
    public int hashCode() {
        int hash = getClassName().hashCode();
        hash = 31 * hash + getMemberSignature().hashCode();
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
        return getClassName().equals(ref.getClassName()) && getMemberSignature().equals(ref.getMemberSignature());
    }

}
