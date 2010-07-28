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
import com.jopdesign.common.Module;
import com.jopdesign.common.config.BoolOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.IntOption;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.misc.NamingConflictException;

import java.io.IOException;
import java.util.Properties;

/**
 * Just an example program to demonstrate some features of the common library.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExampleTool implements Module<ExampleManager> {

    public static final String VERSION = "0.1";

    private final ExampleManager manager;

    public ExampleTool() {
        manager = new ExampleManager();
    }

    public String getModuleVersion() {
        return VERSION;
    }

    public ExampleManager getAttributeManager() {
        return manager;
    }

    public Properties getDefaultProperties() {
        return null;
    }

    public void registerOptions(OptionGroup options) {
        options.addOption( new BoolOption("flag", "switch some stuff on or off") );
        options.addOption( new IntOption("new", "create n new classes", 2).setMinMax(0,10) );
    }

    public void onSetupConfig(AppSetup setup) throws Config.BadConfigurationException {
    }

    public void run(AppSetup setup) {

        AppInfo appInfo = setup.getAppInfo();

        // access and modify some classes
        System.out.println("field of main class: " + manager.getMyField(appInfo.getMainMethod().getClassInfo()) );
        for (ClassInfo root : appInfo.getRootClasses() ) {
            System.out.println("field of root: " + manager.getMyField(root) );
        }

        // create a new class and new methods
        try {
            ClassInfo newCls = appInfo.createClass("MyTest", appInfo.getClassRef("java.lang.Object"), false);
        } catch (NamingConflictException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        ExampleTool example = new ExampleTool();

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
        setup.setUsageInfo("example", "An example application");
        setup.setVersionInfo("This is the version of this application");

        setup.addStandardOptions(true, true);
        setup.addWriteOptions(true);

        setup.registerModule("example", example);

        // parse options and setup config
        String[] rest = setup.setupConfig(args, "example.properties");
        setup.setupLogger();

        // setup classpath, roots and main method, load transitive hull
        setup.setupAppInfo(rest, true);

        // write results
        setup.writeClasses();
    }
}
