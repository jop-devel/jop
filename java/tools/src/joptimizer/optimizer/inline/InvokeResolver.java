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
import com.jopdesign.libgraph.struct.MethodInfo;

import java.util.List;

/**
 * This interface is used to resolve and devirtualize invocations.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface InvokeResolver {
    
    /**
     * Find the invoked method. This method must also check if the invoked method is not overloaded in any
     * other (known) class.
     *
     * @param caller the method which contains the invocation.
     * @param invokeStmt a handle to the invokestatement
     * @param parentInvokes a list of already inlined invokes, first entry is the method itself.
     * @return the invoked method, or null if the method cannot be determined or the method may be overloaded.
     */
    MethodInfo resolveInvokedMethod(MethodInfo caller, StmtHandle invokeStmt, List parentInvokes);

}
