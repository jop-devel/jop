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

import org.apache.bcel.generic.ObjectType;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ObjectTypeInfo extends ReferenceTypeInfo<ObjectType> {

    private final ClassRef classRef;

    public ObjectTypeInfo(ClassRef classRef) {
        super(new ObjectType(classRef.getClassName()));
        this.classRef = classRef;
    }

    public ObjectTypeInfo(ObjectType type) {
        super(type);
        classRef = new ClassRef(type.getClassName());
    }

    public ClassRef getClassRef() {
        return classRef;
    }

}
