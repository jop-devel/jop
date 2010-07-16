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
import com.jopdesign.common.misc.Ternary;

/**
 * A container of a class reference.
 * Holds either a ClassInfo object or a classname with some infos if the
 * classInfo has not been loaded for some reason.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassRef {

    private ClassInfo classInfo;

    private String className;
    private final Ternary anInterface;

    public ClassRef(ClassInfo classInfo) {
        this.classInfo = classInfo;
        anInterface = classInfo.isInterface() ? Ternary.TRUE : Ternary.FALSE;
    }

    public ClassRef(String className) {
        this.className = className;
        anInterface = Ternary.UNKNOWN;
    }

    public ClassRef(String className, boolean anInterface) {
        this.className = className;
        this.anInterface = anInterface ? Ternary.TRUE : Ternary.FALSE;
    }

    public ClassInfo getClassInfo() {
        if ( classInfo == null ) {
            classInfo = AppInfo.getSingleton().getClass(className);
        }
        return classInfo;
    }

    public String getClassName() {
        return classInfo != null ? classInfo.getClassName() : className;
    }

    /**
     * Check if this class is an interface.
     *
     * @return 1 if it is an interface, 0 if not, and -1 if it is unknown.
     */
    public Ternary isInterface() {
        return anInterface;
    }

    public TypeInfo getTypeInfo() {
        return new ObjectType(this);
    }

    public String toString() {
        return getClassName();
    }
}
