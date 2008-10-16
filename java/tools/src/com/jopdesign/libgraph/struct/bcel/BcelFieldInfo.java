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

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.FieldInfo;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.StringType;
import com.jopdesign.libgraph.struct.type.TypeHelper;
import com.jopdesign.libgraph.struct.type.TypeInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BcelFieldInfo extends FieldInfo {

    private Field field;
    private TypeInfo type;

    public BcelFieldInfo(ClassInfo classInfo, Field field) throws TypeException {
        super(classInfo);
        this.field = field;
        type = TypeHelper.parseType(getClassInfo().getAppStruct(), field.getType());
    }

    public String getName() {
        return field.getName();
    }

    public String getSignature() {
        return field.getSignature();
    }

    public TypeInfo getType() {
        return type;
    }

    public ConstantValue getConstantValue() {
        if ( type.getMachineType() == TypeInfo.TYPE_REFERENCE &&
                !StringType.DESCRIPTOR.equals(type.getDescriptor()) )
        {
            // NOTICE constant value for reference??
            return null;
        }

        org.apache.bcel.classfile.ConstantValue value = field.getConstantValue();
        if ( value != null ) {
            return getClassInfo().getConstantPoolInfo().getConstant(value.getConstantValueIndex());
        } else {
            return null;
        }
    }

    public boolean isStatic() {
        return field.isStatic();
    }

    public void setFinal(boolean val) {
        int af = field.getModifiers();
        if ( val ) {
            field.setModifiers(af | Constants.ACC_FINAL);
        } else {
            field.setModifiers(af & (~Constants.ACC_FINAL));
        }
    }

    public void setStatic(boolean val) {
        int af = field.getModifiers();
        if ( val ) {
            field.setModifiers(af | Constants.ACC_STATIC);
        } else {
            field.setModifiers(af & (~Constants.ACC_STATIC));
        }
    }
    
    public void setAccessType(int type) {
        int af = field.getAccessFlags() & ~(Constants.ACC_PRIVATE|Constants.ACC_PROTECTED|Constants.ACC_PUBLIC);
        switch (type) {
            case ACC_PRIVATE: af |= Constants.ACC_PRIVATE; break;
            case ACC_PROTECTED: af |= Constants.ACC_PROTECTED; break;
            case ACC_PUBLIC: af |= Constants.ACC_PUBLIC; break;
        }
        field.setAccessFlags(af);
    }

    public boolean isFinal() {
        return field.isFinal();
    }

    public boolean isVolatile() {
        return field.isVolatile();
    }

    public boolean isSynchronized() {
        return field.isSynchronized();
    }

    public boolean isTransient() {
        return field.isTransient();
    }

    public boolean isConst() {
        return field.getConstantValue() != null;
    }

    public boolean isPrivate() {
        return field.isPrivate();
    }

    public boolean isProtected() {
        return field.isProtected();
    }

    public boolean isPublic() {
        return field.isPublic();
    }
}
