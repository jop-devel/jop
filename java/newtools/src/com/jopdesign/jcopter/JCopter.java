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

package com.jopdesign.jcopter;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.config.BoolOption;
import com.jopdesign.common.config.StringOption;

import java.io.IOException;
import java.util.Properties;

/**
 * User: Stefan Hepp (stefan@stefant.org)
 * Date: 18.05.2010
 */
public class JCopter {

    public static final String VERSION = "0.1";

    public static final StringOption LIBRARY_CLASSES =
            new StringOption("libraries", "comma-separated list of library classes and packages", "");

    public static final StringOption IGNORE_CLASSES =
            new StringOption("ignore", "comma-separated list of classes and packages to ignore", "");

    public static final BoolOption ALLOW_INCOMPLETE_APP =
            new BoolOption("allow-incomplete", "", false);
    
    public static void main(String[] args) {

        // load defaults configuration file
        Properties defaults = null;
        try {
            defaults = AppSetup.loadResourceProps(JCopter.class, "defaults.properties");
        } catch (IOException e) {
            System.out.println("Error loading default configuration file: "+e.getMessage());
            System.exit(1);
        }

        // setup some defaults
        AppSetup setup = new AppSetup(defaults, true);
        setup.addStandardOptions(true, true);
        setup.setUsageInfo("jcopter", "");
        // TODO add version info of WCET and DFA tool to versionInfo text 
        setup.setVersionInfo("jcopter: "+VERSION);

        // setup options
        setup.getConfig().addOption( new BoolOption("useDFA", "run and use results of the DFA") );

        // parse options and config, load application classes
        setup.setupConfig(args);
        setup.setupLogger();

        // setup AppInfo, load app classes
        AppInfo appInfo = setup.getAppInfo();

        // run DFA + WCET

        // run optimizations


        // write results


    }
}
