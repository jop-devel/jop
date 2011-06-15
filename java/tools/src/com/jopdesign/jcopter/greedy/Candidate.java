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
import org.apache.bcel.generic.InstructionHandle;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class Candidate {
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
    public MethodInfo getMethod() {
        return method;
    }

    /**
     * @return the first instruction in the instruction list which will be modified. After optimization this needs
     *         to refer to the first modified or new instruction.
     */
    public InstructionHandle getStart() {
        return start;
    }

    /**
     * @return the last instruction in the instruction list which will be modified. After optimization this needs
     *         to refer to the last modified or new instruction.
     */
    public InstructionHandle getEnd() {
        return end;
    }

    /**
     * @return
     */
    public InstructionHandle getEntry() {
        return start;
    }

    /**
     * Perform optimization, update start and end instruction handle to the new code.
     * @return true if the optimization has been performed.
     */
    public abstract boolean optimize();

    /**
     * Update deltaCodesize, deltaLocals and localGain, after some method which may be invoked by the
     * affected code has been changed.
     * @return false if this is not a candidate anymore.
     */
    public abstract boolean recalculate();

    public abstract int getDeltaCodesize();

    public abstract int getDeltaLocals();

    public abstract int getLocalGain();
}
