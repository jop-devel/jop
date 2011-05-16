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

package com.jopdesign.common;

import com.jopdesign.common.type.Descriptor;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MemberID;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.FieldGen;

/**
 * Class for a field member of a ClassInfo.
 *  
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class FieldInfo extends ClassMemberInfo {

    private final FieldGen fieldGen;

    public FieldInfo(ClassInfo classInfo, FieldGen fieldGen) {
        super(classInfo,
              new MemberID(classInfo.getClassName(), fieldGen.getName(), new Descriptor(fieldGen.getType())),
              fieldGen);
        this.fieldGen = fieldGen;
    }

    public boolean isTransient() {
        return fieldGen.isTransient();
    }

    public void setTransient(boolean val) {
        fieldGen.isTransient(val);
    }

    public boolean isVolatile() {
        return fieldGen.isVolatile();
    }
    
    public void setVolatile(boolean val) {
        fieldGen.isVolatile(val);
    }

    public void setEnum(boolean flag) {
        fieldGen.isEnum(flag);
    }

    public boolean isEnum() {
        return fieldGen.isEnum();
    }

    public Field getField() {
        Field field = fieldGen.getField();
        // Workaround for BCEL bug: ConstantValue attribute gets attached to create a field, but is not removed,
        // so for every getField() call, there will be an additional ConstantValue attribute.. not good
        for (Attribute a : fieldGen.getAttributes()) {
            if (a instanceof ConstantValue) {
                fieldGen.removeAttribute(a);
            }
        }
        return field;
    }

    public FieldRef getFieldRef() {
        return new FieldRef(this);
    }

    /**
     * Should only be used by ClassInfo.
     * @return the internal fieldGen.
     */
    protected FieldGen getInternalFieldGen() {
        return fieldGen;
    }

}
