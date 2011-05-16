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

package com.jopdesign.common;

import com.jopdesign.common.config.Config;

/**
 * This class can be used to initialize a test environment.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class TestFramework {

    private AppSetup appSetup;

    public AppSetup setupAppSetup() {
        return setupAppSetup(".", null);
    }

    public AppSetup setupAppSetup(String classPath, String outputDir) {

        appSetup = new AppSetup();
        appSetup.addStandardOptions(true, true, true);

        appSetup.getConfig().setOption(Config.CLASSPATH, classPath);
        appSetup.getConfig().setOption(Config.VERBOSE, true);
        appSetup.getConfig().setOption(Config.DEBUG,   true);

        if (outputDir != null) {
            appSetup.addWriteOptions(true);
            appSetup.getConfig().setOption(Config.WRITE_PATH, outputDir);
        }

        appSetup.setupLogger(outputDir != null);

        return appSetup;
    }

    /**
     * Setup AppInfo and load the classes. Call {@link #setupAppSetup(String,String)} first.
     *
     * @param rootMethod the root method name
     * @param loadHull if true, load the transitive hull of the root method.
     */
    public AppInfo setupAppInfo(String rootMethod, boolean loadHull) {
        appSetup.setupAppInfo(new String[]{rootMethod}, loadHull);
        return appSetup.getAppInfo();
    }

    public AppSetup getAppSetup() {
        return appSetup;
    }

    public AppInfo getAppInfo() {
        return appSetup.getAppInfo();
    }

}
