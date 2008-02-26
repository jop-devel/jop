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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public class ArchConfig {

    private ArchTiming timing;
    private Set systemClasses;
    private String nativeClass;
    private int maxMethodSize;
    private int maxLocalVars;
    private int maxStackSize;
    private int ramReadCycles;
    private int ramWriteCycles;

    public static final String CONF_SYSTEM_CLASSES = "systemclasses";

    public static final String CONF_NATIVE_CLASS = "nativeclass";

    public static final String CONF_MAX_METHOD_SIZE = "maxmethodsize";

    public static final String CONF_MAX_LOCALS = "maxlocalvars";

    public static final String CONF_MAX_STACK_SIZE = "maxstacksize";

    public static final String CONF_RAM_READ_CYCLES = "ramreadcycles";

    public static final String CONF_RAM_WRITE_CYCLES = "ramwritecycles";

    public ArchConfig() {
        systemClasses = Collections.EMPTY_SET;
        nativeClass = "";
        timing = new JopTimings(this);
    }

    public ArchConfig(URL configfile) throws ConfigurationException {
        loadConfig(configfile);
        // TODO get timings from configuration too
        timing = new JopTimings(this);
    }

    public ArchTiming getArchTiming() {
        return timing;
    }

    public String getNativeClassName() {
        return nativeClass;
    }

    /**
     * Get a set of all classes which are used by the system, like
     * the startup- or JVM-classes, as well as internal exception classes.
     *
     * @return a set of classnames as string.
     */
    public Set getSystemClasses() {
        return systemClasses;
    }

    public int getMaxMethodSize() {
        return maxMethodSize;
    }

    public int getMaxLocalVars() {
        return maxLocalVars;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getRamReadCycles() {
        return ramReadCycles;
    }

    public int getRamWriteCycles() {
        return ramWriteCycles;
    }

    private void loadConfig(URL config) throws ConfigurationException {

        systemClasses = new HashSet();

        Properties props = new Properties();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(config.openStream()));
            props.load(reader);
            reader.close();
        } catch (IOException e) {
            throw new ConfigurationException("Could not read configuration file.", e);
        }

        Object sys = props.get(CONF_SYSTEM_CLASSES);
        if ( sys != null && !"".equals(sys.toString()) ) {
            String[] sysclasses = String.valueOf(sys).split(",");
            systemClasses.addAll(Arrays.asList(sysclasses));
        }

        Object nat = props.get(CONF_NATIVE_CLASS);
        if ( nat != null ) {
            nativeClass = String.valueOf(nat);
        } else {
            nativeClass = "";
        }

        maxMethodSize = loadIntConfig(props, CONF_MAX_METHOD_SIZE, 65535);
        maxLocalVars = loadIntConfig(props, CONF_MAX_LOCALS, 1024);
        maxStackSize = loadIntConfig(props, CONF_MAX_STACK_SIZE, 1024);
        ramReadCycles = loadIntConfig(props, CONF_RAM_READ_CYCLES, 1);
        ramWriteCycles = loadIntConfig(props, CONF_RAM_WRITE_CYCLES, 1);
    }

    private int loadIntConfig(Properties props, String name, int defaultValue) throws ConfigurationException {

        Object val = props.get(name);
        if ( val == null ) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid number {"+val+"} for key {"+name+"}.");
        }
    }
}
