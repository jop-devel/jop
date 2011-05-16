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

package com.jopdesign.common.bcel;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.MemberID;
import com.jopdesign.common.type.MethodRef;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class EnclosingMethod extends CustomAttribute {

    private int classIndex;
    private int methodIndex;

    public EnclosingMethod(int name_index, ConstantPool constant_pool, int classIndex, int methodIndex) {
        super(Constants.ATTR_UNKNOWN, name_index, 4, constant_pool);
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;
    }

    /**
     * @return index of a ConstantClass
     */
    public int getClassIndex() {
        return classIndex;
    }

    /**
     * @param classIndex index of a ConstantClass
     */
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }

    /**
     * @return index of a ConstantNameAndType
     */
    public int getMethodIndex() {
        return methodIndex;
    }

    /**
     * @param methodIndex index of a ConstantNameAndType
     */
    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public MemberID getMemberID() {
        if (methodIndex == 0) {
            // not directly enclosed by a method
            return new MemberID(getClassName());
        }
        ConstantNameAndType nat = (ConstantNameAndType) constant_pool.getConstant(methodIndex);
        return new MemberID(getClassName(), nat.getName(constant_pool),
                Descriptor.parse(nat.getSignature(constant_pool)));
    }

    public String getClassName() {
        ConstantClass cls = (ConstantClass) constant_pool.getConstant(classIndex);
        return cls.getBytes(constant_pool).replace('/','.');
    }

    public MethodRef getMethodRef() {
        return methodIndex == 0 ? null : AppInfo.getSingleton().getMethodRef(getMemberID());
    }

    @Override
    public void dump(DataOutputStream file) throws IOException {
        super.dump(file);
        file.writeShort(classIndex);
        file.writeShort(methodIndex);
    }

    @Override
    public Attribute copy(ConstantPool _constant_pool) {
        return new EnclosingMethod(name_index, _constant_pool, classIndex, methodIndex);
    }

    @Override
    public String toString() {
        return "EnclosingMethod "+ getMemberID();
    }
}
