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
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ParameterAnnotationAttribute extends CustomAttribute {

    private boolean visible;
    private List<Annotation>[] parameterAnnotations;

    public ParameterAnnotationAttribute(int name_index, int length, ConstantPool constant_pool,
                                        boolean visible, int numParameters)
    {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);
        this.visible = visible;
        //noinspection unchecked
        parameterAnnotations = (List<Annotation>[])new List[numParameters];
        for (int i=0; i < numParameters; i++) {
            parameterAnnotations[i] = new ArrayList<Annotation>();
        }
    }

    /**
     * @return true if this attribute is a {@value AnnotationReader#VISIBLE_PARAMETER_ANNOTATION_NAME} attribute
     */
    public boolean isVisible() {
        return visible;
    }

    public int getNumParameters() {
        return parameterAnnotations.length;
    }

    public List<Annotation> getAnnotations(int parameter) {
        return parameterAnnotations[parameter];
    }

    public void addAnnotation(int parameter, Annotation annotation) {
        parameterAnnotations[parameter].add(annotation);
    }

    public void removeAnnotation(int parameter, Annotation annotation) {
        parameterAnnotations[parameter].remove(annotation);
    }

    @Override
    public void dump(DataOutputStream file) throws IOException {
        updateLength();
        super.dump(file);
        file.writeByte(getNumParameters());
        for (int i = 0; i < getNumParameters(); i++) {
            file.writeShort(parameterAnnotations[i].size());
            for (Annotation a : parameterAnnotations[i]) {
                a.dump(file);
            }
        }
    }

    @Override
    public Attribute copy(ConstantPool _constant_pool) {
        ParameterAnnotationAttribute attribute =
                new ParameterAnnotationAttribute(name_index, length, _constant_pool, visible, getNumParameters());

        for (int i = 0; i < getNumParameters(); i++) {
            for (Annotation a : parameterAnnotations[i]) {
                attribute.addAnnotation(i, a.copy());
            }
        }

        return attribute;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void updateLength() {
        int l = 1;
        for (int i = 0; i < getNumParameters(); i++) {
            l += 2;
            for (Annotation a : parameterAnnotations[i]) {
                l += a.length();
            }
        }
        setLength(l);
    }
}
