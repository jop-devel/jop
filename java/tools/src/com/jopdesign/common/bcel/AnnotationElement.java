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

package com.jopdesign.common.bcel;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationElement {

    private short nameIndex;
    private AnnotationElementValue value;
    private ConstantPool constantPool;

    public static AnnotationElement createElement(DataInputStream in, ConstantPool cp) throws IOException {
        short nameIndex = in.readShort();
        return new AnnotationElement(nameIndex, AnnotationElementValue.createValue(in, cp), cp);
    }

    public AnnotationElement(short nameIndex, AnnotationElementValue value, ConstantPool constantPool) {
        this.nameIndex = nameIndex;
        this.value = value;
        this.constantPool = constantPool;
    }

    public short getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(short nameIndex) {
        this.nameIndex = nameIndex;
    }

    public String getName() {
        return ((ConstantUtf8)constantPool.getConstant(nameIndex)).getBytes();
    }

    public AnnotationElementValue getValue() {
        return value;
    }

    public void setValue(AnnotationElementValue value) {
        this.value = value;
    }

    public int length() {
        return 2 + value.length();
    }

    public void dump(DataOutputStream out) throws IOException {
        out.writeShort(nameIndex);
        value.dump(out);
    }

    public AnnotationElement copy() {
        return new AnnotationElement(nameIndex, value.copy(), constantPool);
    }

    @Override
    public String toString() {
        return "AnnotationElement{" +
                "nameIndex=" + nameIndex +
                ", value=" + value +
                '}';
    }

}
