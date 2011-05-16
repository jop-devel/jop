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

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Visitor;

import java.util.Collection;

/**
 * This class should be the base class for all additional BCEL attribute classes, so
 * that they can be visited by a ClassElementVisitor.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class CustomAttribute extends Attribute {

    public static void registerDefaultReader() {
        Attribute.addAttributeReader(EnclosingMethodReader.ATTRIBUTE_NAME, new EnclosingMethodReader());

        for (String name : AnnotationReader.ATTRIBUTE_NAMES) {
            Attribute.addAttributeReader(name, new AnnotationReader(name));
        }

        Attribute.addAttributeReader(StackMapTableReader.ATTRIBUTE_NAME, new StackMapTableReader());

        // SourceDebugExtension and LocalVariableTypeTable is not handled..
    }

    protected CustomAttribute(byte tag, int name_index, int length, ConstantPool constant_pool) {
        super(tag, name_index, length, constant_pool);
    }

    @Override
    public void accept(Visitor v) {
    }

    public String getName() {
        return constant_pool.getConstant(name_index).toString();
    }

    /**
     * If this attribute references any classes they must be returned here.
     * @return a list of classnames this attribute references.
     */
    public String[] getReferencedClassNames() {
        return null;
    }

    public Collection<Integer> getConstantPoolIDs() {
        return null;
    }
}
