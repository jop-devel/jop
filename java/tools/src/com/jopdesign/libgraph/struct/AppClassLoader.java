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

    /**
     * Create a new class for a given classname.
     * The packages must be separated by dots.
     *
     * @param className the fully qualified name of the class.
     * @return the classinfo for this class or null if this class should not be loaded.
     * @throws TypeException if the class cannot be found.
     * @throws java.io.IOException if classfile cannot be loaded.
     */
    ClassInfo createClassInfo(String className) throws TypeException, IOException;

}
