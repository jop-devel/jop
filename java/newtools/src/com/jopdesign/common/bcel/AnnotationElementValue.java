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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationElementValue {

    private byte tag;
    private ConstantPool constantPool;
    private short constValueIndex;

    public static AnnotationElementValue createValue(DataInputStream in, ConstantPool cp) throws IOException {
        byte tag = in.readByte();

        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'T':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                short constValueIndex = in.readShort();
                return new AnnotationElementValue(tag, cp, constValueIndex);
            case 'e':
                // enum_const_value
            case 'c':
                // class_info_index
            case '@':
                // annotation_value
            case '[':
                // array_value
            default:
                throw new UnsupportedOperationException("Annotation element value tag "+((char)tag)+" not supported");
        }
    }

    public AnnotationElementValue(byte tag, ConstantPool constantPool, short constValueIndex) {
        this.tag = tag;
        this.constantPool = constantPool;
        this.constValueIndex = constValueIndex;
    }

    public byte getTag() {
        return tag;
    }

    public void setTag(byte tag) {
        this.tag = tag;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public short getConstValueIndex() {
        return constValueIndex;
    }

    public void setConstValueIndex(short constValueIndex) {
        this.constValueIndex = constValueIndex;
    }

    public int length() {
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'T':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                return 3;
            case 'e':
                // enum_const_value
            case 'c':
                // class_info_index
            case '@':
                // annotation_value
            case '[':
                // array_value
            default:
                throw new UnsupportedOperationException("Annotation element value tag "+((char)tag)+" not supported");
        }
    }

    public void dump(DataOutputStream out) throws IOException {
        out.writeByte(tag);

        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'T':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                out.writeShort(constValueIndex);
                return;
            case 'e':
                // enum_const_value
            case 'c':
                // class_info_index
            case '@':
                // annotation_value
            case '[':
                // array_value
            default:
                throw new UnsupportedOperationException("Annotation element value tag "+((char)tag)+" not supported");
        }
    }

    public AnnotationElementValue copy() {
        return null;
    }


}
