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
import java.util.ArrayList;
import java.util.List;

/**
 * Just for me, because the formatting in the JVM spec is horrible ..
 *
 * annotation {
 *     u2   type_index;
 *     u2   num_element_value_pairs;
 *     {
 *         u2               element_name_index;
 *         element_value    value;
 *     } element_value_pairs[num_element_value_pairs];
 * }
 *
 * element_value {
 *     u1   tag;
 *     union {
 *         u2   const_value_index;
 *         {
 *            u2    type_name_index;
 *            u2    const_name_index;
 *         } enum_const_value;
 *         u2   class_info_index;
 *         annotation   annotation_value;
 *         {
 *              u2  num_values;
 *              element_value   values[num_values];
 *         } array_value;
 *     } value;
 * }
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationElementValue {

    private byte tag;
    private ConstantPool constantPool;
    private short constValueIndex;
    private short typeNameIndex;
    private short constNameIndex;
    private short classInfoIndex;
    private Annotation annotation;
    private List<AnnotationElementValue> arrayValue;

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
                short typeNameIndex = in.readShort();
                short constNameIndex = in.readShort();
                return new AnnotationElementValue(tag, cp, typeNameIndex, constNameIndex);
            case 'c':
                short classInfoIndex = in.readShort();
                return new AnnotationElementValue(tag, cp, classInfoIndex);
            case '@':
                Annotation annotation = Annotation.createAnnotation(in, cp);
                return new AnnotationElementValue(tag, cp, annotation);
            case '[':
                short numValues = in.readShort();
                List<AnnotationElementValue> arrayValue = new ArrayList<AnnotationElementValue>(numValues);
                for (int i = 0; i < numValues; i++) {
                    arrayValue.add( createValue(in, cp) );
                }
                return new AnnotationElementValue(tag, cp, arrayValue);
            default:
                throw new UnsupportedOperationException("Annotation element value tag "+((char)tag)+" not supported");
        }
    }

    public AnnotationElementValue(byte tag, ConstantPool constantPool, short index) {
        this.tag = tag;
        this.constantPool = constantPool;
        if (tag == 'c') {
            this.classInfoIndex = index;
        } else {
            this.constValueIndex = index;
        }
    }

    public AnnotationElementValue(byte tag, ConstantPool constantPool, short typeNameIndex, short constNameIndex) {
        this.tag = tag;
        this.constantPool = constantPool;
        this.typeNameIndex = typeNameIndex;
        this.constNameIndex = constNameIndex;
    }

    public AnnotationElementValue(byte tag, ConstantPool constantPool, List<AnnotationElementValue> arrayValue) {
        this.tag = tag;
        this.constantPool = constantPool;
        this.arrayValue = arrayValue;
    }

    public AnnotationElementValue(byte tag, ConstantPool constantPool, Annotation annotation) {
        this.tag = tag;
        this.constantPool = constantPool;
        this.annotation = annotation;
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

    public boolean isConstValue() {
        return tag == 'B' || tag == 'C' || tag == 'D' || tag == 'F' || tag == 'T' ||
               tag == 'J' || tag == 'S' || tag == 'Z' || tag == 's';
    }

    public boolean isClassValue() {
        return tag == 'c';
    }

    public boolean isEnumValue() {
        return tag == 'e';
    }

    public boolean isAnnotationValue() {
        return tag == '@';
    }

    public boolean isArrayValue() {
        return tag == '[';
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
                return 5;
            case 'c':
                return 3;
            case '@':
                return 1 + annotation.length();
            case '[':
                int length = 3; // tag + num_values
                for (AnnotationElementValue ev : arrayValue) {
                    length += ev.length();
                }
                return length;
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
                out.writeShort(typeNameIndex);
                out.writeShort(constNameIndex);
                return;
            case 'c':
                out.writeShort(classInfoIndex);
                return;
            case '@':
                annotation.dump(out);
                return;
            case '[':
                out.writeShort(arrayValue.size());
                for (AnnotationElementValue ev : arrayValue) {
                    ev.dump(out);
                }
                return;
            default:
                throw new UnsupportedOperationException("Annotation element value tag "+((char)tag)+" not supported");
        }
    }

    public AnnotationElementValue copy() {
        return null;
    }


}
