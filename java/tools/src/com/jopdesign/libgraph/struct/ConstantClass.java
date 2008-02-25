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

import com.jopdesign.libgraph.struct.type.ArrayRefType;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ConstantClass {

    private ClassInfo classInfo;
    private String className;
    private boolean cInterface;
    private ArrayRefType arrayType;

    public ConstantClass(ClassInfo classInfo) {
        this.classInfo = classInfo;
        className = null;
        cInterface = false;
    }

    public ConstantClass(String className) {
        classInfo = null;
        this.className = className;
        cInterface = false;
    }

    public ConstantClass(String className, boolean isInterface) {
        this.className = className;
        this.cInterface = isInterface;
    }

    /**
     * Create an array class.
     * @param arrayType the type of the array (including the array itself).
     */
    public ConstantClass(ArrayRefType arrayType) {
        className = arrayType.getDescriptor();
        this.arrayType = arrayType;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public String getClassName() {
        return classInfo != null ? classInfo.getClassName() : className;
    }
    
    public boolean isAnonymous() {
        return classInfo == null;
    }

    public boolean isInterface() {
        return classInfo != null ? classInfo.isInterface() : cInterface;
    }

    public boolean isArray() {
        return arrayType != null;
    }

    public ArrayRefType getArrayType() {
        return arrayType;
    }
}
