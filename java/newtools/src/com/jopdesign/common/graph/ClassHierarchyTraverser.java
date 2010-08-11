/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.graph;

import com.jopdesign.common.ClassInfo;

/**
 * This traverser can be used to visit all sub-classes or ancestors of a class.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassHierarchyTraverser {

    private ClassVisitor visitor;
    private boolean traverseSuper;
    private boolean visitExtensions;
    private boolean visitImplementations;
    private boolean visitInnerClasses;

    public ClassHierarchyTraverser(ClassVisitor visitor, boolean traverseSuper) {
        this.visitor = visitor;
        this.traverseSuper = traverseSuper;
        visitExtensions = true;
        visitImplementations = true;
        visitInnerClasses = false;
    }

    public ClassVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(ClassVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean doTraverseSuper() {
        return traverseSuper;
    }

    /**
     * Set direction of traverser to all super/outer classes or all sub/inner classes.
     *
     * @param traverseSuper if true, traverse superclasses instead of subclasses.
     */
    public void setTraverseSuper(boolean traverseSuper) {
        this.traverseSuper = traverseSuper;
    }

    public boolean doVisitExtensions() {
        return visitExtensions;
    }

    public void setVisitExtensions(boolean visitExtensions) {
        this.visitExtensions = visitExtensions;
    }

    public boolean doVisitInnerClasses() {
        return visitInnerClasses;
    }

    public void setVisitInnerClasses(boolean visitInnerClasses) {
        this.visitInnerClasses = visitInnerClasses;
    }

    public boolean doVisitImplementations() {
        return visitImplementations;
    }

    /**
     * Visit only the same type (class or interface) as the root class.
     * 
     * @param visitImplementations if true, follow only extensions, else follow both classes and interfaces.
     */
    public void setVisitImplementations(boolean visitImplementations) {
        this.visitImplementations = visitImplementations;
    }


    public void traverse(ClassInfo root) {
        if (traverseSuper) {
            traverseUp(root);
        } else {
            traverseDown(root);
        }
    }

    private void traverseDown(ClassInfo classInfo) {
        if ( !visitor.visitClass(classInfo) ) {
            return;
        }

        if ( visitImplementations || visitExtensions ) {
            for (ClassInfo c : classInfo.getDirectSubclasses()) {
                // if extensions only, only go down if the subclass is a class for classes or
                // if the subclass is an interface for interfaces.
                if ( (visitExtensions && c.isInterface() == classInfo.isInterface()) ||
                     (visitImplementations && !c.isInterface() && classInfo.isInterface()) )
                {
                    traverseDown(c);
                }
            }
        }
        if ( visitInnerClasses ) {
            for (ClassInfo c : classInfo.getDirectNestedClasses()) {
                traverseDown(c);
            }
        }

        visitor.finishClass(classInfo);
    }

    private void traverseUp(ClassInfo classInfo) {
        if ( !visitor.visitClass(classInfo) ) {
            return;
        }

        // we never visit Object from interfaces
        if ( visitExtensions && !classInfo.isInterface()) {
            ClassInfo superClass = classInfo.getSuperClassInfo();
            if ( superClass != null ) {
                traverseUp(superClass);
            }
        }

        if ( (visitExtensions && classInfo.isInterface()) || 
             (visitImplementations && !classInfo.isInterface()) )
        {
            for (ClassInfo i : classInfo.getInterfaces()) {
                traverseUp(i);
            }
        }

        if (visitInnerClasses) {
            ClassInfo outerClass = classInfo.getEnclosingClassInfo();
            if ( outerClass != null ) {
                traverseUp(outerClass);
            }
        }

        visitor.finishClass(classInfo);
    }


}
