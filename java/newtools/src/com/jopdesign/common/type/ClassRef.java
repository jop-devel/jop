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
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

import java.util.Arrays;

/**
 * A container of a class reference.
 * Holds either a ClassInfo object or a classname with some infos if the
 * classInfo has not been loaded for some reason.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassRef {

    private ClassInfo classInfo;

    private final String className;
    private final Ternary anInterface;
    private final boolean arrayClass;
    private final Ternary innerClass;
    private final String[] outerClasses;

    public ClassRef(ClassInfo classInfo) {
        this.classInfo = classInfo;
        anInterface = classInfo.isInterface() ? Ternary.TRUE : Ternary.FALSE;
        className = null;
        innerClass = Ternary.valueOf(classInfo.isInnerclass());
        outerClasses = null;
        // we have no classInfo for arrays
        arrayClass = false;
    }

    public ClassRef(String className) {
        this.className = className;
        anInterface = Ternary.UNKNOWN;
        innerClass = Ternary.UNKNOWN;
        outerClasses = null;
        arrayClass = className.startsWith("[");
    }

    public ClassRef(String className, boolean anInterface) {
        this.className = className;
        this.anInterface = Ternary.valueOf(anInterface);
        innerClass = Ternary.UNKNOWN;
        outerClasses = null;
        arrayClass = className.startsWith("[");
    }

    public ClassRef(String className, String[] outerClasses) {
        this.className = className;
        this.outerClasses = outerClasses;
        anInterface = Ternary.UNKNOWN;
        innerClass = Ternary.valueOf( outerClasses != null );
        arrayClass = className.startsWith("[");
    }

    public ClassRef(String className, boolean anInterface, String[] outerClasses) {
        this.className = className;
        this.anInterface = Ternary.valueOf(anInterface);
        this.outerClasses = outerClasses;
        innerClass = Ternary.valueOf( outerClasses != null );
        arrayClass = className.startsWith("[");
    }

    public ClassInfo getClassInfo() {
        if ( classInfo == null && !arrayClass ) {
            classInfo = AppInfo.getSingleton().getClassInfo(className);
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

    public Ternary isInnerClass() {
        return innerClass;
    }

    public boolean isMemberInnerclass() {
        if ( classInfo != null ) {
            return classInfo.isMemberInnerclass();
        }
        return outerClasses != null && outerClasses.length > 0;
    }

    /**
     * Get a reference to the outer class of this class.
     *
     * @return a reference to the outer class, or null if this is not an inner class or if
     *         the outer class is not known or if this is a non-member inner class. 
     */
    public ClassRef getOuterClassRef() {
        if ( innerClass != Ternary.TRUE ) {
            return null;
        }
        if ( classInfo != null ) {
            return classInfo.getOuterClassRef();
        }

        if (outerClasses.length > 1) {
            return AppInfo.getSingleton().getClassRef(outerClasses[outerClasses.length-1],
                   Arrays.copyOf(outerClasses, outerClasses.length-1) );
        } else if (outerClasses.length == 1) {
            return AppInfo.getSingleton().getClassRef(outerClasses[0], null);
        } else {
            // outerClass of non-member innerclass not known!
            return null;
        }
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
