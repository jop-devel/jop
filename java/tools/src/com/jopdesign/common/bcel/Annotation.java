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
import java.util.ArrayList;
import java.util.List;

/**
 * Class for an annotation entry in an annotation attribute.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Annotation {

    private int typeIndex;
    private ConstantPool constantPool;
    private final List<AnnotationElement> elements;

    public static Annotation createAnnotation(DataInputStream in, ConstantPool cp) throws IOException {
        int typeIndex = in.readUnsignedShort();
        int numElements = in.readUnsignedShort();
        Annotation a = new Annotation(typeIndex, cp, numElements);
        for (int i = 0; i < numElements; i++) {
            a.addElement(AnnotationElement.createElement(in, cp));
        }
        return a;
    }

    public Annotation(int typeIndex, ConstantPool constantPool, int initialElements) {
        this.typeIndex = typeIndex;
        this.constantPool = constantPool;
        elements = new ArrayList<AnnotationElement>(initialElements);
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    /**
     * @return index of a ConstantUtf8 entry.
     */
    public int getTypeIndex() {
        return typeIndex;
    }

    /**
     * @param typeIndex index of a ConstantUtf8 entry.
     */
    public void setTypeIndex(short typeIndex) {
        this.typeIndex = typeIndex;
    }

    public String getTypeName() {
        return ((ConstantUtf8)constantPool.getConstant(typeIndex)).getBytes();
    }

    public int getNumElements() {
        return elements.size();
    }

    public List<AnnotationElement> getElements() {
        return elements;
    }

    public void addElement(AnnotationElement element) {
        elements.add(element);
    }

    public void removeElement(AnnotationElement element) {
        elements.remove(element);
    }

    public int length() {
        int l = 4;
        for (AnnotationElement e : elements) {
            l += e.length();
        }
        return l;
    }

    public void dump(DataOutputStream out) throws IOException {
        out.writeShort(typeIndex);
        out.writeShort(getNumElements());
        for (AnnotationElement e : elements) {
            e.dump(out);
        }
    }

    public Annotation copy() {
        Annotation a = new Annotation(typeIndex, constantPool, getNumElements());
        for (AnnotationElement e : elements) {
            a.addElement(e.copy());
        }
        return a;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "typeIndex=" + typeIndex +
                ", elements=" + elements +
                '}';
    }
}
