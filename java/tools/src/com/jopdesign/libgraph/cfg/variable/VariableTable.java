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

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.struct.ConstantValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class VariableTable {

    private class TableVar extends Variable {

        protected TableVar(String name) {
            super(name);
        }

        protected TableVar(String name, ConstantValue constantValue) {
            super(name, constantValue);
        }

        public VariableTable getVariableTable() {
            return VariableTable.this;
        }

        public int getIndex() {
            return VariableTable.this.getIndex(this);
        }
    }

    private ControlFlowGraph graph;
    private List variables;
    private Map variableNames;
    private Map constants;

    public static final String LOCAL_VAR_PREFIX = "l";
    public static final String STACK_VAR_PREFIX = "s";
    public static final String CONST_VAR_PREFIX = "c";

    public VariableTable(ControlFlowGraph graph) {
        this.graph = graph;
        this.variables = new ArrayList();
        variableNames = new HashMap();
        constants = new HashMap();
    }

    /**
     * Get a variable by name and create a new one if not found.
     * @param name the name of the variable.
     * @return the variable with that name.
     */
    public Variable getVariable(String name) {
        return getVariable(name, true);
    }

    public Variable getDefaultLocalVariable(int pos) {
        Variable var = getVariable(LOCAL_VAR_PREFIX + pos, false);
        if ( var == null ) {
            var = createVariable(pos, LOCAL_VAR_PREFIX + pos);
        }
        return var;
    }

    public Variable getDefaultStackVariable(int depth) {
        return getVariable(STACK_VAR_PREFIX + depth, true);
    }

    /**
     * Get a variable by name.
     *
     * @param name the name of the variable.
     * @param create if this is true and no variable by that name exists, create a new one.
     * @return the new variable, or null if not found and not created.
     */
    public Variable getVariable(String name, boolean create) {
        Variable var = (Variable) variableNames.get(name);
        if ( var == null && create ) {
            var = new TableVar(name);
            variables.add(var);
            variableNames.put(name, var);
        }
        return var;
    }

    /**
     * Get a variable by position in the table.
     * Returns null if number is out of range.
     *
     * @param nr the position of the variable.
     * @return the variable or null if not defined.
     */
    public Variable getVariable(int nr) {
        if ( nr >= variables.size() ) {
            return null;
        }
        return (Variable) variables.get(nr);
    }

    /**
     * Create a new variable at a given position.
     * If a variable exists on this position, it is overwritten.
     * If the index is outside the current table, the table is extended and
     * empty fields are filled with null.
     *
     * @param pos the new position of this variable.
     * @param name the name for the new variable.
     * @return the new variable.
     */
    public Variable createVariable(int pos, String name) {
        Variable var = new TableVar(name);

        for ( int i = variables.size(); i <= pos; i++ ) {
            variables.add(null);
        }
        variables.set(pos, var);
        variableNames.put(name, var);
        
        return var;
    }

    /**
     * Create an new variable at the end of the table with a default name.
     * 
     * @see #getDefaultLocalVariable(int)
     * @return a new variable.
     */
    public Variable createVariable() {
        return getDefaultLocalVariable(variables.size());
    }

    /**
     * Get a list of all variables in this table.
     * Do not modify this list.
     * @return a list of Variables.
     */
    public List getVariables() {
        return Collections.unmodifiableList(variables);
    }

    public int size() {
        return variables.size()+1; // the last variable could be a long or double
    }

    public int getIndex(Variable var) {
        return variables.indexOf(var);
    }

    /**
     * Get a new constant variable for a value. This variable is not registered to the
     * variable table.
     * @param value the constant value.
     * @return a constant variable containing the constant value. 
     */
    public Variable getDefaultConstant(ConstantValue value) {
        Variable var = (Variable) constants.get(value);
        if ( var == null ) {
            var = new TableVar(CONST_VAR_PREFIX + constants.size(), value);
            constants.put(value, var);
        }
        return var;
    }
}
