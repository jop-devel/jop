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

import com.jopdesign.libgraph.cfg.ControlFlowGraph;
import com.jopdesign.libgraph.cfg.statements.StmtHandle;
import com.jopdesign.libgraph.struct.MethodInfo;

import java.util.List;
import java.util.Set;

/**
 * This class is a container for results of inline-checks.
 * 
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class CheckResult {

    private StmtHandle stmt;
    private MethodInfo invokedMethod;
    private ControlFlowGraph srcGraph;
    private boolean unsafeInline;
    private int srcCodeSize;
    private int srcLocals;
    private int localsOffset;
    private Set changePublic;
    private List parentInlines;

    /**
     * Create a new checkresult.
     *
     * @param stmt the checked invoke statement
     * @param invokedMethod the invoked method
     * @param srcGraph the graph of the invoked method which will be inlined.
     * @param unsafeInline true, if the invoked method may change if dynamic class loading is used.
     * @param srcCodeSize the codesize of the invoked method.
     * @param srcLocals the number of locals of the invoked method.
     * @param localsOffset the offset for local variables to use for this method inlining.
     */
    public CheckResult(StmtHandle stmt, MethodInfo invokedMethod, ControlFlowGraph srcGraph, boolean unsafeInline, int srcCodeSize,
                       int srcLocals, int localsOffset) {
        this.stmt = stmt;
        this.invokedMethod = invokedMethod;
        this.srcGraph = srcGraph;
        this.unsafeInline = unsafeInline;
        this.srcCodeSize = srcCodeSize;
        this.srcLocals = srcLocals;
        this.localsOffset = localsOffset;
    }

    public StmtHandle getStmt() {
        return stmt;
    }

    public MethodInfo getInvokedMethod() {
        return invokedMethod;
    }

    public ControlFlowGraph getSrcGraph() {
        return srcGraph;
    }

    public boolean isUnsafeInline() {
        return unsafeInline;
    }

    public int getSrcCodeSize() {
        return srcCodeSize;
    }

    public int getSrcLocals() {
        return srcLocals;
    }

    public int getLocalsOffset() {
        return localsOffset;
    }

    public Set getChangePublic() {
        return changePublic;
    }

    public void setChangePublic(Set changePublic) {
        this.changePublic = changePublic;
    }

    public List getParentInlines() {
        return parentInlines;
    }

    public void setParentInlines(List parentInlines) {
        this.parentInlines = parentInlines;
    }
}
