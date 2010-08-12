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

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnnotationDefault extends CustomAttribute {

    private AnnotationElementValue value;

    public AnnotationDefault(int name_index, int length, ConstantPool constant_pool, AnnotationElementValue value) {
        super(Constants.ATTR_UNKNOWN, name_index, length, constant_pool);

        this.value = value;
    }

    public AnnotationElementValue getValue() {
        return value;
    }

    public void setValue(AnnotationElementValue value) {
        this.value = value;
    }

    @Override
    public void dump(DataOutputStream file) throws IOException {
        setLength(value.length());
        super.dump(file);
        value.dump(file);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Attribute copy(ConstantPool _constant_pool) {
        return new AnnotationDefault(name_index, length, _constant_pool, value.copy());
    }
}
