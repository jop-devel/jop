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

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of visitors, passes on all visitor interface method calls to all visitors.
 * 
 * TODO better name?
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class StructVisitorList implements StructVisitor {

    List visitors;

    public StructVisitorList() {
        this.visitors = new ArrayList();
    }
    
    public void addVisitor(StructVisitor visitor) {
        visitors.add(visitor);
    }

    public void reset() {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).reset();
        }
    }

    public void start(ClassInfo classInfo, MethodInfo methodInfo) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).start(classInfo, methodInfo);
        }
    }

    public void traverseDown(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).traverseDown(classInfo, methodInfo, hasNext);
        }
    }

    public void traverseUp(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).traverseUp(classInfo, methodInfo, hasNext);
        }
    }

    public void traverseLeaf(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).traverseLeaf(classInfo, methodInfo, hasNext);
        }
    }

    public void visitClass(ClassInfo classInfo) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).visitClass(classInfo);
        }
    }

    public void visitMethod(MethodInfo methodInfo) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).visitMethod(methodInfo);
        }
    }

    public void visitMethodInvocation(MethodInvocation invoke) {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).visitMethodInvocation(invoke);
        }
    }

    public void finish() {
        for (int i = 0; i < visitors.size(); i++) {
            ((StructVisitor) visitors.get(i)).finish();
        }
    }
}
