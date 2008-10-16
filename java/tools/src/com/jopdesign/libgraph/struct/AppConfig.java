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

    /**
     * Check if referenced classes should be loaded after the initial class loading phase.
     * 
     * @return true if implicit class loading should be possible after the first loading phase. 
     */
    boolean doLoadOnDemand();

    /**
     * Check if incomplete code should be allowed or if an error should be produced
     * if a class which is not excluded cannot be loaded (i.e. this has an effect
     * only on classes which are not excluded by {@link #doExcludeClassName(String)},
     * {@link #isNativeClassName(String)} and {@link #isLibraryClassName(String)}).
     *
     * @return true if an error should be produced if loading a not excluded class fails.
     */
    boolean doAllowIncompleteCode();

    /**
     * Check if the given classname should be loaded or not.<br>
     * This check does not need to check if the class is a library class
     * or a native class. To check for all possible reasons, use {@link AppStruct#doIgnoreClassName(String)}. 
     *
     * @param className the classname to check.
     * @return true for classes which should not be loaded due to configuration settings.
     */
    boolean doExcludeClassName(String className);

    /**
     * Check if this class is implemented by the JVM. As
     * these classes cannot be loaded, they must always be ignored.
     *
     * @param className the classname of the class to test.
     * @return true for classes implemented by the native JVM.
     */
    boolean isNativeClassName(String className);

    /**
     * Check if this class is part of a library. Library code can be used by the
     * application, but the library must not use code from the application.
     * Library code should be ignored.
     *
     * @param className the classname to check.
     * @return true if the class is part of a library (i.e. code that is not related to the application code).
     */
    boolean isLibraryClassName(String className);
}
