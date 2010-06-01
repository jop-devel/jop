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
public class LoggerConfig {

    public LoggerConfig() {
    }

    /**
     * Setup the logger using configuration options.
     * You may want to call {@link #setReportLoggers(File, File, Level)}
     * first to define the report loggers.
     *
     * @param config the config settings to use.
     */
    public void setupLogger(Config config) {
        ConsoleAppender defaultAppender = new ConsoleAppender(new PatternLayout("[%c{1}] %m\n"), "System.err");
		defaultAppender.setName("ACONSOLE");
		if(config.getOption(Config.DEBUG)) {
			defaultAppender.setThreshold(Level.INFO);
		} else {
			defaultAppender.setThreshold(Level.WARN);
		}
		Logger.getRootLogger().addAppender(defaultAppender);

        PropertyConfigurator.configure(config.getProperties());
	}

	public void setReportLoggers(File errorLog, File infoLog, Level consoleLevel)
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
