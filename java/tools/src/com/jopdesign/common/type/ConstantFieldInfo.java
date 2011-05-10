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

import com.jopdesign.common.FieldInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantFieldInfo extends ConstantInfo<FieldRef, Type> {

    public ConstantFieldInfo(FieldRef value) {
        super(Constants.CONSTANT_Fieldref, value);
    }

    @Override
    public ClassRef getClassRef() {
        return getValue().getClassRef();
    }

    @Override
    public Type getType() {
        return getValue().getType();
    }

    public String getName() {
        return getValue().getName();
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        FieldRef fieldRef = getValue();
        int i = cpg.addClass(fieldRef.getClassName());
        int n = cpg.addNameAndType(fieldRef.getName(), fieldRef.getType().getSignature());
        return new ConstantFieldref(i, n);
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        FieldRef fieldRef = getValue();
        return cpg.addFieldref(fieldRef.getClassName(), fieldRef.getName(),
                               fieldRef.getType().getSignature());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        FieldRef fieldRef = getValue();
        return cpg.lookupFieldref(fieldRef.getClassName(), fieldRef.getName(),
                               fieldRef.getType().getSignature());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        FieldInfo fieldInfo = getValue().getFieldInfo();
        if (fieldInfo != null && fieldInfo.isStatic()) {
            // Special case although this is not a constant we are pushing, but it simplifies some code,
            return new GETSTATIC(addConstant(cpg));
        }
        return null;
    }

}
