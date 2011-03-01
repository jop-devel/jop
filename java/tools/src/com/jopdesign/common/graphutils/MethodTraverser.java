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

package com.jopdesign.common.graphutils;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;

/**
 * This is just a simple helper class to iterate over all methods using
 * {@link AppInfo#iterate(ClassVisitor)}.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodTraverser implements ClassVisitor {

    public interface MethodVisitor {
        void visitMethod(MethodInfo method);
    }

    private final MethodVisitor visitor;
    private final boolean skipNoCode;

    /**
     * @param visitor the visitor to apply to all methods
     * @param skipNoCode if true, do not visit abstract or native methods
     */
    public MethodTraverser(MethodVisitor visitor, boolean skipNoCode) {
        this.visitor = visitor;
        this.skipNoCode = skipNoCode;
    }

    @Override
    public boolean visitClass(ClassInfo classInfo) {
        for (MethodInfo m : classInfo.getMethods()) {
            if (skipNoCode && !m.hasCode()) continue;
            visitor.visitMethod(m);
        }
        return true;
    }

    @Override
    public void finishClass(ClassInfo classInfo) {
    }
}
