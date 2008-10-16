/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.struct;

import com.jopdesign.libgraph.struct.type.ArrayRefType;
import com.jopdesign.libgraph.struct.type.TypeHelper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A container which holds all informations about the structure and data of the
 * loaded application and libraries.
 *
 * This class provides methods to access and modify this struct.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class AppStruct {

    private Map classInfos;
    private AppClassLoader loader;
    private AppConfig config;

    private static final Logger logger = Logger.getLogger(AppStruct.class);

    public AppStruct(AppClassLoader loader, AppConfig config) {
        this.loader = loader;
        this.config = config;
        classInfos = new HashMap();
    }

    public AppConfig getConfig() {
        return config;
    }

    public String getClassPath() {
        return loader.getClassPath();
    }

    /**
     * Set the current classpath to use for loading classes.
     * This does not change or reload already loaded classes.
     * @param path the new path to use.
     */
    public void setClassPath(String path) {
        loader.setClassPath(path);
    }

    /**
     * This is a wrapper for {@link com.jopdesign.libgraph.struct.AppClassLoader#loadClassInfo(AppStruct, String)}
     * of the current loader. A new classInfo object will be created, even if the class is already loaded in the
     * AppStruct.
     *
     * {@link AppConfig} settings are ignored by this method. To honor AppConfig, use {@link #getClassInfo(String, boolean)}
     * or {@link #tryLoadMissingClass(String)}.
     *
     * @see com.jopdesign.libgraph.struct.AppClassLoader# loadClassInfo (AppStruct, String)
     * @see #getClassInfo(String, boolean)  
     * @param className the name of the class to load.
     * @return a new ClassInfo of the class.
     * @throws TypeException
     */
    public ClassInfo loadClassInfo(String className) throws TypeException {
        try {
            return loader.loadClassInfo(this, className);
        } catch (IOException e) {
            throw new TypeException("Could not create class {"+className+"}. Please check your classpath.", e);
        }
    }

    public ClassInfo createClassInfo(String className, String superClassName, boolean isInterface) {
        return loader.createClassInfo(this, className, superClassName, isInterface);
    }

    /**
     * Try to load a missing class. <br>
     * This returns the new class if found and loaded,
     * null if this class should by ignored, or throws an exeption if the class
     * is missing but shouldn't. <br>
     * This depends on the settings of appConfig. <br>
     *
     * TODO maybe introduce load/post-load phases for AppStruct, set 'onDemand/init' depending on phase ?
     * TODO maybe add loaded classes to a separate cache (not to the classlist, as this should only contain explicitly added classes).
     *
     * @param className the classname which should by tried to load.
     * @return the loaded classinfo or null if this class should be ignored.
     * @throws TypeException if this class should not be missing.
     */
    public ClassInfo tryLoadMissingClass(String className) throws TypeException {
        return tryLoadClass(className, true, true);
    }

    /**
     * Try to load a class. <br>
     * This returns the new class if found and loaded,
     * null if this class should by ignored, or throws an exeption if the class
     * is missing but shouldn't. <br>
     * This depends on the settings of appConfig. <br>
     *
     * @param className the classname which should by tried to load.
     * @param onDemand true if the class is loaded after the initial class loading phase because it is referenced by another class.
     * @param initClass true if the classinfo should be initialized after loading it. 
     * @return the loaded classinfo or null if this class should be ignored.
     * @throws TypeException if this class should not be missing.
     */
    public ClassInfo tryLoadClass(String className, boolean onDemand, boolean initClass) throws TypeException {

        // first, check if class should be ignored by configuration, return null for ignored classes.
        String reason = doIgnoreClassName(className);        
        if ( reason != null ) {
            if (logger.isDebugEnabled()) {
                logger.debug(reason);
            }
            return null;
        }

        // a bit tricky: if load-on-demand is disabled and the class is loaded on demand (and not ignored),
        // do not load the class, but return null or throw an error if incomplete code is not allowed
        if ( !config.doLoadOnDemand() && onDemand ) {
            if ( config.doAllowIncompleteCode() ) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Skipping on-demand loading of class {" + className + "}");
                }
                return null;
            } else {
                throw new TypeException("Load-on-demand not enabled (class "+className+").");
            }
        }

        ClassInfo info;

        try {
            info = loader.loadClassInfo(this, className);
        } catch (IOException e) {
            if ( config.doAllowIncompleteCode() ) {
                logger.warn("Could not load class {"+className+"}, ignoring.", e);
                return null;
            } else {
                throw new TypeException("Could not load class {"+className+"}.", e);
            }
        }

        if ( info == null ) {
            if ( config.doAllowIncompleteCode() ) {
                if (logger.isInfoEnabled()) {
                    logger.info("Could not find class {" + className + "}, ignoring.");
                }
                return null;
            } else {
                throw new TypeException("Could not find class {" + className + "}.");
            }
        }

        // need to initialize the class (?)
        if ( initClass ) {
            info.reload();
            for (Iterator it = info.getMethodInfos().iterator(); it.hasNext();) {
                MethodInfo methodInfo = (MethodInfo) it.next();
                methodInfo.reload();
            }
        }

        return info;
    }

    /**
     * Check if the class should be ignored due to configuration settings.<br>
     * {@link AppConfig#doAllowIncompleteCode()} has no effect here.
     *
     * @param className the fully qualified class name.
     * @return a reason text why the class is ignored, or null if the class should be loaded.
     */
    public String doIgnoreClassName(String className) {
        if ( config.isNativeClassName(className) ) {
            return "Skipping native class {" + className + "}.";
        }

        if ( config.isLibraryClassName(className) ) {
            return "Skipping library class {" + className + "}";
        }

        if ( config.doExcludeClassName(className) ) {
            return "Skipping excluded class {"+ className + "}";
        }

        return null;
    }

    /**
     * get a list of all classInfos of the application.
     * @return a collection of all classinfos.
     */
    public Collection getClassInfos() {
        return classInfos.values();
    }

    /**
     * Get a known classInfo by name.
     * This does not try to load unkown classes. 
     *
     * @see #getClassInfo(String, boolean)
     * @param className the fully qualified classname.
     * @return the classinfo if found, else null.
     */
    public ClassInfo getClassInfo(String className) {
        return (ClassInfo) classInfos.get(className);
    }

    /**
     * Get a classInfo and try to load it if not found using {@link #tryLoadMissingClass(String)}.<br>
     * A new class is not added to the classlist (this might someday be done by tryLoadMissingClass).
     *
     * @param className the fully qualified class name of the class to load.
     * @param ignoreMissing if false and the class is not loaded, an exception is thrown
     *    (i.e. this function never returns null if ignoreMissing is false).
     * @return the classInfo or null if class not found and ignoreMissing is true.
     * @throws TypeException if the class should be loaded but was not found.
     */
    public ClassInfo getClassInfo(String className, boolean ignoreMissing) throws TypeException {
        ClassInfo classInfo = getClassInfo(className);
        if ( classInfo == null ) {
            classInfo = tryLoadMissingClass(className);
        }
        if ( classInfo == null && !ignoreMissing ) {
            throw new TypeException("Could not load required class {"+className+"}.");
        }
        return classInfo;
    }

    /**
     * A function to create a constantclass for a classname.
     * This function tries to load the class with {@link #getClassInfo(String)} and {@link #tryLoadMissingClass(String)}
     * and creates an anonymous constantclass if the class could not be loaded.
     *
     * @param className the classname of the class to load.
     * @param isInterface true, if the class is an interface; only used if anonymous constantclass is created.
     * @return the new constantclass for this class.
     * @throws TypeException if the class could not be loaded and anonymous classes are disabled.
     */
    public ConstantClass getConstantClass(String className, boolean isInterface) throws TypeException {

        // This is an array-class
        if ( className.startsWith("[") ) {
            ArrayRefType type = (ArrayRefType) TypeHelper.parseType(this, className);
            return new ConstantClass(type);            
        }

        ClassInfo classInfo = getClassInfo(className);
        if ( classInfo == null ) {
            classInfo = tryLoadMissingClass(className);
        }
        ConstantClass cls;
        if ( classInfo == null ) {
            cls = new ConstantClass(className, isInterface);
        } else {
            cls = new ConstantClass(classInfo);
        }
        return cls;
    }

    public ConstantMethod getConstantMethod(ConstantClass clazz, String methodName, String signature,
                                            boolean isStatic) throws TypeException
    {
        ConstantMethod method = null;

        ClassInfo info = clazz.getClassInfo();
        if ( info != null ) {

            MethodInfo methodInfo = info.getVirtualMethodInfo(methodName, signature);

            if ( methodInfo != null ) {
                method = new ConstantMethod(info, methodInfo);
            } else {
                // TODO check if class really extends unloaded classes, else throw exception
                //throw new TypeException("Could not find method {" + methodName + "} with signature {" +
                //        signature + "} in class {" + info.getClassName() + "}");
            }
        }

        if ( method == null ) {
            method = new ConstantMethod(clazz.getClassName(), methodName, signature, clazz.isInterface(), isStatic);
        }

        return method;
    }

    public ConstantField getConstantField(ConstantClass clazz, String fieldName, String signature,
                                          boolean isStatic) throws TypeException
    {
        ConstantField field = null;

        ClassInfo info = clazz.getClassInfo();
        if ( info != null ) {

            FieldInfo fieldInfo = info.getVirtualFieldInfo(fieldName);

            if ( fieldInfo != null ) {
                field = new ConstantField(info, fieldInfo);
            } else {
                // TODO check if class really extends unloaded classes, else throw exception
                //throw new TypeException("Could not find field {"+fieldName+"} in class {"+info.getClassName()+"}.");
            }

        }

        if ( field == null ) {
            field = new ConstantField(clazz.getClassName(), fieldName, signature, isStatic);
        }

        return field;
    }

    public void addClass(ClassInfo classInfo) {
        classInfos.put(classInfo.getClassName(), classInfo);

        if ( logger.isInfoEnabled() ) {
            logger.info("Add class: " + classInfo.getClassName());
        }
    }

    public void removeClass(String className) {
        classInfos.remove(className);
    }

    /**
     * Initialize all the classInfos.
     *
     * @param forceReload true if the classes should be reloaded even if they are already initialized.
     */
    public void initClassInfos(boolean forceReload) throws TypeException {
        Iterator it = getClassInfos().iterator();

        // reload all classinfos
        while (it.hasNext()) {
            ClassInfo classInfo = (ClassInfo) it.next();
            if ( forceReload || !classInfo.isInitialized() ) {
                classInfo.reload();
            }
        }

        // reload all methodinfos. Must (currently) be done after all classes have been initialized
        // because this needs the methodinfos of super- and sub-classes.
        it = getClassInfos().iterator();
        while (it.hasNext()) {
            ClassInfo classInfo = (ClassInfo) it.next();
            Collection methods = classInfo.getMethodInfos();

            for (Iterator it2 = methods.iterator(); it2.hasNext();) {
                MethodInfo method = (MethodInfo) it2.next();
                if ( forceReload || !method.isInitialized() ) {
                    method.reload();
                }
            }
        }

    }

    /**
     * Remove all classes.
     */
    public void clear() {
        classInfos.clear();
    }

    public boolean contains(String className) {
        return classInfos.containsKey(className);
    }
}
