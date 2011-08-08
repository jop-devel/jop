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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.MethodInfo;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collection;

/**
 * This class represents changes to the code size and the callgraph made by optimizations and which can
 * be passed to various analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface CodeModification {

    MethodInfo getMethod();

    /**
     * @return the first instruction in the instruction list which will be modified. After optimization this needs
     *         to refer to the first modified or new instruction.
     */
    InstructionHandle getStart();

    /**
     * @return the last instruction in the instruction list which will be modified. After optimization this needs
     *         to refer to the last modified or new instruction.
     */
    InstructionHandle getEnd();

    int getDeltaLocalCodesize();

    /**
     * Return a set of methods which become unreachable (w.r.t. to the complete callgraph, not to the set of
     * methods to optimize) after the optimization has been performed, e.g. due to inlining the last callsite.
     *
     * <p>This is used to calculate the resulting total codesize as well as to skip unused methods from optimizations.</p>
     *
     * <p>Note that the resultset can become larger after another optimization took place. Also note that if a method
     * becomes unreachable in the target- or WCA-callgraph but not in the main callgraph, it is not returned here.</p>
     *
     * @return a set of methods which can be removed after the optimization, or null or an empty set if nothing changes.
     */
    Collection<MethodInfo> getUnreachableMethods();

    /**
     * @return a set of methods which are no longer invoked anywhere in the method after the optimization in any context.
     */
    Collection<MethodInfo> getRemovedInvokees();
}
