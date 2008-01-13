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
package com.jopdesign.libgraph.cfg.variable;

import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class Variable {

    private ConstantValue constantValue;
    private TypeInfo type;
    private Set defs;
    private Set uses;
    private String name;

    public Variable(String name) {
        this.name = name;
        constantValue = null;
        type = null;
        defs = new HashSet();
        uses = new HashSet();
    }


    public String getName() {
        return name;
    }

    public boolean hasType() {
        return type != null;
    }

    public TypeInfo getType() {
        return type;
    }

    public boolean isConstant() {
        return constantValue != null;
    }

    public ConstantValue getConstantValue() {
        return constantValue;
    }

    public void addUsage(StmtHandle handle) {
        uses.add(handle);
    }

    public void addAssignment(StmtHandle handle) {
        defs.add(handle);
    }

    public String toString() {
        return getName();
    }
}
