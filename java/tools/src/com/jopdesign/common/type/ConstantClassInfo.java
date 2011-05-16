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

import com.jopdesign.common.ClassInfo;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.PushInstruction;
import org.apache.bcel.generic.ReferenceType;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantClassInfo extends ConstantInfo<ClassRef, ReferenceType> {
    
    public ConstantClassInfo(ClassRef value) {
        super(Constants.CONSTANT_Class, value);
    }

    public ConstantClassInfo(ClassInfo value) {
        super(Constants.CONSTANT_Class, value.getClassRef());
    }

    @Override
    public ClassRef getClassRef() {
        return getValue();
    }

    @Override
    public ReferenceType getType() {
        return getValue().getType();
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        int i = cpg.addUtf8(getValue().getClassName().replace('.','/'));
        return new ConstantClass(i);
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        return cpg.addClass(getValue().getClassName());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        return cpg.lookupClass(getValue().getClassName());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        // TODO this is debatable: we could push a new class by creating a new object ..
        return new NEW(addConstant(cpg));
    }

    public String getClassName() {
        return getValue().getClassName();
    }
}
