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
package joptimizer.config;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ArchConfig {

    private ArchTiming timing;

    public ArchConfig(String filename) {
        timing = new JopTimings(this);
    }

    public ArchTiming getArchTiming() {
        return timing;
    }

    public String getNativeClassName() {
        return "com.jopdesign.sys.Native";
    }

    public String getStartupClassName() {
        return "com.jopdesign.sys.Startup";
    }

    public String getJvmClassName() {
        return "com.jopdesign.sys.JVM";
    }

    public String getHelpClassName() {
        return "com.jopdesign.sys.JVMHelp";
    }

    /**
     * Get a set of all classes which are used by the system, like
     * the startup- or JVM-classes, as well as internal exception classes.
     *
     * @return a set of classnames as string.
     */
    public Set getSystemClasses() {
        Set systemClasses = new HashSet();

        systemClasses.add(getStartupClassName());
        systemClasses.add(getJvmClassName());
        systemClasses.add(getHelpClassName());

        //systemClasses.add("java.lang.NullPointerException");

        return systemClasses;
    }

    public int getMaxMethodSize() {
        return 256;
    }

    public int getMaxLocalVars() {
        return 31;
    }

    public int getMaxStackSize() {
        return 31;
    }

    public int getRamReadCycles() {
        return 2;
    }

    public int getRamWriteCycles() {
        return 2;
    }
}
