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

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public interface AppConfig {

    boolean doLoadOnDemand();

    boolean doAllowIncompleteCode();

    /**
     * check if the given classname should be loaded or not.
     *
     * @param className the classname to check.
     * @return the reason as string if the class should be ignored, else null.
     */
    String doExcludeClassName(String className);

    /**
     * Check if this class is implemented by the JVM. As
     * these classes cannot be loaded, they must always be ignored.
     * @param className the classname of the class to test.
     * @return true for classes implemented by the native JVM.
     */
    boolean isNativeClassName(String className);
}
