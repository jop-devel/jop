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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for RuntimeVisibleAnnotations and RuntimeInvisibleAnnotations, as defined in
 * http://java.sun.com/docs/books/jvms/second_edition/ClassFileFormat-Java5.pdf
 * including support for custom atomic attribute.
 *
 * @author Peter Hilber (peter@hilber.name)
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationAttribute extends CustomAttribute {

    /**
     * Annotation Type Tag for RTTM Atomic annotation
     */
    public static final String ATOMIC_TAG_NAME = "Lrttm/atomic;";

    /**
     * Annotation Type Tag for Unused Member flag
     */
    public static final String UNUSED_TAG_NAME = "Lcom/jopdesign/unused;";

    private boolean visible;
    private List<Annotation> annotations;

    public AnnotationAttribute(int name_index, int length, ConstantPool constant_pool,
                               boolean visible, int initialNumAnnotations)
    {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);
        this.visible = visible;
        annotations = new ArrayList<Annotation>(initialNumAnnotations);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Custom annotations support
    // TODO maybe move this to a separate class/to MemberInfo/MethodInfo ??
    //////////////////////////////////////////////////////////////////////////////

    public boolean hasAtomicAnnotation() {
        return findAnnotation(ATOMIC_TAG_NAME) != null;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Generic annotations methods
    //////////////////////////////////////////////////////////////////////////////

    /**
     * @return true if this attribute is a {@value AnnotationReader#VISIBLE_ANNOTATION_NAME} attribute
     */
    public boolean isVisible() {
        return visible;
    }

    public int getNumAnnotations() {
        return annotations.size();
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(Annotation a) {
        annotations.add(a);
    }

    public void removeAnnotation(Annotation a) {
        annotations.remove(a);
    }

    public Annotation findAnnotation(String typeName) {
        for (Annotation a : annotations) {
            if (typeName.equals(a.getTypeName())) {
                return a;
            }
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Common attribute methods
    //////////////////////////////////////////////////////////////////////////////

    @Override
    public void dump(DataOutputStream file) throws IOException {
        updateLength();
        super.dump(file);
        file.writeShort(getNumAnnotations());
        for (Annotation a : annotations) {
            a.dump(file);
        }
    }

    @Override
    public Attribute copy(ConstantPool _constant_pool) {
        AnnotationAttribute attribute = new AnnotationAttribute(name_index, length, _constant_pool,
                visible, annotations.size());
        for (Annotation a : annotations) {
            attribute.addAnnotation(a.copy());
        }
        return attribute;
    }

    public void updateLength() {
        int l = 2;
        for (Annotation a : annotations) {
            l += a.length();
        }
        setLength(l);
    }

}
