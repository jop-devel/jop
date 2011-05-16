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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantNameAndTypeInfo extends ConstantInfo<MemberID, Type> {
    
    public ConstantNameAndTypeInfo(MemberID value) {
        super(Constants.CONSTANT_NameAndType, value);
    }

    @Override
    public ClassRef getClassRef() {
        return null;
    }

    @Override
    public Type getType() {
        return getValue().getDescriptor().getType();
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        int n = cpg.addUtf8(getValue().getMemberName());
        int s = cpg.addUtf8(getValue().getDescriptor().toString());
        return new ConstantNameAndType(n, s);
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        MemberID sig = getValue();
        return cpg.addNameAndType(sig.getMemberName(), sig.getDescriptor().toString());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        MemberID sig = getValue();
        return cpg.lookupNameAndType(sig.getMemberName(), sig.getDescriptor().toString());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        return null;
    }

}
