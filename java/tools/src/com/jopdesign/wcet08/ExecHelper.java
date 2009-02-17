/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2009, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet08;

import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.config.Option;
import com.jopdesign.wcet08.config.Config.BadConfigurationException;

/**
 * Helper class for command line executables.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ExecHelper {
	private Class<?> execClass;
	private String configFile;
	private Logger tlLogger;
	private Config config;
	private String version;

	public ExecHelper(Class<?> clazz, String version, Logger topLevelLogger, String configFile) {
		this.execClass = clazz;
		this.version = version;
		this.tlLogger = topLevelLogger;
		this.configFile = configFile;
	}
	public void initTopLevelLogger() {
		ConsoleAppender consoleApp = new ConsoleAppender(new PatternLayout(), ConsoleAppender.SYSTEM_ERR);
		consoleApp.setName("TOP-LEVEL");
		consoleApp.setThreshold(Level.INFO);
		consoleApp.setLayout(new PatternLayout("["+execClass.getSimpleName()+" %-6rms] %m%n"));
		tlLogger.addAppender(consoleApp);		
	}
	/**
	 * @param args
	 */
	public void loadConfig(String[] args) {
		try {
			String[] argsrest = Config.load(System.getProperty(configFile),args);
			config = Config.instance();
			if(config.helpRequested()) exitUsage(false);
			if(config.versionRequested()) exitVersion();
			if(argsrest.length != 0) {
				exitUsage("Unknown command line arguments: "+Arrays.toString(argsrest));
			}
			config.checkOptions();
			tlLogger.info("Configuration:\n"+config.dumpConfiguration(4));
			tlLogger.info("java.library.path: "+System.getProperty("java.library.path"));
		} catch(BadConfigurationException e) { 
			exitUsage(e.getMessage());					
		} catch (Exception e) {
			e.printStackTrace();
			bail("Loading configuration failed");
		}
	}
	public void exitVersion() {
		printSep();
		System.err.println(""+this.execClass);
		System.err.println("Version: "+version);
		printSep();
		System.exit(0);		
	}
	public void exitUsage(String reason) {
		printSep();
		System.err.println("[USAGE ERROR] "+reason);
		printSep();
		exitUsage(true);
	}

	public void exitUsage(boolean dumpConfig) {
		System.err.println(
			MessageFormat.format("" +
					"Usage:\n  java -D{0}=file://<path-to-config> {1} [OPTIONS]", 
					configFile, execClass.getCanonicalName()));
		System.err.println(
			MessageFormat.format(
				"Example:\n    java -D{0}=file:///home/jop/myconf.props {1} -{2} {3}\n",
				 configFile, WCETAnalysis.class.getName(), 
				 ProjectConfig.APP_CLASS_NAME.getKey(), "wcet.Method"));
		System.err.println("OPTIONS can be configured using system properties"+
		                   ", supplying a property file or as command line arguments");
		for(Option<?> o : Config.instance().availableOptions()) {
			System.err.println("    "+o.toString(15));
		}
		System.err.println("\nSee 'wcet.properties' for an example configuration");
		if(dumpConfig) System.err.println("Current configuration:\n"+Config.instance().dumpConfiguration(4));
		System.exit(1);		
	}
	public void logException(String ctx, Throwable e) {
		e.printStackTrace();
		tlLogger.error("Exception occured when "+ctx+": "+e);
	}
	public void bail(String msg) {
		printSep();
		System.err.println("[ERROR] "+msg);
		printSep();
		System.exit(1);
	}

	private void printSep() {
		System.err.println("---------------------------------------------------------------");
	}
	public static double timeDiff(long nanoStart, long nanoStop) {
		return (((double)nanoStop-nanoStart) / 1.0E9);		
	}
}
