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

/**
 * An empty class which implements all callback handlers of AppEventHandler, so you
 * don't need to.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class EmptyAppEventHandler implements AppEventHandler {

    @Override
    public void onCreateClass(ClassInfo classInfo, boolean loaded) {
    }

    @Override
    public void onRemoveClass(ClassInfo classInfo) {
    }

    @Override
    public void onRemoveField(FieldInfo field) {
    }

    @Override
    public void onRemoveMethod(MethodInfo method) {
    }

    @Override
    public void onClearAppInfo(AppInfo appInfo) {
    }

    @Override
    public void onCreateControlFlowGraph(ControlFlowGraph cfg) {
    }

    @Override
    public void onCreateMethodControlFlowGraph(ControlFlowGraph cfg, boolean clear) {
    }

    @Override
    public void onMethodCodeModify(MethodCode methodCode, boolean beforeModification) {
    }
}
