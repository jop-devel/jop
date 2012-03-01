/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.jcopter.greedy;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.CodeModification;
import com.jopdesign.jcopter.analysis.ExecFrequencyProvider;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;

/**
 * TODO we currently do not account for additional advantages when pairs (or sets) of candidates are optimized.
 *      We would need to tell the optimizer about
 *      - possible future candidates, e.g. invoke sites which are created by inlining a method.
 *      - advantages when optimizing combinations of candidates (from the same optimizer; considering gains when
 *        candidates of different optimizers are taken into account would be quite tricky), e.g. if all invokesites
 *        are inlined, we know that we can remove the invokee. We could use the CodeOptimizers to find promising sets
 *        of candidates, else the candidates would need to provide all the required information to find such sets in a
 *        generic way.
 *      - improved WCET-gains when optimizing multiple candidates (this could be done in some generic way).
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class Candidate implements CodeModification {

    private final MethodInfo method;
    protected InstructionHandle start;
    protected InstructionHandle end;

    protected Candidate(MethodInfo method, InstructionHandle start, InstructionHandle end) {
        this.method = method;
        this.start = start;
        this.end = end;
    }

    /**
     * @return the method containing this candidate.
     */
    @Override
    public MethodInfo getMethod() {
        return method;
    }

    public InstructionHandle getStart() {
        return start;
    }

    public InstructionHandle getEnd() {
        return end;
    }

    /**
     * We assume that there is only a single entry edge into this optimized code region. The entry instruction is
     * used to check if the optimized code is on the WCET path and to get the execution frequency for the code to optimize.
     *
     * @return the direct dominator instruction for the optimized code.
     */
    public InstructionHandle getEntry() {
        return start;
    }

    public float getHeuristicFactor() {
        return 1.0f;
    }

    /**
     * Perform optimization, update start and end instruction handle to the new code.
     *
     * @param analyses the analyses used by the optimizer.
     * @param stacksize the stack analysis for the method to optimize
     * @return true if the optimization has been performed.
     */
    public abstract boolean optimize(AnalysisManager analyses, StacksizeAnalysis stacksize);

    /**
     * Update deltaCodesize, deltaLocals and localGain, after some method which may be invoked by the
     * affected code has been changed.
     *
     * <p>This MUST return false if either the stack size or the number of locals exceeds the limits of the
     * processor. This MAY return false if the new local codesize exceeds the limits of the processor (but this
     * will be checked anyway by the candidate selector).</p>
     *
     *
     * @param analyses the analyses used by the optimizer.
     * @param stacksize the stack analysis for the method to optimize.
     * @return false if this is not a candidate anymore.
     */
    public abstract boolean recalculate(AnalysisManager analyses, StacksizeAnalysis stacksize);

    @Override
    public Collection<MethodInfo> getUnreachableMethods() {
        return null;
    }

    /**
     * To get a few more candidates, we allow for candidates which only work in a set of execution contexts.
     * The optimization is only valid if either all other execution contexts are removed (e.g. by inlining or
     * callgraph pruning), of if the optimizer creates a copy of the method for all other execution contexts first.
     *
     * @return a set of callstrings leading to the optimized method for which this optimization is valid, or null
     * or an empty set (or a set with a single empty callstring) if this optimization is valid for all callstrings.
     */
    public Collection<CallString> getRequiredContext() {
        return null;
    }

    public abstract int getMaxLocalsInRegion();

    /**
     * TODO in order to support this properly, we need to extend the StacksizeAnalysis with a life range analysis
     *      for locals (this could use the ValueMapAnalysis).
     *
     * @return the number of additional local slots required outside the changed code range.
     */
    public int getNumPersistentLocals() {
        return 0;
    }

    /**
     * @return the expected gain in cycles for a single execution of this code without cache costs
     *        (i.e. assuming always-hit).
     */
    public abstract long getLocalGain();

    /**
     * Return the expected difference for cache miss costs for a single execution of this code, assuming an
     * unknown cache state previous to the execution of the code. This does not need to assume always-miss:
     * if a method is invoked twice in the optimized code and the invoker is in an all-fit region, the second
     * invoke (and the return from the first invoke!) can be assumed to be a hit, although the first invoke
     * must be assumed to be a miss. A negative value means reduced cache miss costs.
     *
     * <p>This does not include the cache effects due to the increased codesize of the optimized method.</p>
     *
     * @return the cache miss costs difference assuming an unknown cache state for a single execution for the invokes
     *         in the optimized code.
     * @param analyses
     * @param ecp
     */
    public abstract long getDeltaCacheMissCosts(AnalysisManager analyses, ExecFrequencyProvider ecp);

    @Override
    public String toString() {
        return getMethod().toString()+"@"+getEntry();
    }
}
