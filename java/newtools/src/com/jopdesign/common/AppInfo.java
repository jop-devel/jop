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

import org.apache.bcel.util.ClassPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppInfo {

    private Map<String,ClassInfo> classes;
    private List<BaseInfo> roots;
    private MethodInfo entryMethod;
    private ClassPath classPath;

    public AppInfo(ClassPath classPath) {
        this.classPath = classPath;
        classes = new HashMap<String, ClassInfo>();
    }

    public ClassPath getClassPath() {
        return classPath;
    }

    public void setClassPath(ClassPath classPath) {
        this.classPath = classPath;
    }

    public ClassInfo loadClassInfo(String className) {

        return null;
    }

    public void removeClassInfo(ClassInfo classInfo) {

    }

    public void addRoot(ClassInfo classInfo) {

    }

    public void addRoot(MethodInfo methodInfo) {

    }

    public void setEntryMethod(MethodInfo entry) {
        
    }

    public void clear(boolean clearRoots) {

    }

    public ClassInfo getClassInfo(String className) {
        return classes.get(className);
    }

     

}
