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
package joptimizer.framework.visit;

import com.jopdesign.libgraph.struct.ClassInfo;
import com.jopdesign.libgraph.struct.MethodCode;
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.MethodInvocation;

/**
 * A simple visitor which collects some statistics as it goes along.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StatisticsVisitor extends EmptyStructVisitor {

    private int maxStackSize;
    private int maxLocalSize;
    private int maxCodeSize;


    public StatisticsVisitor() {
        reset();
    }

    public void reset() {
        maxStackSize = 0;
        maxLocalSize = 0;
        maxCodeSize = 0;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getMaxLocalSize() {
        return maxLocalSize;
    }

    public int getMaxCodeSize() {
        return maxCodeSize;
    }

    public void start(ClassInfo classInfo, MethodInfo methodInfo) {
    }

    public void visitClass(ClassInfo classInfo) {
    }

    public void visitMethod(MethodInfo methodInfo) {
        MethodCode code = methodInfo.getMethodCode();
        if ( code != null ) {
            maxStackSize = Math.max(maxStackSize, code.getMaxStackSize());
            maxLocalSize = Math.max(maxLocalSize, code.getMaxLocals());
            maxCodeSize = Math.max(maxCodeSize, code.getCodeSize());
        }
    }

    public void visitMethodInvocation(MethodInvocation invoke) {
    }
}
