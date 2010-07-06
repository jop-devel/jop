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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class AppInfo {

    private ClassPath classPath;
    private Map<String,ClassInfo> classes;

    private Set<MemberInfo> roots;
    private MethodInfo mainMethod;

    private boolean ignoreMissing;
    private boolean loadNatives;
    private boolean loadLibraries;
    private Set<String> nativeClasses;
    private Set<String> libraryClasses;
    private Set<String> ignoredClasses;

    private Map<String, CustomValueManager> infoManagers;
    private Map<String,CustomKey> registeredKeys;

    public static class ClassInfoNotFoundException extends Exception {
        public ClassInfoNotFoundException() {
        }

        public ClassInfoNotFoundException(String message) {
            super(message);
        }

        public ClassInfoNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public ClassInfoNotFoundException(Throwable cause) {
            super(cause);
        }
    }

    public static class CustomKey  {
        private String keyname;
        private int id;

        private CustomKey(String keyname, int id) {
            this.keyname = keyname;
            this.id = id;
        }

        public String getKeyname() {
            return keyname;
        }

        public int getId() {
            return id;
        }
    }

    public AppInfo(ClassPath classPath) {
        this.classPath = classPath;

        ignoreMissing = false;
        loadNatives = false;
        loadLibraries = true;

        classes = new HashMap<String, ClassInfo>();
        roots = new HashSet<MemberInfo>();
        nativeClasses = new HashSet<String>(1);
        libraryClasses = new HashSet<String>(1);
        ignoredClasses = new HashSet<String>(1);

        infoManagers = new HashMap<String, CustomValueManager>(1);
        registeredKeys = new HashMap<String, CustomKey>();
    }

    public CustomValueManager registerManager(String key, CustomValueManager valueManager) {
        valueManager.registerManager(this);
        return infoManagers.put(key, valueManager);
    }

    public CustomValueManager getManager(String key) {
        return infoManagers.get(key);
    }

    public CustomKey registerKey(String key) {

        // check if exists
        CustomKey k = registeredKeys.get(key);
        if ( k != null ) {
            return k;
        }

        k = new CustomKey(key, registeredKeys.size());
        registeredKeys.put(key, k);
        return k;
    }

    public CustomKey getRegisteredKey(String key) {
        return registeredKeys.get(key);
    }

    public void clearKey(CustomKey key) {
        clearKey(key, true, true, true);
    }

    public void clearKey(CustomKey key, boolean fromClassInfos, boolean fromClassMembers, boolean fromCode) {
        for ( ClassInfo cls : classes.values() ) {
            if ( fromClassInfos ) {
                cls.removeCustomValue(key);
            }
            if ( fromClassMembers ) {
                for ( FieldInfo field : cls.getFields() ) {
                    field.removeCustomValue(key);
                }
            }
            if ( fromClassMembers || fromCode ) {
                for ( MethodInfo method : cls.getMethods() ) {
                    if ( fromClassMembers ) {
                        method.removeCustomValue(key);
                    }
                    if ( fromCode ) {
                        // TODO implement
                    }
                }
            }
        }
    }

    public ClassPath getClassPath() {
        return classPath;
    }

    /**
     * Set the new classPath, overwriting the old one.
     * ClassInfos are not reloaded, use {@link #reloadClasses(boolean)} for that.
     *
     * @param classPath the new classpath.
     */
    public void setClassPath(ClassPath classPath) {
        this.classPath = classPath;
    }

    /**
     * Try to load a classInfo.
     * If the class is already loaded, return the existing classInfo.
     * If the class is excluded, return null.
     * If the class does not exist but should be loaded, print an error message and exit.
     *
     * @see #loadClass(String, boolean, boolean)
     * @param className the fully qualified name of the class.
     * @return the classInfo for the classname or null if excluded.
     */
    public ClassInfo loadClass(String className) {
        ClassInfo info = null;
        try {
            info = loadClass(className, false, false);
        } catch (ClassInfoNotFoundException e) {
            System.out.println("Error loading required class '"+className+"': " + e.getMessage());
            System.exit(4);
        }
        return info;
    }

    /**
     * Try to load a classInfo.
     * If a class is excluded (native, library, ignored), return null.
     * If a class is not found or if loading failed and the class is not excluded, or if it is
     * required, an exception is thrown.
     * If a class is not required and ignoreMissing is true, no exception will be thrown.
     *
     * @param className the fully qualified name of the class.
     * @param required if true, throw an exception even if the class is excluded or ignoreMissing is true.
     * @param reload if true, reload the classInfo if it already exists.
     * @return the classInfo for the classname, or null if excluded.
     * @throws ClassInfoNotFoundException if the class could not be loaded and is not excluded.
     */
    public ClassInfo loadClass(String className, boolean required, boolean reload) throws ClassInfoNotFoundException {

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
                throw new ClassInfoNotFoundException("Class '"+className+"' is excluded but required.");
            }
            return null;
        }

        return performLoadClass(className, required);
    }

    public ClassInfo createClass(String className, ClassRef superClass) {
        return null;
    }

    public void removeClass(ClassInfo classInfo) {
        classes.remove(classInfo.getClassName());

        for ( CustomValueManager mgr : infoManagers.values() ) {
            mgr.onRemoveClass(classInfo);
        }
    }

    public void addRoot(ClassInfo classInfo) {
        roots.add(classInfo);
    }

    public void addRoot(MethodInfo methodInfo) {
        roots.add(methodInfo);
    }

    public Collection<ClassInfo> getRootClasses() {
        // TODO maintain rootClasses as field?
        Map<String,ClassInfo> rootClasses = new HashMap<String, ClassInfo>();
        for (MemberInfo root : roots) {
            rootClasses.put(root.getClassInfo().getClassName(), root.getClassInfo());
        }
        return rootClasses.values();
    }

    public Set<MemberInfo> getRoots() {
        return Collections.unmodifiableSet( roots );
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

    public void addNative(String nativeClass) {
        nativeClasses.add(nativeClass);
    }

    public void addLibrary(String libraryClass) {
        libraryClasses.add(libraryClass);
    }

    public void addIgnored(String ignoredClass) {
        ignoredClasses.add(ignoredClass);
    }

    public void setIgnoreMissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }

    public boolean doIgnoreMissing() {
        return ignoreMissing;
    }

    public boolean doLoadLibraries() {
        return loadLibraries;
    }

    public void setLoadLibraries(boolean loadLibraries) {
        this.loadLibraries = loadLibraries;
    }

    public boolean doLoadNatives() {
        return loadNatives;
    }

    /**
     * Set to true to load native classes too.
     *
     * @param loadNatives if true, load native classes too.
     */
    public void setLoadNatives(boolean loadNatives) {
        this.loadNatives = loadNatives;
    }

    /**
     * Remove all classInfos.
     *
     * @param clearRoots if true, clear list of roots and main method as well, else keep the root classes in the class list.
     */
    public void clear(boolean clearRoots) {

        for (CustomValueManager mgr : infoManagers.values()) {
            mgr.onClearAppInfo(this);
        }

        classes.clear();
        
        if ( clearRoots ) {
            roots.clear();
            mainMethod = null;
        } else {
            // re-add all root classes
            for (MemberInfo root : roots) {
                classes.put(root.getClassInfo().getClassName(), root.getClassInfo());
            }
        }
    }

    /**
     * Reload all currently loaded classInfos from disk, using the current classpath.
     * @param checkExcludes if true, reevaluate excludes, else reload all classes even if excluded.
     * @throws ClassInfoNotFoundException if a class could not be reloaded.
     */
    public void reloadClasses(boolean checkExcludes) throws ClassInfoNotFoundException {

        List<String> clsNames = new LinkedList<String>(classes.keySet());

        clear(false);

        for (String clsName : clsNames ) {

            if ( checkExcludes && isExcluded(clsName) ) {
                continue;
            }

            ClassInfo cls = performLoadClass(clsName, true);
        }

        // reload mainMethod
        if ( mainMethod != null ) {
            ClassInfo mainClass = classes.get(mainMethod.getClassInfo().getClassName());
            if ( mainClass == null ) {
                mainMethod = null;
                throw new ClassInfoNotFoundException("Could not find main class.");
            }

            mainMethod = mainClass.getMethodInfo(mainMethod.getSignature().getMemberSignature());
            if (mainMethod == null) {
                throw new ClassInfoNotFoundException("Could not find main method in main class");
            }
        }
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
     * @param className fully qualified name of the class to get.
     * @param required if true, always throw an exception if not loaded.
     * @return the classInfo, or null if excluded or ignored and not required.
     */
    public ClassInfo getClass(String className, boolean required) throws ClassInfoNotFoundException {
        ClassInfo cls = classes.get(className);
        if ( cls == null && required ) {
            throw new ClassInfoNotFoundException("Required class '"+className+"' not loaded.");
        }
        if ( cls == null && !ignoreMissing && !isExcluded(className) ) {
            throw new ClassInfoNotFoundException("Requested class '"+className+"' is missing and not excluded.");            
        }
        return cls;
    }

    public Collection<ClassInfo> getClassInfos() {
        return classes.values();
    }

    public ClassRef getClassRef(String className) {

        return null;
    }

    public MethodRef getMethodRef(Signature methodSignature) {

        return null;
    }

    public boolean isNative(String className) {
        for (String s : nativeClasses) {
            if (className.equals(s) || className.startsWith(s + ".")) {
                return true;
            }
        }
        return false;
    }

    public boolean isLibrary(String className) {
        for (String s : libraryClasses) {
            if (className.equals(s) || className.startsWith(s + ".")) {
                return true;
            }
        }
        return false;
    }

    public boolean isIgnored(String className) {
        for (String s : ignoredClasses) {
            if (className.equals(s) || className.startsWith(s + ".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if the classname is excluded from loading,
     * either because it is a native or library class (and loading is disabled for them)
     * or if the class is ignored.
     *
     * @param className the fully qualified class name to check.
     * @return true if it should be excluded from loading.
     */
    public boolean isExcluded(String className) {
        if ( !loadNatives && isNative(className) ) {
            return true;
        }
        if ( !loadLibraries && isLibrary(className) ) {
            return true;
        }
        return isIgnored(className);
    }

    /**
     * Get number of registered keys. Should be accessed only by MemberInfo.
     *
     * @return number of currently registered keys.
     */
    int getRegisteredKeyCount() {
        return registeredKeys.size();
    }
    
    private ClassInfo performLoadClass(String className, boolean required) throws ClassInfoNotFoundException {

        // try to load the class
        ClassInfo cls = tryLoadClass(className);
        if ( cls == null ) {
            // class is not excluded, but loading failed
            if ( required || !ignoreMissing ) {
                throw new ClassInfoNotFoundException("Class '"+className+"' could not be loaded.");
            }
        } else {
            for (CustomValueManager mgr : infoManagers.values()) {
                mgr.onLoadClass(cls);
            }
        }

        return cls;
    }

    private ClassInfo tryLoadClass(String className) {
        
        return null;
    }

}
