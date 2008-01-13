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
package com.jopdesign.libgraph.cfg.statements.quad;

import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.MonitorStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackEntermonitor;
import com.jopdesign.libgraph.cfg.statements.stack.StackExitmonitor;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadMonitor extends MonitorStmt implements QuadStatement, VariableStmt {

    private Variable monitor;

    public QuadMonitor(int type, Variable monitor) {
        super(type);
        this.monitor = monitor;
    }

    public StackStatement[] getStackCode(VariableTable varTable) {
        if ( getMonitorType() == MONITOR_ENTER ) {
            return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                new StackEntermonitor()
            };
        } else {
            return new StackStatement[] {
                QuadHelper.createLoad(this, 0),
                new StackExitmonitor()
            };
        }
    }

    public Variable[] getUsedVars() {
        return new Variable[] { monitor };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_OBJECTREF };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            monitor = var;
        }
    }

    public String getCodeLine() {
        return (getMonitorType() == MONITOR_ENTER ? "monitorenter " : "monitorexit ") + monitor.getName();
    }
}
