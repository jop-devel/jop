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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.misc.ClassInfoNotFoundException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.Repository;

/**
 * An interface class to redirect requests to BCEL Repository to AppInfo.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class BcelRepositoryWrapper implements Repository {

    public void storeClass(JavaClass clazz) {
        // Not implemented (and not used by BCEL ??)
    }

    public void removeClass(JavaClass clazz) {
        ClassInfo cls = AppInfo.getSingleton().getClassInfo(clazz.getClassName());
        if ( cls != null ) {
            AppInfo.getSingleton().removeClass(cls);
        }
    }

    public JavaClass findClass(String className) {
        ClassInfo cls = AppInfo.getSingleton().getClassInfo(className);
        if ( cls != null ) {
            return cls.getJavaClass();
        }
        return null;
    }

    public JavaClass loadClass(String className) throws ClassNotFoundException {
        ClassInfo cls = AppInfo.getSingleton().loadClass(className);
        if ( cls != null ) {
            return cls.getJavaClass();
        }
        return null;
    }

    public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
        ClassInfo cls;
        try {
            cls = AppInfo.getSingleton().loadClass(clazz.getName(), false, false);
        } catch (ClassInfoNotFoundException e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }
        if ( cls != null ) {
            return cls.getJavaClass();
        }
        return null;
    }

    public void clear() {
        AppInfo.getSingleton().clear(false);
    }

    public ClassPath getClassPath() {
        return AppInfo.getSingleton().getClassPath();
    }
}
