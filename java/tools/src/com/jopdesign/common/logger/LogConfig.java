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
import org.apache.log4j.Appender;
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
     * Logger for control flow graph and related classes
     */
    public static final String LOG_CFG = "common.appinfo.code.cfg";

    public static final String LOG_GRAPH = "common.graph";
    private File errorLog;
    private File infoLog;

    public LogConfig() {
    }

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

        Level defaultLevel = Level.INFO;
        Level rootLevel = Level.DEBUG;
        if (debug) {
	    defaultLevel = Level.DEBUG;
            rootLevel = Level.ALL;
        } else if ( quiet ) {
            defaultLevel = Level.WARN;
	}

        FilteredConsoleAppender defaultAppender;

        if (verbose) {
            defaultAppender = new FilteredConsoleAppender(new PatternLayout("%5r %-5p [%c] %m\n"), "System.err");
        } else {
            defaultAppender = new FilteredConsoleAppender(new ConsoleLayout("%5r %-5p [%c{1}] %m\n"), "System.err");
        }
        defaultAppender.setName("ACONSOLE");
        defaultAppender.setThreshold(defaultLevel);
        defaultAppender.setConfig(config);
        defaultAppender.addWarnOnly(config.getOption(Config.SHOW_WARN_ONLY));
        defaultAppender.addInfoOnly(config.getOption(Config.SHOW_INFO_ONLY));

	Logger.getRootLogger().addAppender(defaultAppender);
        Logger.getRootLogger().setLevel(rootLevel);

        PropertyConfigurator.configure(config.getProperties());
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void setReportLoggers(File errorLog, File infoLog)
		throws IOException
    {
        this.errorLog = errorLog;
        this.infoLog = infoLog;

        if ( errorLog != null ) {
            errorLog.delete();
            FileAppender eapp = new FileAppender(new HTMLLayout(), errorLog.getPath());
            eapp.setName("AERROR");
            eapp.setThreshold(Level.WARN);
            Logger.getRootLogger().addAppender(eapp);
        }

        if ( infoLog != null ) {
            infoLog.delete();
            FileAppender iapp = new FileAppender(new HTMLLayout(), infoLog.getPath());
            // TODO maybe make level of info logger configurable (one of INFO, DEBUG, ALL)?
            iapp.setThreshold(Level.ALL);
            iapp.setName("AINFO");
            
            Logger.getRootLogger().addAppender(iapp);
        }
    }

    public static void stopLogger() {

        String[] names = {"ACONSOLE", "AERROR", "AINFO"};

        for ( String appender : names) {
            Appender app = Logger.getRootLogger().getAppender(appender);
            if ( app == null ) {
                continue;
            }
            app.close();
            Logger.getRootLogger().removeAppender(app);
        }
    }

    public File getErrorLogFile() {
        return errorLog;
    }

    public File getInfoLogFile() {
        return infoLog;
    }
}
