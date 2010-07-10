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

import com.jopdesign.common.AppInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class ConstantInfo<T> {

    private final byte tag;
    private final T value;

    protected ConstantInfo(byte tag, T value) {
        this.tag = tag;
        this.value = value;
    }

    public static <U> ConstantInfo<U> createFromValue(U value) {
        return null;
    }

    public static ConstantInfo<?> createFromConstant(AppInfo appInfo, ConstantPool cp, Constant constant) {
        byte tag = constant.getTag();
        switch (tag) {
            case Constants.CONSTANT_Class:

            case Constants.CONSTANT_Fieldref:

            case Constants.CONSTANT_Methodref:

            case Constants.CONSTANT_InterfaceMethodref:

            case Constants.CONSTANT_String:
                return new ConstantStringInfo(((ConstantString)constant).getBytes(cp), false);
            case Constants.CONSTANT_Integer:
                return new ConstantIntegerInfo(((ConstantInteger)constant).getBytes());
            case Constants.CONSTANT_Float:
                return new ConstantFloatInfo(((ConstantFloat)constant).getBytes());
            case Constants.CONSTANT_Long:
                return new ConstantLongInfo(((ConstantLong)constant).getBytes());
            case Constants.CONSTANT_Double:
                return new ConstantDoubleInfo(((ConstantDouble)constant).getBytes());
            case Constants.CONSTANT_NameAndType:

            case Constants.CONSTANT_Utf8:
                return new ConstantStringInfo(((ConstantUtf8)constant).getBytes(), true);
            default:
                throw new ClassFormatException("Invalid byte tag in constant pool: " + tag);
        }
    }

    public byte getTag() {
        return tag;
    }

    public T getValue() {
        return value;
    }

    public abstract ClassRef getClassRef();

    public abstract TypeInfo getTypeInfo();

    public abstract Constant createConstant(ConstantPoolGen cpg);

    public abstract int addConstant(ConstantPoolGen cpg);

    public abstract int lookupConstant(ConstantPoolGen cpg);

    @Override
    public String toString() {
        return value.toString();
    }
}
