/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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

package com.jopdesign.common.logger;

import com.jopdesign.common.config.Config;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;


/**
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp <stefan@stefant.org>
 */
public class LogConfig {

    /////////////////////////////////////////////////////////////////////
    // Various standard logging keys
    /////////////////////////////////////////////////////////////////////

    /**
     * Logger for config related tasks
     */
    public static final String LOG_CONFIG = "common.config";
    /**
     * Logger for everything related to AppInfo
     */
    public static final String LOG_APPINFO = "common.appinfo";
    /**
     * Logger for class loading tasks
     */
    public static final String LOG_LOADING = "common.appinfo.loading";
    /**
     * Logger for output writing tasks
     */
    public static final String LOG_WRITING = "common.appinfo.writing";
    /**
     * Logger for all AppInfo structure classes (Class, Method, Field, ..)
     */
    public static final String LOG_STRUCT = "common.appinfo.struct";
    /**
     * Logger for code handling related tasks
     */
    public static final String LOG_CODE = "common.appinfo.code";


    /**
     * Setup the logger using configuration options.
     * You may want to call {@link #setReportLoggers(File, File)}
     * first to define the report loggers.
     *
     * @param config the config settings to use.
     */
    public void setupLogger(Config config) {

        boolean verbose = config.getOption(Config.VERBOSE);
        boolean debug = config.getOption(Config.DEBUG);
        boolean quiet = config.getOption(Config.QUIET);

        ConsoleAppender defaultAppender;

        if (verbose) {
            defaultAppender = new ConsoleAppender(new PatternLayout("%r [%c] %m\n"), "System.err");
        } else {
            defaultAppender = new ConsoleAppender(new ConsoleLayout("%r [%c{1}] %m\n"), "System.err");
        }
        defaultAppender.setName("ACONSOLE");

        Level defaultLevel = Level.INFO;

        if (debug) {
			defaultLevel = Level.DEBUG;
        } else if ( quiet ) {
            defaultLevel = Level.WARN;
		}
        defaultAppender.setThreshold(defaultLevel);

		Logger.getRootLogger().addAppender(defaultAppender);
        Logger.getRootLogger().setLevel(defaultLevel);

        // TODO if Config option is used, add html-report logger (or add it anyway?)

        PropertyConfigurator.configure(config.getProperties());
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void setReportLoggers(File errorLog, File infoLog)
		throws IOException
    {
			errorLog.delete();
			FileAppender eapp = new FileAppender(new HTMLLayout(), errorLog.getPath());
			eapp.setName("AERROR");
			eapp.setThreshold(Level.ERROR);
			infoLog.delete();
			FileAppender iapp = new FileAppender(new HTMLLayout(), infoLog.getPath());
			iapp.setThreshold(Level.ALL);
			iapp.setName("AINFO");
			Logger.getRootLogger().addAppender(eapp);
			Logger.getRootLogger().addAppender(iapp);
	}
}
