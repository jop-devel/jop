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

package com.jopdesign.jcopter.inline;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.greedy.Candidate;
import org.apache.bcel.generic.InstructionHandle;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class InlineCandidate extends Candidate {


    protected InlineCandidate(MethodInfo method, InstructionHandle start, InstructionHandle end) {
        super(method, start, end);
    }

    @Override
    public boolean optimize() {
        return false;
    }

    @Override
    public boolean recalculate() {
        return false;
    }

    @Override
    public int getDeltaCodesize() {
        return 0;
    }

    @Override
    public int getDeltaLocals() {
        return 0;
    }

    @Override
    public int getLocalGain() {
        return 0;
    }
}
