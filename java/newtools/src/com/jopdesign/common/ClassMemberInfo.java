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

package com.jopdesign.common;

import com.jopdesign.common.type.Descriptor;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.generic.FieldGenOrMethodGen;
import org.apache.bcel.generic.Type;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class ClassMemberInfo extends MemberInfo {

    private final ClassInfo classInfo;
    private final FieldGenOrMethodGen classMember;

    public ClassMemberInfo(ClassInfo classInfo, FieldGenOrMethodGen classMember) {
        super(classMember);
        this.classInfo = classInfo;
        this.classMember = classMember;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    @Override
    public String getSimpleName() {
        return classMember.getName();
    }

    public Type getType() {
        return classMember.getType();
    }

    public abstract Descriptor getDescriptor();

    @Override
    public Attribute[] getAttributes() {
        return classMember.getAttributes();
    }

    @Override
    public void addAttribute(Attribute a) {
        classMember.addAttribute(a);
    }

    @Override
    public void removeAttribute(Attribute a) {
        classMember.removeAttribute(a);
    }

}
