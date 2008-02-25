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

    public static final String CONF_SYSTEM_CLASSES = "systemclasses";

    public static final String CONF_NATIVE_CLASS = "nativeclass";

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

    private void loadConfig(URL config) throws ConfigurationException {

        systemClasses = new HashSet();

        Properties props = new Properties();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(config.openStream()));
            props.load(reader);
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
        
    }
}
