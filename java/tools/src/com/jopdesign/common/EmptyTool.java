/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
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
import com.jopdesign.common.config.Config.BadConfigurationException;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class EmptyTool<T extends AppEventHandler> implements JopTool<T> {

    private final String version;

    protected EmptyTool(String version) {
        this.version = version;
    }

    @Override
    public String getToolVersion() {
        return version;
    }

    @Override
    public T getEventHandler() {
        return null;
    }

    @Override
    public Properties getDefaultProperties() throws IOException {
        return null;
    }

    @Override
    public void registerOptions(Config config) {
    }

    @Override
    public void onSetupConfig(AppSetup setup) throws BadConfigurationException {
    }

    @Override
    public void onSetupRoots(AppSetup setup, AppInfo appInfo) throws BadConfigurationException {
    }

    @Override
    public void onSetupAppInfo(AppSetup setup, AppInfo appInfo) throws BadConfigurationException {
    }
}
