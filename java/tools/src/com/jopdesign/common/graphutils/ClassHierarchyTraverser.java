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

package com.jopdesign.common.graphutils;

import com.jopdesign.common.ClassInfo;

/**
 * This traverser can be used to visit all sub-classes or ancestors of a class.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ClassHierarchyTraverser {

    private ClassVisitor visitor;
    private boolean visitExtensions;
    private boolean visitImplementations;
    private boolean visitInnerClasses;

    /**
     * Create a new traverser for the given visitor.
     * By default, traverse down all subclasses but not the nested classes.
     *
     * @param visitor the visitor to use.
     */
    public ClassHierarchyTraverser(ClassVisitor visitor) {
        this.visitor = visitor;
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

    public boolean doVisitExtensions() {
        return visitExtensions;
    }

    public boolean doVisitImplementations() {
        return visitImplementations;
    }

    /**
     * Set which subclasses of a class to visit.
     *
     * @param visitExtensions if true, visit extensions, i.e. classes of the same type (class or interface)
     * @param visitImplementations if true, visit implementations, i.e. classes which implement an interface
     */
    public void setVisitSubclasses(boolean visitExtensions, boolean visitImplementations) {
        this.visitExtensions = visitExtensions;
        this.visitImplementations = visitImplementations;
    }

    public boolean doVisitInnerClasses() {
        return visitInnerClasses;
    }

    public void setVisitInnerClasses(boolean visitInnerClasses) {
        this.visitInnerClasses = visitInnerClasses;
    }

    /**
     * Visit the given class, all its subclasses and nested classes, depending on the set modes.
     * If the visitor returns false for a class, the subclasses/nested classes of this class are not
     * visited and the traverser continues with its next sibling.
     * <p>This traverser does not check if an interface has already been visited.</p>
     *
     * @param classInfo the class to visit first
     */
    public void traverseDown(ClassInfo classInfo) {
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

    /**
     * Visit the superclasses, its implemented interfaces and enclosing classes of a class.
     * If the visitor returns false for a class, the superclasses/interfaces/enclosing classes of this class are not
     * visited and the traverser continues with its next sibling.
     * <p>
     * For classes, the superclass is visited if extensions should be visited, and its implemented interfaces are
     * visited if implementations should be visited.
     * For interfaces, the extended interfaces are visited (this traverser does not check if an interface has
     * already been visited), the implementations-flag is ignored.
     * </p>
     *
     * @param classInfo the class to visit first
     */
    public void traverseUp(ClassInfo classInfo) {
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
