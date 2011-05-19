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

import com.jopdesign.common.code.ControlFlowGraph;
import org.apache.bcel.generic.InstructionList;

/**
 * An AppEventHandler is used to access attributes and flow-facts from classes, methods, fields and code.
 * It manages the {@link KeyManager.CustomKey} keys and type casts internally, and provides some callback
 * methods to AppInfo to be notified of (some) changes to the classes.
 * <p>
 * Each AppEventHandler should provide custom methods to get/set its custom attributes in addition to
 * the callback methods.
 * </p>
 * TODO add callback methods for KeyManager (onClearKey, ..)
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface AppEventHandler {

    /**
     * Called on registration by AppInfo.
     * <p>
     * Let the manager perform tasks on registration, like registering keys and updating
     * flow-facts for all already loaded classes.
     * </p>
     *
     * @param appInfo the AppInfo for which the manager is registered.
     */
    void onRegisterEventHandler(AppInfo appInfo);

    /**
     * Called when a new class is created or loaded from disk, allows the manager to add custom fields to the class.
     *
     * @param classInfo the classInfo which has been created.
     * @param loaded true if the class has been loaded from a file, false if it has been created from scratch.
     */
    void onCreateClass(ClassInfo classInfo, boolean loaded);
    
    /**
     * Called when a class is removed from AppInfo.
     *
     * @param classInfo the classInfo before it is removed.
     */
    void onRemoveClass(ClassInfo classInfo);

    void onRemoveField(FieldInfo field);

    void onRemoveMethod(MethodInfo method);

    /**
     * Called before all classes are removed from AppInfo.
     * @param appInfo the appinfo which will be cleared.
     */
    void onClearAppInfo(AppInfo appInfo);

    /**
     * Called whenever a new ControlFlowGraph is created, either via constructor or via
     * {@link MethodCode#getControlFlowGraph(boolean)}.
     *
     * @see #onCreateMethodControlFlowGraph(ControlFlowGraph, boolean)
     * @param cfg the new CFG.
     */
    void onCreateControlFlowGraph(ControlFlowGraph cfg);

    /**
     * Called when {@link MethodCode#getControlFlowGraph(boolean)} creates a new CFG.
     * Not called when a CFG is created outside the framework.
     *
     * @see #onCreateControlFlowGraph(ControlFlowGraph)
     * @param cfg the new CFG
     * @param clean true if a 'clean' graph is requested, i.e. no analyse transformations should be performed.
     */
    void onCreateMethodControlFlowGraph(ControlFlowGraph cfg, boolean clean);

    /**
     * Called when the method instruction list is modified.
     * <p>
     * We currently do not have a safe way to detect modifications to instruction lists, therefore to be on the safe
     * side this event might be triggered before the actual modification takes place, i.e. when
     * {@link MethodCode#getInstructionList()} is called.
     * </p>
     * TODO we might want to implement something like MethodCode#modified() which is called by the user after modification.
     *
     * @param methodCode the method which got modified.
     * @param beforeModification true if called when the instruction list is handed out for modification, false if
     *        {@link MethodCode#setInstructionList(InstructionList)} is called.
     */
    void onMethodCodeModify(MethodCode methodCode, boolean beforeModification);

}
