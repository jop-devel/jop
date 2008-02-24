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
package com.jopdesign.libgraph.cfg.statements.stack;

import com.jopdesign.libgraph.cfg.statements.SideEffectStmt;
import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.IncStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadBinop;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * TODO modifies variable inplace, no assignment here..
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackIInc extends IncStmt implements StackStatement, VariableStmt, SideEffectStmt {

    private Variable variable;

    public StackIInc(Variable variable, int increment) {
        super(increment);
        this.variable = variable;
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) {
        return new QuadStatement[] { new QuadBinop(TypeInfo.CONST_INT, QuadBinop.OP_ADD, variable, variable,
                varTable.getDefaultConstant( new ConstantValue(TypeInfo.CONST_INT, getIncrement())) ) };
    }

    public int getOpcode() {
        return 0x84;
    }

    public int getBytecodeSize() {
        return 3;
    }

    public String getCodeLine() {
        return "iinc " + variable.getName() + ", " + getIncrement();
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

    public Variable[] getUsedVars() {
        return new Variable[] { variable };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_INT };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            variable = var;
        }
    }

    public Variable getIncVariable() {
        return variable;
    }

    public boolean isModified(int var) {
        return var == 0;
    }

    public boolean isConstant(int var) {
        return false;
    }

    public ConstantValue getConstantValue(int var) {
        return null;
    }
}
