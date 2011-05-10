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
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LDC_W;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantIntegerInfo extends ConstantInfo<Integer, BasicType> {

    public ConstantIntegerInfo(Integer value) {
        super(Constants.CONSTANT_Integer, value);
    }

    @Override
    public ClassRef getClassRef() {
        return null;
    }

    @Override
    public BasicType getType() {
        return Type.INT;
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        return new ConstantInteger(getValue());
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        return cpg.addInteger(getValue());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        return cpg.lookupInteger(getValue());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        if (getValue() >= -1 && getValue() <= 5) {
            return new ICONST(getValue());
        }
        if (getValue() >= -128 && getValue() < 128) {
            return new BIPUSH(getValue().byteValue());
        }
        if (getValue() >= -32768 && getValue() < 32768) {
            return new SIPUSH(getValue().shortValue());
        }
        int index = addConstant(cpg);
        return index < 256 ? new LDC(index) : new LDC_W(index);
    }
}
