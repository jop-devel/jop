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

import com.jopdesign.common.ClassInfo;
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

    public ClassInfo getClassInfo() {
        if (classRef == null) {
            return fieldInfo.getClassInfo();
        }
        return classRef.getClassInfo();
    }

    public FieldInfo getFieldInfo() {
        if (fieldInfo == null) {
            ClassInfo cls = classRef.getClassInfo();
            if (cls != null) {
                // try to find the field info in the class or its superclasses
                fieldInfo = cls.getFieldInfoInherited(name, true);
            }
        }
        return fieldInfo;
    }

    public String getName() {
        return fieldInfo != null ? fieldInfo.getShortName() : name;
    }

    public Type getType() {
        if (fieldInfo == null && type == null) {
            ClassInfo classInfo = classRef.getClassInfo();
            if (classInfo != null) {
                fieldInfo = classInfo.getFieldInfoInherited(name, true);
            }
        }
        if (fieldInfo != null) return fieldInfo.getType();
        return type;
    }

    public String toString() {
        return MemberID.getMemberID(getClassName(),getName());
    }
}
