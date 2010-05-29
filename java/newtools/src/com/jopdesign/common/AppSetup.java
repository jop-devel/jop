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
 * This class is a helper used for creating and setting up the AppInfo class as well as for common
 * configuration tasks.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AppSetup {

    private Config config;
    private AppInfo appInfo;
    private String[] args;


    public AppSetup(String[] args) {
        this.args = args;
        config = new Config();
    }

    public Config getConfig() {
        return config;
    }

    /**
     * Create and initialize a new AppInfo using the current options and return it.
     * @return a new, initialized AppInfo.
     */
    public AppInfo loadAppStruct() {

        appInfo = new AppInfo();

        return appInfo;
    }

    /**
     * Get the last loaded AppInfo.
     * @return the last loaded AppInfo, or null if not yet loaded.
     */
    public AppInfo getAppStruct() {
        return appInfo;
    }
}
