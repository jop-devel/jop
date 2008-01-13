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

import joptimizer.framework.actions.ActionException;
import com.jopdesign.libgraph.struct.*;

import java.util.*;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class MethodInvokationTraverser implements StructTraverser {

    private StructVisitor visitor;

    private int maxDepth;
    private Set ignore;

    public MethodInvokationTraverser(StructVisitor visitor) {
        this.visitor = visitor;
        this.ignore = new HashSet();
    }

    public StructVisitor getVisitor() {
        return visitor;
    }

    public void addIgnorePrefix(String prefix) {
        ignore.add(prefix);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void traverse(ClassInfo startClass, MethodInfo startMethod) throws ActionException {

        visitor.start(startClass, startMethod);

        followMethod(startClass, startMethod, false, 0);

        visitor.finish();

    }

    private void followMethod(ClassInfo classInfo, MethodInfo methodInfo, boolean hasNext, int depth) throws ActionException {

        visitor.visitClass(classInfo);
        visitor.visitMethod(methodInfo);

        // visit all invokations and follow them down
        MethodCode code = methodInfo.getMethodCode();
        List invokedMethods = null;
        try {
            invokedMethods = code != null ? code.getInvokedMethods() : Collections.EMPTY_LIST;
        } catch (TypeException e) {
            throw new ActionException("Could not get method invocation list.",e);
        }
        List visibleMethods = new ArrayList(invokedMethods.size());

        // need to filter visible invokations first to provide hasNext correctly to visitors.
        for (Iterator it = invokedMethods.iterator(); it.hasNext();) {
            MethodInvokation invoke = (MethodInvokation) it.next();
            if ( doFollowInvokation(invoke, depth)) {
                visibleMethods.add(invoke);
            }
        }

        if ( visibleMethods.size() == 0 ) {
            visitor.traverseLeaf(classInfo, methodInfo, hasNext);
        } else {
            visitor.traverseDown(classInfo, methodInfo, hasNext);

            for (Iterator it = visibleMethods.iterator(); it.hasNext();) {
                MethodInvokation invoke = (MethodInvokation) it.next();

                visitor.visitMethodInvokation(invoke);

                followMethod(invoke.getInvokedClass(), invoke.getInvokedMethod(), it.hasNext(), depth + 1);
            }

            visitor.traverseUp(classInfo, methodInfo, hasNext);
        }
    }

    private boolean doFollowInvokation(MethodInvokation invoke, int depth) {

        if ( depth > maxDepth && maxDepth > 0 ) {
            return false;
        }

        // TODO optional check if invoked method has been visited

        for (Iterator it = ignore.iterator(); it.hasNext();) {
            String ign = (String) it.next();
            if (invoke.getInvokedClass().getClassName().startsWith(ign)) {
                return false;
            }
        }

        return true;
    }
}
