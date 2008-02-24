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

import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.JSRReturnStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadJSRReturn;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StackJSRReturn extends JSRReturnStmt implements StackStatement, VariableStmt {
    
    private Variable retAddress;

    public StackJSRReturn(Variable retAddress) {
        this.retAddress = retAddress;
    }

    public TypeInfo[] getPopTypes() {
        return new TypeInfo[0];
    }

    public TypeInfo[] getPushTypes() {
        return new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        return new QuadStatement[] { new QuadJSRReturn(retAddress) };
    }

    public int getOpcode() {
        return 0xa9;
    }

    public int getBytecodeSize() {
        return 2;
    }

    public String getCodeLine() {
        return "ret " + retAddress.getName();
    }

    public Variable getRetAddressVar() {
        return retAddress;
    }

    public Variable[] getUsedVars() {
        return new Variable[] { retAddress };
    }

    public TypeInfo[] getUsedTypes() {
        return new TypeInfo[] { TypeInfo.CONST_ADDRESSREF };
    }

    public void setUsedVar(int i, Variable var) {
        if ( i == 0 ) {
            retAddress = var;
        }
    }
}
