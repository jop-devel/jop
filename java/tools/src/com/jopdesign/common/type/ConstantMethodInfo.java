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

import com.jopdesign.common.misc.Ternary;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantMethodInfo extends ConstantInfo<MethodRef, Type> {

    public ConstantMethodInfo(MethodRef value) {
        super(value.isInterfaceMethod() == Ternary.TRUE ?
                Constants.CONSTANT_InterfaceMethodref : Constants.CONSTANT_Methodref,
                value);
    }

    public boolean isInterfaceMethod() {
        return getTag() == Constants.CONSTANT_InterfaceMethodref;
    }

    public MethodRef getMethodRef() {
        return getValue();
    }

    @Override
    public ClassRef getClassRef() {
        return getValue().getClassRef();
    }

    @Override
    public Type getType() {
        return getValue().getDescriptor().getType();
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        MethodRef method = getValue();
        int i = cpg.addClass(method.getClassName());
        int n = cpg.addNameAndType(method.getName(), method.getDescriptor().toString());
        return new ConstantMethodref(i, n);
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        MethodRef method = getValue();
        if (isInterfaceMethod()) {
            return cpg.addInterfaceMethodref(method.getClassName(), method.getName(),
                    method.getDescriptor().toString());
        }
        return cpg.addMethodref(method.getClassName(), method.getName(),
                method.getDescriptor().toString());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        MethodRef method = getValue();
        if (isInterfaceMethod()) {
            return cpg.lookupInterfaceMethodref(method.getClassName(), method.getName(),
                    method.getDescriptor().toString());
        }
        return cpg.lookupMethodref(method.getClassName(), method.getName(), 
                method.getDescriptor().toString());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        // there are no method references on the stack, only class refs
        return null;
    }
}
