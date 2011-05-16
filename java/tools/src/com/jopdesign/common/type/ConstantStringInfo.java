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
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC_W;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ConstantStringInfo extends ConstantInfo<String, ObjectType> {

    public ConstantStringInfo(String value) {
        super(Constants.CONSTANT_String, value);
    }

    public ConstantStringInfo(String value, boolean isUtf8Entry) {
        super(isUtf8Entry ? Constants.CONSTANT_Utf8 : Constants.CONSTANT_String, value);
    }

    public boolean isUtf8Entry() {
        return getTag() == Constants.CONSTANT_Utf8;
    }

    @Override
    public ClassRef getClassRef() {
        if ( !isUtf8Entry() ) {
            return AppInfo.getSingleton().getClassRef("java.lang.String");
        }
        return null;
    }

    @Override
    public ObjectType getType() {
        return Type.STRING;
    }

    @Override
    public Constant createConstant(ConstantPoolGen cpg) {
        if ( getTag() == Constants.CONSTANT_Utf8 ) {
            return new ConstantUtf8(getValue());
        }
        int i = cpg.addUtf8(getValue());
        return new ConstantString(i);
    }

    @Override
    public int addConstant(ConstantPoolGen cpg) {
        if ( getTag() == Constants.CONSTANT_Utf8 ) {
            return cpg.addUtf8(getValue());
        }
        return cpg.addString(getValue());
    }

    @Override
    public int lookupConstant(ConstantPoolGen cpg) {
        if ( getTag() == Constants.CONSTANT_Utf8 ) {
            return cpg.lookupUtf8(getValue());
        }
        return cpg.lookupString(getValue());
    }

    @Override
    public Instruction createPushInstruction(ConstantPoolGen cpg) {
        if (getTag() == Constants.CONSTANT_Utf8) {
            return null;
        }
        int index = addConstant(cpg);
        return index < 256 ? new LDC(index) : new LDC_W(index);
    }
}
