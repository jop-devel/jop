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
import com.jopdesign.libgraph.struct.MethodInfo;
import com.jopdesign.libgraph.struct.MethodInvocation;

/**
 * An empty implementation of StructVisitor, can be used to define only implemented methods.
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class EmptyStructVisitor implements StructVisitor {

    public void reset() {
    }

    public void start(ClassInfo classInfo, MethodInfo methodInfo) {
    }

    public void traverseDown(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
    }

    public void traverseUp(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
    }

    public void traverseLeaf(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
    }

    public void visitClass(ClassInfo classInfo) {
    }

    public void visitMethod(MethodInfo methodInfo) {
    }

    public void visitMethodInvocation(MethodInvocation invoke) {
    }

    public void finish() {
    }
}
