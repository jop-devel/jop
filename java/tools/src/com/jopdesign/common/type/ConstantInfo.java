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
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.JavaClassFormatError;
import org.apache.bcel.Constants;
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
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.PushInstruction;
import org.apache.bcel.generic.Type;
import org.apache.log4j.Logger;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class ConstantInfo<T, U extends Type> {

    private final byte tag;
    private final T value;

    protected static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".ConstantInfo");

    protected ConstantInfo(byte tag, T value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * Create a new constantInfo from a BCEL constant.
     * If the constantpool contains invalid data, a {@link JavaClassFormatError} is thrown.
     *
     * @param cp the constantpool used to resolve the index references.
     * @param constant the BCEL constant to convert
     * @return a new ConstantInfo containing the constant value.
     */
    public static ConstantInfo createFromConstant(ConstantPool cp, Constant constant) {
        MemberID sig;
        MethodRef methodRef;
        ConstantNameAndType nRef;
        AppInfo appInfo = AppInfo.getSingleton();

        byte tag = constant.getTag();
        switch (tag) {
            case Constants.CONSTANT_Class:
                ClassRef classRef = appInfo.getClassRef(((ConstantClass)constant).getBytes(cp).replace('/','.'));
                return new ConstantClassInfo(classRef);
            case Constants.CONSTANT_Fieldref:
                ConstantFieldref fRef = (ConstantFieldref) constant;
                nRef = (ConstantNameAndType) cp.getConstant(fRef.getNameAndTypeIndex());
                sig = new MemberID(fRef.getClass(cp), nRef.getName(cp), nRef.getSignature(cp));
                FieldRef fieldRef = appInfo.getFieldRef(sig);
                return new ConstantFieldInfo(fieldRef);
            case Constants.CONSTANT_Methodref:
                ConstantMethodref mRef = (ConstantMethodref) constant;
                nRef = (ConstantNameAndType) cp.getConstant(mRef.getNameAndTypeIndex());
                sig = new MemberID(mRef.getClass(cp), nRef.getName(cp), nRef.getSignature(cp));
                methodRef = appInfo.getMethodRef(sig, false);
                return new ConstantMethodInfo(methodRef);
            case Constants.CONSTANT_InterfaceMethodref:
                ConstantInterfaceMethodref imRef = (ConstantInterfaceMethodref) constant;
                nRef = (ConstantNameAndType) cp.getConstant(imRef.getNameAndTypeIndex());
                sig = new MemberID(imRef.getClass(cp), nRef.getName(cp), nRef.getSignature(cp));
                methodRef = appInfo.getMethodRef(sig, true);
                return new ConstantMethodInfo(methodRef);
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
                String name = ((ConstantNameAndType)constant).getName(cp);
                String signature = ((ConstantNameAndType)constant).getSignature(cp);
                return new ConstantNameAndTypeInfo(new MemberID(name, signature));
            case Constants.CONSTANT_Utf8:
                return new ConstantStringInfo(((ConstantUtf8)constant).getBytes(), true);
            default:
                throw new JavaClassFormatError("Invalid byte tag in constant pool: " + tag);
        }
    }

    public byte getTag() {
        return tag;
    }

    public T getValue() {
        return value;
    }

    /**
     * Get a reference to the class this constant refers to if it is a
     * class-, method- or field-constant.
     *
     * @return the referenced class or null if no class is referenced by this constant type.
     */
    public abstract ClassRef getClassRef();

    public abstract U getType();

    /**
     * Create a new constant but do not add it to the constant pool.
     * Note however that in order to get the indices for supplementary constants,
     * new constants might be created.
     *
     * @param cpg the constantpool to lookup or add required referenced constants.
     * @return a new constant for this constantinfo using the given constantpool.
     */
    public abstract Constant createConstant(ConstantPoolGen cpg);

    /**
     * Add this constant to the constantpool if needed and return the index of this constant in the constantpool.
     *
     * @param cpg the constantpool to use.
     * @return the index of this constant in the pool.
     */
    public abstract int addConstant(ConstantPoolGen cpg);

    public abstract int lookupConstant(ConstantPoolGen cpg);

    /**
     * Create an instruction which pushes this constant on the stack.
     * @param cpg the constant pool to use for the new instruction.
     * @return a new instruction which pushes this value.
     */
    public abstract Instruction createPushInstruction(ConstantPoolGen cpg);

    @Override
    public String toString() {
        return value.toString();
    }
}
