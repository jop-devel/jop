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
package joptimizer.optimizer.inline;

import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.MethodInfo;

import java.util.List;

/**
 * This is a simple implementation of the invocation method resolver, which only checks if any method
 * overwrites the resolved method.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class BasicInvokeResolver implements InvokeResolver {

    public MethodInfo resolveInvokedMethod(MethodInfo caller, StmtHandle invokeStmt, List parentInvokes) {

        MethodInfo method = resolveInvokeStmt(caller, invokeStmt);
        if ( method != null && method.isOverwritten() ) {
            return null;
        }

        return method;
    }

    protected MethodInfo resolveInvokeStmt(MethodInfo caller, StmtHandle invokeStmt) {
        InvokeStmt invoke = (InvokeStmt) invokeStmt.getStatement();

        // get method info
        ConstantMethod method = invoke.getMethodConstant();
        if ( method.isAnonymous() ) {
            return null;
        }

        MethodInfo info = method.getMethodInfo();
        if ( info == null ) {
            return null;
        }

        // now, resolve correct method depending on the invocation type
        if ( invoke.getInvokeType() == InvokeStmt.TYPE_SPECIAL ) {

            if ( info.isPrivate() || "<init>".equals(info.getName()) ||
                    !caller.getClassInfo().isSubclassOf(invoke.getClassInfo()) )
            {
                return info;
            } else {
                // TODO resolve super invokes, as described in the jvmspec for invokespecial
                return null;
            }

        } else if ( invoke.getInvokeType() == InvokeStmt.TYPE_VIRTUAL ) {

			if (!info.isOverwritten()) {
				return info;
			} else {
				return null;
			}

            // TODO try to narrow down possibilities by using the CFG and variable type information
            // TODO try to narrow down possibilities by reducing possible subclass invocations, search interface impls.

        } else if ( invoke.getInvokeType() == InvokeStmt.TYPE_INTERFACE ) {
			// TODO resolve interface invokes, as described in the jvmspec for invokeinterface
			return null;
		}

        return info;
    }

}
