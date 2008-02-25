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

import com.jopdesign.libgraph.struct.ConstantClass;
import com.jopdesign.libgraph.struct.ConstantField;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.ConstantPoolInfo;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.StringType;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ConstantPoolGen;

/**
 * Implementation of Bcel constantpool access, based on the bcel implementations.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelConstantPoolInfo extends ConstantPoolInfo {

    private ConstantPoolGen cpg;
    private BcelClassInfo classInfo;

    public BcelConstantPoolInfo(BcelClassInfo classInfo) {
        super(classInfo.getAppStruct());
        this.classInfo = classInfo;
        this.cpg = classInfo.getConstantPoolGen();
    }

    public ConstantValue getConstant(int pos) {

        Constant c = cpg.getConstantPool().getConstant(pos);

        switch(c.getTag()) {
            case org.apache.bcel.Constants.CONSTANT_String:
                int i = ((ConstantString)c).getStringIndex();
                c = cpg.getConstantPool().getConstant(i);
                return new ConstantValue(((ConstantUtf8)c).getBytes());

            case org.apache.bcel.Constants.CONSTANT_Float:
                return new ConstantValue(TypeInfo.CONST_FLOAT, new Float(((ConstantFloat)c).getBytes()));

            case org.apache.bcel.Constants.CONSTANT_Integer:
                return new ConstantValue(TypeInfo.CONST_INT, new Integer(((ConstantInteger)c).getBytes()));

            case org.apache.bcel.Constants.CONSTANT_Long:
	            return new ConstantValue(TypeInfo.CONST_LONG, new Long(((ConstantLong)c).getBytes()));

            case org.apache.bcel.Constants.CONSTANT_Double:
	            return new ConstantValue(TypeInfo.CONST_DOUBLE, new Double(((ConstantDouble)c).getBytes()));

            default:
                throw new RuntimeException("Unknown or invalid constant type at " + pos);
        }
    }

    public ConstantClass getClassReference(int pos) throws TypeException {
        ConstantPool cp = cpg.getConstantPool();
        org.apache.bcel.classfile.ConstantClass cmr =
                (org.apache.bcel.classfile.ConstantClass)cp.getConstant(pos);

        String className = ((ConstantUtf8)cp.getConstant(cmr.getNameIndex())).getBytes().replace('/','.');

        return getAppStruct().getConstantClass(className, false);
    }

    public ConstantMethod getMethodReference(int pos, boolean isStatic) throws TypeException {
        ConstantPool cp = cpg.getConstantPool();
        ConstantCP cmr  = (ConstantCP)cp.getConstant(pos);
        ConstantNameAndType cnat = (ConstantNameAndType)cp.getConstant(cmr.getNameAndTypeIndex());

        String signature = ((ConstantUtf8)cp.getConstant(cnat.getSignatureIndex())).getBytes();
        String name = ((ConstantUtf8)cp.getConstant(cnat.getNameIndex())).getBytes();

        String className = getClassName(cp, cmr);
        boolean isInterface = cmr.getTag() == Constants.CONSTANT_InterfaceMethodref;

        ConstantClass cClass = getAppStruct().getConstantClass(className, isInterface);
        return getAppStruct().getConstantMethod(cClass, name, signature, isStatic);
    }

    public ConstantField getFieldReference(int pos, boolean isStatic) throws TypeException {
        ConstantPool cp = cpg.getConstantPool();
        ConstantCP cmr  = (ConstantCP)cp.getConstant(pos);
        ConstantNameAndType cnat = (ConstantNameAndType)cp.getConstant(cmr.getNameAndTypeIndex());

        String name = ((ConstantUtf8)cp.getConstant(cnat.getNameIndex())).getBytes();
        String signature = ((ConstantUtf8)cp.getConstant(cnat.getSignatureIndex())).getBytes();

        String className = getClassName(cp, cmr);

        ConstantClass cClass = getAppStruct().getConstantClass(className, false);
        return getAppStruct().getConstantField(cClass, name, signature, isStatic);
    }


    public int addConstant(ConstantValue value) throws TypeException {

        classInfo.setModified();

        switch ( value.getType().getMachineType() ) {
            case TypeInfo.TYPE_INT:
                return cpg.addInteger(value.getIntValue());
            case TypeInfo.TYPE_LONG:
                return cpg.addLong(value.getLongValue());
            case TypeInfo.TYPE_FLOAT:
                return cpg.addFloat(value.getFloatValue());
            case TypeInfo.TYPE_DOUBLE:
                return cpg.addDouble(value.getDoubleValue());
            case TypeInfo.TYPE_REFERENCE:
                if ( StringType.DESCRIPTOR.equals(value.getType().getDescriptor() ) ) {
                    return cpg.addString(value.getTxtValue());
                }
            default:
                throw new TypeException("Invalid constant type {" + value.getType().getTypeName() + "}" );
        }
    }

    public int addConstant(com.jopdesign.libgraph.struct.ConstantClass value) {
        classInfo.setModified();
        return cpg.addClass(value.getClassName());
    }

    public int addConstant(ConstantMethod value) {
        classInfo.setModified();
        if ( value.isInterface() ) {
            return cpg.addInterfaceMethodref(value.getClassName(), value.getMethodName(),
                    value.getSignature());
        }
        return cpg.addMethodref(value.getClassName(), value.getMethodName(),
                value.getSignature());
    }

    public int addConstant(ConstantField value) {
        classInfo.setModified();
        return cpg.addFieldref(value.getClassName(),
                value.getFieldName(),
                value.getSignature());
    }

    protected String getClassName(ConstantPool cp, ConstantCP cmr) {
        return cp.getConstantString(cmr.getClassIndex(),
                org.apache.bcel.Constants.CONSTANT_Class).replace('/', '.');
    }

}
