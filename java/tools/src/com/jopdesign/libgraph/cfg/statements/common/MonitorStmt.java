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
package com.jopdesign.libgraph.cfg.statements.common;

import com.jopdesign.libgraph.cfg.statements.Statement;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class MonitorStmt extends AbstractStatement implements Statement {

    public static final int MONITOR_ENTER = 1;
    public static final int MONITOR_EXIT  = 2;

    private int type;

    protected MonitorStmt(int type) {
        this.type = type;
    }

    public int getMonitorType() {
        return type;
    }

    public boolean canThrowException() {
        return true;
    }

}
