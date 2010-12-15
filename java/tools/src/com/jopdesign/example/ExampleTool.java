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
import com.jopdesign.common.JopTool;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.IntegerOption;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.misc.NamingConflictException;

import java.io.IOException;
import java.util.Properties;

/**
 * Just an example program to demonstrate some features of the common library.
 * 
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class ExampleTool implements JopTool<ExampleManager> {

    public static final String VERSION = "0.1";

    private final ExampleManager manager;

    public ExampleTool() {
        manager = new ExampleManager();
    }

    public String getToolVersion() {
        return VERSION;
    }

    public ExampleManager getEventHandler() {
        return manager;
    }

    public Properties getDefaultProperties() throws IOException {
        // we could also provide defaults in a file to overwrite the values in registerOptions()
        // return AppSetup.loadResourceProps(ExampleTool.class, "defaults.properties");
        // .. but we don't use it here
        return null;
    }

    public void registerOptions(OptionGroup options) {
        options.addOption( new BooleanOption("flag", "switch some stuff on or off") );
        options.addOption( new IntegerOption("new", "create n new classes", 2).setMinMax(0,10) );
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

        // setup some defaults
        AppSetup setup = new AppSetup();
        setup.setUsageInfo("example", "This is an example application just to show off.");
        setup.setVersionInfo("The version of this whole application is 0.1");

        setup.addStandardOptions(true, true);
        setup.addWriteOptions(true);
        // set the name of the (optional) user-provided config file
        setup.setConfigFilename("example.properties");

        ExampleTool example = new ExampleTool();
        setup.registerTool("example", example);

        // parse options and setup config
        String[] rest = setup.setupConfig(args);
        setup.setupLogger(false);

        // setup classpath, roots and main method, load transitive hull
        setup.setupAppInfo(rest, true);

        // write results
        setup.writeClasses();
    }
}
