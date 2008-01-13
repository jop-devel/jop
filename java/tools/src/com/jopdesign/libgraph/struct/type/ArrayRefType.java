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
package com.jopdesign.libgraph.struct.type;

import com.jopdesign.libgraph.struct.ConstantClass;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ArrayRefType extends RefTypeInfo {

    private int dimension;
    private int[] length;
    private TypeInfo arrayType;
    private String descriptor;

    public ArrayRefType(int dimension, TypeInfo arrayType) {
        super(TYPE_ARRAYREF);
        this.dimension = dimension;
        this.arrayType = arrayType;
        descriptor = null;
        length = null;
    }


    public ArrayRefType(int dimension, int[] length, TypeInfo arrayType) {
        super(TYPE_ARRAYREF);
        this.dimension = dimension;
        this.length = length;
        this.arrayType = arrayType;
    }

    public int getDimension() {
        return dimension;
    }

    /**
     * get the length of the array, if known, else null.
     * @return the length of the array per dimension, or null if not statically known.
     */
    public int[] getArrayLength() {
        return length;
    }

    public TypeInfo getArrayType() {
        return arrayType;
    }

    public String getDescriptor() {
        if ( descriptor == null ) {
            StringBuffer out = new StringBuffer("");
            for (int i = 0; i < dimension; i++) {
                out.append("[");
            }
            out.append(arrayType.getDescriptor());
            descriptor = out.toString();
        }

        return descriptor;
    }

    public String getTypeName() {
        StringBuffer out = new StringBuffer(arrayType.getTypeName());
        for (int i = 0; i < dimension; i++) {
            out.append("[]");
        }
        return out.toString();
    }

    public ConstantClass getClassConstant() {
        // TODO check
        return new ConstantClass(getDescriptor());
    }
}
