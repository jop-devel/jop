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
package com.jopdesign.libgraph.struct;

import java.io.IOException;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface AppClassLoader {

    String getClassPath();

    void setClassPath(String path);

    /**
     * Load a class for a given classname from a file.
     * The packages must be separated by dots.
     *
     * @param appStruct the AppStruct which will be used by the new class.
     * @param className the fully qualified name of the class.
     * @return the classinfo for this class or null if this class should not be loaded.
     * @throws java.io.IOException if classfile cannot be loaded.
     */
    ClassInfo loadClassInfo(AppStruct appStruct, String className) throws IOException;

    /**
     * Create a new class from scratch with the given name.
     * The packages must be separated by dots.
     *
     * @param appStruct      the AppStruct which will be used by the new class.
     * @param className      the fully qualified name of the class.
     * @param superClassName the fully qualified name of the superclass (e.g. 'java.lang.Object').
     * @param isInterface    true if the class should be an interface.
     * @return a new classinfo instance.
     */
    ClassInfo createClassInfo(AppStruct appStruct, String className, String superClassName, boolean isInterface);
}
