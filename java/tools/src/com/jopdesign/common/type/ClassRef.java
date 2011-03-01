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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.misc.ClassInfoNotFoundException;
import com.jopdesign.common.misc.Ternary;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

/**
 * A container of a class reference.
 * Holds either a ClassInfo object or a classname with some infos if the
 * classInfo has not been loaded for some reason.
 *
 * @author Sitefan Hepp (stefan@stefant.org)
 */
public class ClassRef {

    private ClassInfo classInfo;

    private final String className;
    private final Ternary anInterface;
    private final boolean arrayClass;

    public ClassRef(ClassInfo classInfo) {
        this.classInfo = classInfo;
        anInterface = classInfo.isInterface() ? Ternary.TRUE : Ternary.FALSE;
        className = null;
        // we have no classInfo for arrays
        arrayClass = false;
    }

    public ClassRef(String className) {
        this.className = className;
        anInterface = Ternary.UNKNOWN;
        arrayClass = className.startsWith("[");
    }

    public ClassRef(String className, boolean anInterface) {
        this.className = className;
        this.anInterface = Ternary.valueOf(anInterface);
        arrayClass = className.startsWith("[");
    }

    public ClassInfo getClassInfo() {
        // TODO what shall we do with array classes? Ignore for now ..
        if ( classInfo == null && !arrayClass ) {
            try {
                classInfo = AppInfo.getSingleton().getClassInfo(className,false);
            } catch (ClassInfoNotFoundException ignored) {
                return null;
            }
        }
        return classInfo;
    }

    public String getClassName() {
        return classInfo != null ? classInfo.getClassName() : className;
    }

    public Ternary isInterface() {
        return anInterface;
    }

    public boolean isArray() {
        return arrayClass;
    }

    public boolean isNative() {
        return AppInfo.getSingleton().isNative(getClassName());
    }

    public ReferenceType getType() {
        if ( arrayClass ) {
            int dim = className.lastIndexOf('[') + 1;
            return new ArrayType(Type.getType(className.substring(dim)),dim);
        }
        return new ObjectType(getClassName());
    }

    public String toString() {
        return getClassName();
    }

}
