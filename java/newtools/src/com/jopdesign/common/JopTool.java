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
import com.jopdesign.common.config.OptionGroup;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface JopTool<T extends AttributeManager> {

    String getToolVersion();

    T getAttributeManager();

    /**
     * Get a properties file containing defaults which should be added to the configuration.
     *
     * @see AppSetup#loadResourceProps(Class, String)
     * @return default properties or null if no additional defaults are used.
     * @throws IOException on loading errors.
     */
    Properties getDefaultProperties() throws IOException;

    void registerOptions(OptionGroup options);

    void onSetupConfig(AppSetup setup) throws Config.BadConfigurationException;

    void run(AppSetup setup);
}
