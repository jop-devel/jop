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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;

/**
 * This is an interface for actions which are applied to one or more classes.
 *
 * <p>This does not really used to implement a visitor pattern, but in most cases we
 * do not need double dispatch. Instead, we use this interface for pre- and post-order traversal.
 * </p>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface ClassVisitor {

    /**
     * Visit a class before recursion. The concrete meaning of the return value depends on the used traverser.
     *
     * <p>For recursive traversers, returning false skips descending down for this class. For
     * other traversers such as {@link AppInfo#iterate(ClassVisitor)} returning false aborts
     * traversion.</p> 
     *
     * @param classInfo the class to perform some acton on.
     * @return true to continue, false to abort iteration or recursion.
     */
    boolean visitClass(ClassInfo classInfo);

    /**
     * Finish visiting a class. Called after all children have been visited, but only if
     * {@link #visitClass(ClassInfo)} has returned true.
     *
     * @param classInfo the class for which recursion is finished.
     */
    void finishClass(ClassInfo classInfo);

}
