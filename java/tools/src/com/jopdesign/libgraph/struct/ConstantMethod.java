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
import com.jopdesign.libgraph.struct.type.TypeHelper;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.log4j.Logger;

/**
 * Container for a reference to a method.
 * As the reference may refer to a subclass of the class which defines the method,
 * the class which was used to access the method is stored too.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ConstantMethod {

    private ClassInfo classInfo;
    private MethodInfo methodInfo;
    private MethodSignature signature;
    private String className;

    private static final Logger logger = Logger.getLogger(ConstantMethod.class);
    private boolean cInterface;
    private boolean cStatic;

    public ConstantMethod(MethodInfo methodInfo) throws TypeException {
        this.classInfo = methodInfo.getClassInfo();
        this.methodInfo = methodInfo;
        this.signature = methodInfo.getMethodSignature();
    }

    public ConstantMethod(ClassInfo classInfo, MethodInfo methodInfo) throws TypeException {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
        this.signature = methodInfo.getMethodSignature();
    }

    public ConstantMethod(String className, String methodName, String signature, boolean isInterface, 
                          boolean isStatic)
    {
        this.className = className;
        this.cInterface = isInterface;
        this.cStatic = isStatic;
        try {
            this.signature = TypeHelper.parseSignature(null, methodName, signature);
        } catch (TypeException e) {
            logger.error("Could not parse signature {"+signature+"}, should not happen.", e);
        }
    }

    public MethodSignature getMethodSignature() {
        return signature;
    }

    public String getSignature() {
        if ( methodInfo != null ) {
            return methodInfo.getSignature();
        }
        return signature.getSignature();
    }

    public String getClassName() {
        return classInfo != null ? classInfo.getClassName() : className;
    }

    public String getMethodName() {
        return methodInfo != null ? methodInfo.getName() : signature.getName();
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public TypeInfo getResultType() {
        return signature.getType();
    }

    public TypeInfo[] getParameterTypes() {
        return signature.getParameterTypes();
    }

    public boolean isAnonymous() {
        return classInfo == null || methodInfo == null;
    }

    public boolean isInterface() {
        return classInfo != null ? classInfo.isInterface() : cInterface;
    }

    public boolean isStatic() {
        return methodInfo != null ? methodInfo.isStatic() : cStatic;
    }

    public String getFQMethodName() {
        return MethodInfo.createFQMethodName(getClassName(), getMethodName(), getSignature());
    }

    public String toString() {
        return getFQMethodName();
    }
}
