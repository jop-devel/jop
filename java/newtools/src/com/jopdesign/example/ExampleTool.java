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

package com.jopdesign.example;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.config.BoolOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.IntOption;
import com.jopdesign.common.tools.ClassWriter;
import com.jopdesign.common.tools.TransitiveHullLoader;

import java.io.IOException;
import java.util.Properties;

/**
 * Just an example program to demonstrate some features of the common library.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExampleTool {
    public static final String VERSION = "0.1";
   
    public static void main(String[] args) {

        // load defaults configuration file
        Properties defaults = null;
        try {
            defaults = AppSetup.loadResourceProps(ExampleTool.class, "defaults.properties");
        } catch (IOException e) {
            System.out.println("Error loading default configuration file: "+e.getMessage());
            System.exit(1);
        }

        // setup some defaults
        AppSetup setup = new AppSetup(defaults, true);
        setup.addStandardOptions(true, true);
        setup.setUsageInfo("example", "An example application");
        setup.setVersionInfo("example: "+VERSION);

        // setup options
        setup.getConfig().addOption( Config.WRITE_PATH );
        setup.getConfig().addOption( new BoolOption("flag", "switch some stuff on or off") );
        setup.getConfig().addOption( new IntOption("new", "create n new classes", 2).setMinMax(0,10) );

        // parse options and to config
        String[] rest = setup.setupConfig("example.properties", args);
        setup.setupLogger();

        // init AppInfo
        ExampleManager myMgr = new ExampleManager();

        AppInfo appInfo = setup.getAppInfo();
        appInfo.registerManager("example", myMgr);

        // setup classpath, roots and main method
        setup.setupAppInfo(rest);

        new TransitiveHullLoader(appInfo).load();

        // access and modify some classes
        System.out.println("field of main class: " + myMgr.getMyField(appInfo.getMainMethod().getClassInfo()) );
        for (ClassInfo root : appInfo.getRootClasses() ) {
            System.out.println("field of root: " + myMgr.getMyField(root) );
        }

        // create a new class and new methods
        ClassInfo newCls = appInfo.createClass("MyTest", appInfo.getClassRef("java.lang.Object"));

        // write results
        new ClassWriter(appInfo).writeToDir(setup.getConfig().getOption(Config.WRITE_PATH));
    }

}
