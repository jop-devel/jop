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

import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.util.ClassPath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppInfo {

    private ClassPath classPath;
    private Map<String,ClassInfo> classes;

    private Set<BaseInfo> roots;
    private MethodInfo mainMethod;

    private boolean ignoreMissing;
    private Set<String> excludeNative;
    private Set<String> excludeLibrary;
    private Set<String> excludeIgnored;

    public AppInfo(ClassPath classPath) {
        this.classPath = classPath;

        classes = new HashMap<String, ClassInfo>();
        roots = new HashSet<BaseInfo>();
        excludeNative = new HashSet<String>(1);
        excludeLibrary = new HashSet<String>(0);
        excludeIgnored = new HashSet<String>(0);
    }

    public ClassPath getClassPath() {
        return classPath;
    }

    public void setClassPath(ClassPath classPath) {
        this.classPath = classPath;
    }

    public ClassInfo loadClass(String className) {
        return loadClass(className, false, false);
    }

    /**
     * Try to load a classInfo.
     * If a class is excluded (native, library, ignored), return null.
     * If a class is not found or if loading failed and the class is not excluded, or if it is
     * required, an exception is thrown.
     *
     * @param className the fully qualified name of the class.
     * @param required if true, throw an exception even if the class is excluded or ignoreMissing is true.
     * @param reload if true, reload the classInfo if it already exists.
     * @return
     */
    public ClassInfo loadClass(String className, boolean required, boolean reload) {

        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            if ( reload ) {
                removeClass(cls);
            } else {
                return cls;
            }
        }

        // check if it is excluded (and not required)
        if ( isExcluded(className) ) {
            if ( required ) {
                // throw exception
            }
            return null;
        }

        // try to load the class
        cls = tryLoadClass(className);
        if ( cls == null ) {
            // class is not excluded, but loading failed
            if ( required || !ignoreMissing ) {
                // throw exception
            }
        }

        return cls;
    }

    public boolean isExcluded(String className) {
        return false;
    }

    private ClassInfo tryLoadClass(String className) {
        return null;
    }

    public ClassInfo createClass(String className, ClassRef superClass) {
        return null;
    }

    public void removeClass(ClassInfo classInfo) {

    }

    public void addRoot(ClassInfo classInfo) {
        roots.add(classInfo);
    }

    public void addRoot(MethodInfo methodInfo) {
        roots.add(methodInfo);
    }

    public void setMainMethod(MethodInfo main) {
        if ( main != null ) {
            addRoot(main.getClassInfo());
        }
        mainMethod = main;
    }

    public MethodInfo getMainMethod() {
        return mainMethod;
    }

    public void excludeNative(String nativeClass) {
        excludeNative.add(nativeClass);
    }

    public void excludeLibrary(String libraryClass) {
        excludeLibrary.add(libraryClass);
    }

    public void excludeIgnored(String ignoreClass) {
        excludeIgnored.add(ignoreClass);
    }

    public void setIgnoreMissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }

    public boolean doIgnoreMissing() {
        return ignoreMissing;
    }

    public void clear(boolean clearRoots) {

    }

    /**
     * Get an already loaded class, or null if the class has not been loaded.
     *
     * @param className the classname of the class.
     * @return the classInfo of the class or null if it has not been loaded.
     */
    public ClassInfo getClass(String className) {
        return classes.get(className);
    }

    /**
     * Get an already loaded class, or null if the class has been excluded, or
     * throw an exception if the class is either required or not excluded.
     *
     * @param className
     * @param required
     * @return
     */
    public ClassInfo getClass(String className, boolean required) {

        return null;
    }

    public ClassRef getClassRef(String className) {

        return null;
    }

    public MethodRef getMethodRef(Signature methodSignature) {

        return null;
    }

}
