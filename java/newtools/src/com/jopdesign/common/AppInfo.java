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

import com.jopdesign.common.graph.ClassVisitor;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.bcel.BcelRepositoryWrapper;
import com.jopdesign.common.misc.ClassInfoNotFoundException;
import com.jopdesign.common.misc.MissingClassError;
import com.jopdesign.common.misc.NamingConflictException;
import com.jopdesign.common.type.ClassRef;
import com.jopdesign.common.type.FieldRef;
import com.jopdesign.common.type.MethodRef;
import com.jopdesign.common.type.Signature;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.util.ClassPath;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The AppInfo class loads, creates and holds ClassInfos, handles all the loading related stuff,
 * manages CustomKeys and modification events, maintains a class hierarchy and provides various
 * methods to get and to iterate over ClassInfos.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public final class AppInfo {

    private static final Logger logger = Logger.getLogger(LogConfig.LOG_STRUCT + ".AppInfo");

    private ClassPath classPath;
    private final Map<String,ClassInfo> classes;

    private final Set<MemberInfo> roots;
    private MethodInfo mainMethod;

    // if true, an invalid or missing (and not excluded) class does not trigger an error
    private boolean ignoreMissingClasses;
    // if true, native classes are loaded too
    private boolean loadNatives;
    // if true, library classes are loaded too
    private boolean loadLibraries;

    private boolean exitOnMissingClass;

    private final Set<String> nativeClasses;
    private final Set<String> libraryClasses;
    private final Set<String> ignoredClasses;

    private final Map<String, AttributeManager> managers;
    private final Map<String,CustomKey> registeredKeys;


    /**
     * A class for custom attribute key.
     * To create a new key, use {@link com.jopdesign.common.AppInfo#registerKey(String)}.
     */
    public static final class CustomKey  {
        private final String keyname;
        private final int id;

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

        public int hashCode() {
            return id;
        }

        public boolean equals(Object o) {
            if ( o instanceof CustomKey ) {
                return ((CustomKey)o).getId() == id;
            }
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Singleton
    //////////////////////////////////////////////////////////////////////////////

    private static final AppInfo singleton;    
    static {
        singleton = new AppInfo();
        Repository.setRepository(new BcelRepositoryWrapper());
    }

    public static AppInfo getSingleton() {
        return singleton; 
    }


    private AppInfo() {
        this.classPath = new ClassPath(".");

        ignoreMissingClasses = false;
        loadNatives = false;
        loadLibraries = true;
        exitOnMissingClass = false;

        classes = new HashMap<String, ClassInfo>();
        roots = new HashSet<MemberInfo>();
        nativeClasses = new HashSet<String>(1);
        libraryClasses = new HashSet<String>(1);
        ignoredClasses = new HashSet<String>(1);

        managers = new HashMap<String, AttributeManager>(1);
        registeredKeys = new HashMap<String, CustomKey>();
    }


    //////////////////////////////////////////////////////////////////////////////
    // AttributeManager and CustomKey management, AppInfo setup stuff
    //////////////////////////////////////////////////////////////////////////////

    public AttributeManager registerManager(String key, AttributeManager manager) {
        manager.registerManager(this);
        return managers.put(key, manager);
    }

    public AttributeManager getManager(String key) {
        return managers.get(key);
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


    //////////////////////////////////////////////////////////////////////////////
    // Methods to create, load, get and remove ClassInfos
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Create a new classInfo. If a class with the same name exists, return the existing classInfo.
     *
     * <p>Note that this does not update the complete class hierarchy. You need to call {@link #reloadClassHierarchy()}
     * after you finished creating and loading classes. SuperClass will be set, but nothing more.</p>
     *
     * @param className the fully qualified name of the class.
     * @param superClass the references to the superclass, or null to use java.lang.Object (except if the new class is
     *   java.lang.Object).
     * @param isInterface true if this class should be an interface.
     * @return a new ClassInfo or the current ClassInfo by the same name if it exists.
     * @throws NamingConflictException if a class with the same name exists, but has a different definition.
     */
    public ClassInfo createClass(String className, ClassRef superClass, boolean isInterface)
            throws NamingConflictException
    {
        String superClassName;
        if (superClass == null) {
            superClassName = "java.lang.Object".equals(className) ? null : "java.lang.Object";
        } else {
            superClassName = superClass.getClassName();
        }

        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            if ( isInterface != cls.isInterface() ||
                 !cls.getSuperClassName().equals(superClassName) )
            {
                throw new NamingConflictException("Class '"+className+
                        "' already exists but has a different definition.");
            }
            return cls;
        }

        cls = createClassInfo(className, superClassName, isInterface);

        // do a "partial" class hierarchy update (i.e. set superClass, but not subclasses of the new class)
        cls.updateClassHierarchy();

        classes.put(className, cls);

        for (AttributeManager mgr : managers.values()) {
            mgr.onCreateClass(cls);
        }

        return cls;
    }

    /**
     * Try to load a classInfo.
     * If the class is already loaded, return the existing classInfo.
     * If the class is excluded, return null.
     * If the class does not exist but should be loaded, throw an {@link MissingClassError}.
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
            handleClassLoadFailure(e.getMessage(), e);
        }
        return info;
    }

    /**
     * Try to load a classInfo.
     * If a class is excluded (native, library, ignored), return null.
     * If a class is not found or if loading failed and the class is not excluded, or if it is
     * required, an exception is thrown.
     * If a class is not required and ignoreMissing is true, no exception will be thrown.
     * To update the class hierarchy relations of the ClassInfos, you need to call
     * {@link #reloadClassHierarchy()} after all classes have been loaded.
     *
     * @param className the fully qualified name of the class.
     * @param required if true, throw an exception even if the class is excluded or ignoreMissing is true.
     * @param reload if true, reload the classInfo if it already exists.
     * @return the classInfo for the classname, or null if excluded.
     * @throws ClassInfoNotFoundException if the class could not be loaded and is not excluded.
     */
    public ClassInfo loadClass(String className, boolean required, boolean reload)
            throws ClassInfoNotFoundException
    {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            if ( reload ) {
                removeClass(cls, false, false);
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

    /**
     * Remove a class from AppInfo.
     *
     * @param classInfo the class to remove.
     * @param purgeClass if true, remove all extensions and inner classes too, remove from implemented interfaces
     *        lists and from InnerClasses of enclosing classes.
     * @param updateHierarchy if true, update the class hierarchy infos now.
     */
    public void removeClass(ClassInfo classInfo, boolean purgeClass, boolean updateHierarchy) {
        classes.remove(classInfo.getClassName());

        if ( purgeClass ) {
            // TODO purge
        }

        for ( AttributeManager mgr : managers.values() ) {
            mgr.onRemoveClass(classInfo);
        }

        if ( updateHierarchy ) {
            classInfo.removeFromClassHierarchy();
        }
    }

    /**
     * Get an already loaded class.
     *
     * This method only returns null if classes are excluded from loading or the class is missing
     * and doIgnoreMissingClasses is set. Else a {@link MissingClassError} is thrown.
     *
     * @see #getClassInfo(String, boolean)
     * @param className the classname of the class.
     * @return the classInfo of the class or null if the class is excluded.
     */
    public ClassInfo getClassInfo(String className) {
        ClassInfo classInfo = null;
        try {
            classInfo = getClassInfo(className, false);
        } catch (ClassInfoNotFoundException e) {
            handleClassLoadFailure(e.getMessage(), e);
        }
        return classInfo;
    }

    /**
     * Get an already loaded class.
     *
     * This method only returns null if classes are excluded from loading or the class is missing
     * and doIgnoreMissingClasses is set. Else an exception is thrown.
     * If required is true, this method never returns null but throws an exception if the classInfo is not found. 
     *
     * @see #getClassInfo(String)
     * @param className fully qualified name of the class to get.
     * @param required if true, always throw an exception if not loaded.
     * @return the classInfo, or null if excluded or not found and ignored and not required.
     * @throws ClassInfoNotFoundException if the class is required but not found or excluded.
     */
    public ClassInfo getClassInfo(String className, boolean required) throws ClassInfoNotFoundException {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            return cls;
        }

        if ( isExcluded(className) ) {
            if ( required ) {
                throw new ClassInfoNotFoundException("Class '"+className+"' is excluded but required.");
            }
            return null;
        }

        // class is null, not excluded, and not loaded on demand, i.e. missing
        if ( required  ) {
            throw new ClassInfoNotFoundException("Required class '"+className+"' not loaded.");
        }
        if ( !ignoreMissingClasses ) {
            throw new ClassInfoNotFoundException("Requested class '"+className+"' is missing and not excluded.");
        }

        return cls;
    }

    /**
     * Remove all classInfos.
     *
     * @param clearRoots if true, clear list of roots and main method as well, else
     *        keep the root classes in the class list.
     */
    public void clear(boolean clearRoots) {

        for (AttributeManager mgr : managers.values()) {
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

            performLoadClass(clsName, false);
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

        reloadClassHierarchy();
    }

    /**
     * Reload all super- and subclass references of all classInfos.
     */
    public void reloadClassHierarchy() {
        for (ClassInfo cls : classes.values()) {
            cls.resetHierarchyInfos();
        }
        for (ClassInfo cls : classes.values()) {
            cls.updateClassHierarchy();
        }
        for (ClassInfo cls : classes.values()) {
            cls.updateCompleteFlag(false);
        }
    }

    public boolean hasClassInfo(String className) {
        return classes.containsKey(className);
    }

    /**
     * Get a collection of all classInfos in this AppInfo.
     * Changes to the AppInfo are visible to the returned collection.
     * You should not modify this collection directly.
     *
     * @return an unmodifiable view of the collection of AppInfos.
     */
    public Collection<ClassInfo> getClassInfos() {
        return Collections.unmodifiableCollection(classes.values());
    }

    public void iterate(ClassVisitor visitor) {
        for (ClassInfo c : classes.values()) {
            if (!visitor.visitClass(c)) {
                return;
            }
            visitor.finishClass(c);
        }
    }

    public void compileAll() {
        for (ClassInfo c : classes.values()) {
            c.compileJavaClass();
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // Helper methods to find classes, fields and methods; Convenience methods
    //////////////////////////////////////////////////////////////////////////////

    public ClassRef getClassRef(String className) {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            return cls.getClassRef();
        }
        return new ClassRef(className);
    }

    public ClassRef getClassRef(String className, boolean isInterface) {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            if ( cls.isInterface() != isInterface ) {
                throw new ClassFormatException("Class '"+className+"' interface flag does not match.");
            }
            return cls.getClassRef();
        }

        return new ClassRef(className, isInterface);
    }

    /**
     * Get a reference to a class.
     *
     * @param className the name of the class.
     * @param outerClasses a list of outer classnames, or null if the class is not an inner class.
     * @return a reference to a class.
     */
    public ClassRef getClassRef(String className, String[] outerClasses) {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            return cls.getClassRef();
        }

        return new ClassRef(className, outerClasses);
    }

    /**
     * Get a reference to a class.
     *
     * @param className the name of the class.
     * @param isInterface true if the class is an interface.
     * @param outerClasses a list of outer classnames, or null if the class is not an inner class.
     * @return a reference to a class.
     */
    public ClassRef getClassRef(String className, boolean isInterface, String[] outerClasses) {
        ClassInfo cls = classes.get(className);
        if ( cls != null ) {
            if ( cls.isInterface() != isInterface ) {
                throw new ClassFormatException("Class '"+className+"' interface flag does not match.");
            }
            return cls.getClassRef();
        }

        return new ClassRef(className, isInterface, outerClasses);
    }


    /**
     * Get a reference to a method using the given signature.
     * If you already have a classRef, use {@link #getMethodRef(ClassRef, Signature)}
     * instead.
     *
     * @param signature the signature of the method
     * @param isInterfaceMethod true if the class is an interface.
     * @return a method reference with or without MethodInfo or ClassInfo.
     */
    public MethodRef getMethodRef(Signature signature, boolean isInterfaceMethod) {
        ClassInfo cls = classes.get(signature.getClassName());
        ClassRef clsRef;
        if ( cls != null ) {
            if ( cls.isInterface() != isInterfaceMethod ) {
                throw new ClassFormatException("Class '"+cls.getClassName()+"' interface flag does not match.");
            }
            clsRef = cls.getClassRef();
        } else {
            clsRef = new ClassRef(signature.getClassName(),isInterfaceMethod);
        }
        return getMethodRef(clsRef, signature);
    }

    /**
     * Get a reference to a method using the given signature.
     *
     * @param classRef The reference to the class or interface containing the method.
     * @param signature The signature of the method. Only memberName and memberDescriptor are used.
     * @return A method reference with or without MethodInfo or ClassInfo.
     */
    public MethodRef getMethodRef(ClassRef classRef, Signature signature) {
        ClassInfo classInfo = classRef.getClassInfo();
        if ( classInfo != null ) {
            MethodInfo method = classInfo.getMethodInfo(signature);
            if ( method == null ) {
                return new MethodRef(classInfo.getClassRef(), signature.getMemberName(),
                        signature.getMemberDescriptor());
            } else {
                return method.getMethodRef();
            }
        }

        return new MethodRef(classRef, signature.getMemberName(), signature.getMemberDescriptor());
    }

    /**
     * Get a reference to a field using the given signature.
     *
     * @param signature The signature of the field.
     * @return A field reference with or without FieldInfo or ClassInfo.
     */
    public FieldRef getFieldRef(Signature signature) {
        ClassInfo cls = classes.get(signature.getClassName());
        ClassRef clsRef;
        if ( cls != null ) {
            clsRef = cls.getClassRef();
        } else {
            clsRef = new ClassRef(signature.getClassName());
        }
        return getFieldRef(clsRef, signature);
    }

    /**
     * Get a reference to a field using the given signature.
     *
     * @param classRef The class which contains the field.
     * @param signature The signature of the field. Only memberName and memberDescriptor are used.
     * @return A field reference with or without FieldInfo or ClassInfo.
     */
    public FieldRef getFieldRef(ClassRef classRef, Signature signature) {
        ClassInfo cls = classRef.getClassInfo();
        if ( cls != null ) {
            FieldInfo field = cls.getFieldInfo(signature.getMemberName());
            if ( field == null ) {
                return new FieldRef(cls.getClassRef(), signature.getMemberName(),
                        signature.getMemberDescriptor().getType() );
            } else {
                return field.getFieldRef();
            }
        }

        return new FieldRef(classRef, signature.getMemberName(), signature.getMemberDescriptor().getType());
    }


    //////////////////////////////////////////////////////////////////////////////
    // Roots
    //////////////////////////////////////////////////////////////////////////////

    public void addRoot(ClassInfo classInfo) {
        roots.add(classInfo);
    }

    public void addRoot(MethodInfo methodInfo) {
        roots.add(methodInfo);
    }

    public Collection<ClassInfo> getRootClasses() {
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


    //////////////////////////////////////////////////////////////////////////////
    // Class loading configuration
    //////////////////////////////////////////////////////////////////////////////

    /**
     * Add the name of a native class or a package of native classes.
     * Native classes will usually be handled in a special way by most tools.
     *
     * @see #setLoadNatives(boolean)
     * @param nativeClass the FQN of a native class or a package of native classes.
     */
    public void addNative(String nativeClass) {
        nativeClasses.add(nativeClass);
    }

    /**
     * Add the name of a library class or a library package.
     * Libraries must not contain references to application code classes.
     *
     * @see #setLoadLibraries(boolean)
     * @param libraryClass the FQN of a library class or a package of a library.
     */
    public void addLibrary(String libraryClass) {
        libraryClasses.add(libraryClass);
    }

    /**
     * Add the name of a class or a package which should not be loaded.
     * If anything is added here, the tools must be able to handle missing classes.
     *
     * <p>Note that this is independent of the value of {@link #doIgnoreMissingClasses()}.</p>
     *
     * @param ignoredClass the FQN of a class or package to exclude from loading.
     */
    public void addIgnored(String ignoredClass) {
        ignoredClasses.add(ignoredClass);
    }

    /**
     * If this is set to true, a missing class or unreadable class file will be ignored
     * and {@link #getClassInfo(String)} and {@link #loadClass(String)} (and their variants) will
     * return {@code null} instead. Else, a missing and not excluded class will trigger an {@link MissingClassError}.
     *
     * <p>This affects only classes which are not excluded by other means. If this is set to true,
     * the tools must be able to handle missing classes.</p>
     *
     * @param ignoreMissingClasses if true, ignore class load errors.
     */
    public void setIgnoreMissingClasses(boolean ignoreMissingClasses) {
        this.ignoreMissingClasses = ignoreMissingClasses;
    }

    /**
     * Check if missing and not excluded or invalid class files do not trigger an Error.
     * @see #setIgnoreMissingClasses(boolean)
     * @return true if missing or invalid classes do not trigger an error on load.
     */
    public boolean doIgnoreMissingClasses() {
        return ignoreMissingClasses;
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
     * @see #setExitOnMissingClass(boolean)
     * @return true, if {@link #loadClass(String)} or {@link #getClassInfo(String)} exists instead of throwing an Error.
     */
    public boolean doExitOnMissingClass() {
        return exitOnMissingClass;
    }

    /**
     * If set to true, the application will exit with an error message instead of throwing an
     * unchecked {@link MissingClassError} if a non-excluded class cannot be loaded in
     * {@link #loadClass(String)} or {@link #getClassInfo(String)}.
     *
     * @param exitOnMissingClass if true, exit with an error message instead of throwing an error.
     */
    public void setExitOnMissingClass(boolean exitOnMissingClass) {
        this.exitOnMissingClass = exitOnMissingClass;
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
     * Check if the classname is excluded from loading,
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


    //////////////////////////////////////////////////////////////////////////////
    // Internal Affairs
    //////////////////////////////////////////////////////////////////////////////

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
        ClassInfo cls = null;
        try {
            cls = tryLoadClass(className);

            classes.put(className, cls);

            for (AttributeManager mgr : managers.values()) {
                mgr.onLoadClass(cls);
            }
        } catch (IOException e) {
            if ( required || !ignoreMissingClasses) {
                throw new ClassInfoNotFoundException("Class '"+className+"' could not be loaded: " +
                        e.getMessage(), e);
            }
            // else cls = null
        }

        return cls;
    }

    private ClassInfo tryLoadClass(String className) throws IOException {

        InputStream is = classPath.getInputStream(className);
        JavaClass javaClass = new ClassParser(is, className).parse();
        is.close();

        return new ClassInfo(new ClassGen(javaClass));
    }

    private ClassInfo createClassInfo(String className, String superClassName, boolean isInterface) {

        String filename = className.replace(".", File.separator) + ".class";

        int af = Constants.ACC_PUBLIC;
        if ( isInterface ) {
            af |= Constants.ACC_INTERFACE;
        }

        ClassGen clsGen = new ClassGen(className, superClassName, filename, af, new String[0]);
        return new ClassInfo(clsGen);
    }

    private void handleClassLoadFailure(String message, Exception cause) {
        if ( exitOnMissingClass ) {
            logger.error(message, cause);
            System.exit(4);
        } else {
            throw new MissingClassError(message, cause);
        }
    }

}
