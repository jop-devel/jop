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

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface CustomValueManager {

    /**
     * Called on registration by AppInfo.
     * Let the manager perform tasks on registration, like registering keys and updating
     * flowfacts for all already loaded classes.
     *
     * @param appInfo the AppInfo for which the manager is registered.
     */
    void registerManager(AppInfo appInfo);

    
    void onCreateClass(ClassInfo classInfo);
    
    /**
     * Called if a class is loaded, allows the manager to add custom fields to the class.
     *
     * @param classInfo the classInfo which has been loaded.
     */
    void onLoadClass(ClassInfo classInfo);

    void onRemoveClass(ClassInfo classInfo);

    void onClearAppInfo(AppInfo appInfo);

    // TODO methods to update/clear/reset custom fields on class/method/field-modify
    // TODO add reason/modification-type to onModified

    void onClassModified(ClassInfo classInfo);

    void onMethodModified(MethodInfo methodInfo);

}
