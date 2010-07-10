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

/**
 * A container of a class reference.
 * Holds either a ClassInfo object or a classname with some infos if the
 * classInfo has not been loaded for some reason.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodRef {

    private ClassRef classRef;
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
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public ClassRef getClassRef() {
        return methodInfo != null ? methodInfo.getClassInfo().getClassRef() : classRef;
    }

    public Descriptor getDescriptor() {
        return methodInfo != null ? methodInfo.getDescriptor() : descriptor;
    }

    public boolean isInterfaceMethod() {
        return classRef.isInterface();
    }

    public String getName() {
        return methodInfo != null ? methodInfo.getName() : methodName;
    }

    public String getClassName() {
        return methodInfo != null ? methodInfo.getClassInfo().getClassName() : classRef.getClassName();
    }
}
