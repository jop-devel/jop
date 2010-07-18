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

import com.jopdesign.common.FieldInfo;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class FieldRef {
    private ClassRef classRef;
    private String name;
    private Type type;
    private FieldInfo fieldInfo;

    public FieldRef(ClassRef classRef, String name, Type type) {
        this.classRef = classRef;
        this.name = name;
        this.type = type;
    }

    public FieldRef(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    public ClassRef getClassRef() {
        if ( classRef == null ) {
            classRef = fieldInfo.getClassInfo().getClassRef();
        }
        return classRef;
    }

    public String getClassName() {
        if (classRef == null ) {
            return fieldInfo.getClassInfo().getClassName();
        }
        return classRef.getClassName();
    }

    public FieldInfo getFieldInfo() {
        // TODO if null, try loading using AppInfo
        return fieldInfo;
    }

    public String getName() {
        return fieldInfo != null ? fieldInfo.getName() : name;
    }

    public Type getType() {
        return fieldInfo != null ? fieldInfo.getType() : type;
    }

    public String toString() {
        return Signature.getSignature(getClassName(),getName());
    }
}
