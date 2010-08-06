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
    private boolean visitSuper;
    private boolean extensionsOnly;

    public ClassHierarchyTraverser(ClassVisitor visitor, boolean visitSuper) {
        this.visitor = visitor;
        this.visitSuper = visitSuper;
        extensionsOnly = false;
    }

    public ClassVisitor getVisitor() {
        return visitor;
    }

    public void setVisitor(ClassVisitor visitor) {
        this.visitor = visitor;
    }

    public boolean doVisitSuper() {
        return visitSuper;
    }

    public void setVisitSuper(boolean visitSuper) {
        this.visitSuper = visitSuper;
    }

    public boolean doExtensionsOnly() {
        return extensionsOnly;
    }

    /**
     * Visit only the same type (class or interface) as the root class.
     * 
     * @param extensionsOnly if true, follow only extensions, else follow both classes and interfaces.
     */
    public void setExtensionsOnly(boolean extensionsOnly) {
        this.extensionsOnly = extensionsOnly;
    }


    public void traverse(ClassInfo root) {
        if ( visitSuper ) {
            traverseUp(root);
        } else {
            traverseDown(root);
        }
    }

    private void traverseDown(ClassInfo classInfo) {
        if ( !visitor.visitClass(classInfo) ) {
            return;
        }

        for (ClassInfo c : classInfo.getDirectSubclasses()) {
            // if extensions only, only go down if the subclass is a class for classes or
            // if the subclass is an interface for interfaces.
            if ( !extensionsOnly || c.isInterface() == classInfo.isInterface() ) {
                traverseDown(c);
            }
        }

        visitor.finishClass(classInfo);
    }

    private void traverseUp(ClassInfo classInfo) {
        if ( !visitor.visitClass(classInfo) ) {
            return;
        }

        if ( !extensionsOnly || !classInfo.isInterface() ) {
            ClassInfo superClass = classInfo.getSuperClassInfo();
            if ( superClass != null ) {
                traverseUp(superClass);
            }
        }

        if ( !extensionsOnly || classInfo.isInterface() ) {
            for (ClassInfo i : classInfo.getInterfaces()) {
                traverseUp(i);
            }
        }

        visitor.finishClass(classInfo);
    }


}
