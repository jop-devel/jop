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

import com.jopdesign.libgraph.cfg.statements.AssignStmt;
import com.jopdesign.libgraph.cfg.statements.VariableStmt;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.stack.StackInvoke;
import com.jopdesign.libgraph.cfg.statements.stack.StackLoad;
import com.jopdesign.libgraph.cfg.statements.stack.StackStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class QuadInvoke extends InvokeStmt implements QuadStatement, AssignStmt, VariableStmt {

    private Variable result;
    private Variable instance;
    private Variable[] params;

    public QuadInvoke(ConstantMethod method, Variable result, Variable[] params) throws TypeException {
        super(method, TYPE_STATIC);
        this.params = params;
        this.result = result;
    }

    public QuadInvoke(ConstantMethod method, int invokeType, Variable result, Variable instance,
                      Variable[] params) throws TypeException
    {
        super(method, invokeType);
        this.result = result;
        this.instance = instance;
        this.params = params;
    }

    public String getCodeLine() {
        StringBuffer code = new StringBuffer();
        if ( getResultType().getType() != TypeInfo.TYPE_VOID ) {
            code.append(result.getName());
            code.append(" = ");
        }
        if ( getInvokeType() == TYPE_STATIC ) {
            code.append(getMethodInfo().getClassInfo().getClassName());
        } else {
            code.append(instance.getName());
        }
        code.append(".");
        code.append(getMethodInfo().getName());
        code.append("(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) code.append(", ");
            code.append(params[i]);
        }
        code.append(")");
        
        return code.toString();
    }

    public StackStatement[] getStackCode(VariableTable varTable) throws TypeException {
        int cnt = params.length + 1;
        if ( result != null ) {
            cnt += 1;
        }
        if ( getInvokeType() != TYPE_STATIC ) {
            cnt += 1;
        }
        StackStatement[] stmts = new StackStatement[cnt];
        cnt = 0;

        if ( getInvokeType() != TYPE_STATIC ) {
            stmts[cnt++] = QuadHelper.createLoad(this, 0);
        }
        TypeInfo[] types = getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            stmts[cnt++] = new StackLoad(types[i], params[i]);
        }

        stmts[cnt++] = new StackInvoke(getMethodConstant(), getInvokeType());

        if ( result != null ) {
            stmts[cnt] = QuadHelper.createStore(this); 
        }

        return stmts;
    }

    public boolean isConstant() {
        return false;
    }

    public ConstantValue evaluateConstantStmt() {
        return null;
    }

    public Variable getAssignedVar() {
        return result;
    }

    public Variable[] getUsedVars() {
        if ( getInvokeType() != TYPE_STATIC ) {
            Variable[] args = new Variable[params.length + 1];
            args[0] = instance;
            System.arraycopy(params, 0, args, 1, params.length);
            return args;
        } else {
            return params;
        }
    }

    public TypeInfo getAssignedType() {
        return getResultType();
    }

    public void setAssignedVar(Variable newVar) {
        result = newVar;
    }

    public TypeInfo[] getUsedTypes() {
        if ( getInvokeType() != TYPE_STATIC ) {
            TypeInfo[] args = new TypeInfo[params.length + 1];
            args[0] = TypeInfo.CONST_OBJECTREF;
            System.arraycopy(getParameterTypes(), 0, args, 1, params.length);
            return args;
        } else {
            return getParameterTypes();
        }
    }

    public void setUsedVar(int i, Variable var) {
        if ( getInvokeType() != TYPE_STATIC ) {
            if ( i == 0 ) {
                instance = var;
            } else if ( i <= params.length ) {
                params[i-1] = var;
            }
        } else if ( i < params.length ) {
            params[i] = var;
        }
    }
}
