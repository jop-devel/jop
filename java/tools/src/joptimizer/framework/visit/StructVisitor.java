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
 * Interface for classes which are used to execute something while walking through the
 * application class structure.
 *
 * The methods and the order of the methods which are called depends on the implemented algorithm.
 *
 * TODO extend classInfo and methodInfo from common Node class, provide 'parent' to visit methods.
 *
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface StructVisitor {

    void reset();

    void start(ClassInfo classInfo, MethodInfo methodInfo);

    void traverseDown(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext);

    void traverseUp(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext);

    void traverseLeaf(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext);

    void visitClass(ClassInfo classInfo);

    void visitMethod(MethodInfo methodInfo);

    void visitMethodInvocation(MethodInvocation invoke);

    void finish();

}
