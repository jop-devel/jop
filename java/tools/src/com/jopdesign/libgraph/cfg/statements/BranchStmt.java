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
package com.jopdesign.libgraph.cfg.statements;

/**
 * Common interface for all stmts which have at least two alternative targets.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface BranchStmt extends ControlFlowStmt {

    /**
     * Get number of targets this statement has.
     *
     * @return number of targets.
     */
    int getTargetCount();

    /**
     * Check if this statement always returns the same target (i.e. all
     * arguments are constant, if any)
     *
     * @return true, if always the same branch is taken.
     */
    boolean isConstant();

    /**
     * Get the number of the constant target.
     * @return the number of the constant target, or -1 if no branch is taken                                
     */
    int getConstantTarget();
}
