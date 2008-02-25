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

    public ClassInfo createClassInfo(String className) throws TypeException {
        try {
            return loader.createClassInfo(this, className);
        } catch (IOException e) {
            throw new TypeException("Could not create class {"+className+"}", e);
        }
    }

    /**
     * Try to load a missing class. <br>
     * This returns the new (uninitialized) class if found and loaded,
     * null if this class should by ignored, or throws an exeption if the class
     * is missing but shouldn't. <br>
     * This depends on the settings of jopConfig.
     *
     * @param className the classname which should by tried to load.
     * @return the loaded classinfo or null if this class should be ignored.
     * @throws TypeException if this class should not be missing.
     */
    public ClassInfo tryLoadMissingClass(String className) throws TypeException {

        if ( config.isLibraryClassName(className) ) {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping post-loading of library class {" + className + "}");
            }
            return null;
        }

        if ( !config.doLoadOnDemand() ) {
            if ( config.doAllowIncompleteCode() || config.isNativeClassName(className) ) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Skipping loading of class {" + className + "}");
                }
                return null;
            } else {
                throw new TypeException("Load-on-demand not enabled (class "+className+").");
            }
        }

        String reason = config.doExcludeClassName(className);
        if ( reason != null ) {
            if ( config.doAllowIncompleteCode() ) {
                if ( logger.isInfoEnabled() ) {
                    logger.info(reason);
                }
                return null;
            } else {
                throw new TypeException(reason);
            }
        }

        ClassInfo info;

        try {
            info = loader.createClassInfo(this, className);
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
        info.reload();
        for (Iterator it = info.getMethodInfos().iterator(); it.hasNext();) {
            MethodInfo methodInfo = (MethodInfo) it.next();
            methodInfo.reload();
        }

        return info;
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
     * Get a classInfo and try to load it if not found.
     *
     * @param className the fully qualified class name of the class to load.
     * @param ignoreMissing if the class is not loaded, throw an exception.
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
     * Remove all classes.
     */
    public void clear() {
        classInfos.clear();
    }

    public boolean contains(String className) {
        return classInfos.containsKey(className);
    }
}
