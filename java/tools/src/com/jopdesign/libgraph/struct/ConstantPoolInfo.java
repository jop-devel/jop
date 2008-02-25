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

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class ConstantPoolInfo {
    
    private AppStruct appStruct;

    protected ConstantPoolInfo(AppStruct appStruct) {
        this.appStruct = appStruct;
    }

    public AppStruct getAppStruct() {
        return appStruct;
    }

    public abstract ConstantValue getConstant(int pos);

    public abstract ConstantClass getClassReference(int pos) throws TypeException;

    public abstract ConstantMethod getMethodReference(int pos, boolean isStatic) throws TypeException;

    public abstract ConstantField getFieldReference(int pos, boolean isStatic) throws TypeException;

    /**
     * Add a constant to the constantpool. Return the index of the constant.
     * If the constantpool already contains this value, return the index of the existing constant.
     *
     * @param value the value to add.
     * @return the index of this value in the constantpool.
     */
    public abstract int addConstant(ConstantValue value) throws TypeException;

    public abstract int addConstant(ConstantClass value);

    public abstract int addConstant(ConstantMethod value);

    public abstract int addConstant(ConstantField value);
}
