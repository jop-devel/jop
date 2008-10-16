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
package com.jopdesign.libgraph.struct.bcel;

import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelMethodInfo extends MethodInfo {

    private Method method;
    private BcelMethodCode methodCode;
    private BcelClassInfo classInfo;

    public BcelMethodInfo(BcelClassInfo classInfo, Method method) {
        super(classInfo);
        this.classInfo = classInfo;
        this.methodCode = method.getCode() != null ? new BcelMethodCode(this, method) : null;
        this.method = method;
    }

    public String getName() {
        return method.getName();
    }

    public String getSignature() {
        return method.getSignature();
    }

    public boolean isPrivate() {
        return method.isPrivate();
    }

    public boolean isProtected() {
        return method.isProtected();
    }

    public boolean isPublic() {
        return method.isPublic();
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public boolean isSynchronized() {
        return method.isSynchronized();
    }

    public void setFinal(boolean val) {
        int af = method.getModifiers();
        if ( val ) {
            method.setModifiers(af | Constants.ACC_FINAL);
        } else {
            method.setModifiers(af & (~Constants.ACC_FINAL));
        }
    }

    public void setStatic(boolean val) {
        int af = method.getModifiers();
        if ( val ) {
            method.setModifiers(af | Constants.ACC_STATIC);
        } else {
            method.setModifiers(af & (~Constants.ACC_STATIC));
        }
    }

    public void setAccessType(int type) {
        int af = method.getAccessFlags() & ~(Constants.ACC_PRIVATE|Constants.ACC_PROTECTED|Constants.ACC_PUBLIC);
        switch (type) {
            case ACC_PRIVATE: af |= Constants.ACC_PRIVATE; break;
            case ACC_PROTECTED: af |= Constants.ACC_PROTECTED; break;
            case ACC_PUBLIC: af |= Constants.ACC_PUBLIC; break;
        }
        method.setAccessFlags(af);
    }

    public boolean isFinal() {
        return method.isFinal();
    }

    public boolean isInterface() {
        return method.isInterface();
    }

    public boolean isNative() {
        return method.isNative();
    }

    public boolean isAbstract() {
        return methodCode == null;
    }

    public MethodCode getMethodCode() {
        return methodCode;
    }

    /**
     * Called by MethodCode if new method has been generated.
     * @param method new method to set.
     */
    public void setMethod(Method method) {
        this.method = method;
        classInfo.setMethod(method);
    }

    public ConstantPoolGen getConstantPoolGen() {
        return classInfo.getConstantPoolGen();
    }
}
