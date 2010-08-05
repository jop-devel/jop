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
import com.jopdesign.common.FieldInfo;
import com.jopdesign.common.MethodInfo;

/**
 * A class visitor which traverses all elements of a classInfo. Similar to BCELs DescendingVisitor.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class DescendingClassTraverser implements ClassVisitor {

    private final ClassElementVisitor visitor;

    public DescendingClassTraverser(ClassElementVisitor visitor) {
        this.visitor = visitor;
    }

    public ClassElementVisitor getVisitor() {
        return visitor;
    }

    public boolean visitClass(ClassInfo classInfo) {

        visitor.start();

        if ( !visitor.visitClass(classInfo) ) {
            // TODO we might want to make this configurable
            return false;
        }

        // methods and fields are final, no need to call accept()
        for (FieldInfo f : classInfo.getFields()) {
            visitor.visitField(f);
        }
        for (MethodInfo m : classInfo.getMethods()) {
            visitor.visitMethod(m);
        }

        // TODO visit constants using accept(), visit attributes, ..

        visitor.stop();

        return true;
    }
    
}
