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
 * Interface to setup all JOP-tools.
 * <p>
 * All tools should provide a class which implements this interface. The class will then
 * be used by {@link AppSetup} to initialize the tool.
 * </p><p>
 * Each tool should also provide a custom {@link AppEventHandler} which can be used to retrieve
 * attributes and flow-facts managed by this tool from classes, methods, code and fields.
 * </p>
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public interface JopTool<T extends AppEventHandler> {

    /**
     * Get a version string for this tool (excluding the tool name) which will be displayed
     * with --version.
     * @return the version number of this tool.
     */
    String getToolVersion();

    /**
     * Get the tool-specific event handler for this tool. The handler should be used
     * to set and access tool-specific attributes.
     *
     * @return a custom event handler.
     */
    T getEventHandler();

    /**
     * Get properties containing defaults which should be added to the configuration.
     * <p>
     * The properties override the default values of the config options, but can be overwritten
     * by application specific defaults provided at {@link AppSetup#AppSetup(Properties, boolean)}
     * and by commandline options (including loading a user config file).
     * </p>
     *
     * @see AppSetup#loadResourceProps(Class, String)
     * @return default properties or null if no additional defaults are used.
     * @throws IOException on loading errors.
     */
    Properties getDefaultProperties() throws IOException;

    /**
     * Register the options of this tool to the given optiongroup.
     * @param options the optiongroup where this tool is expected to add its options.
     */
    void registerOptions(OptionGroup options);

    /**
     * Called by {@link AppSetup#setupConfig(String[])} after the configuration has been loaded.
     * The tool should use the config values from AppSetup to check the configuration, load its options
     * and initialize the tool.
     *
     * @param setup the AppSetup used to initialize this app
     * @throws Config.BadConfigurationException if there is something rotten in the config or
     *                                          if the tool cannot be initialized
     */
    void onSetupConfig(AppSetup setup) throws Config.BadConfigurationException;

    /**
     * Run this tool with the current options.
     * <p>
     * This is mainly a convenience method provided by the tool so that other tools can execute
     * this tool when needed.
     * </p>
     *
     * @param config the config to use
     */
    void run(Config config);
}
