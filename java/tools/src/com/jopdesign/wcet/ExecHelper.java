/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008-2010, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet;

import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Option;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.WcetSearch;
import lpsolve.LpSolve;
import lpsolve.VersionInfo;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Helper class for command line executables.
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class ExecHelper {
	/* Idea adopted from the Java Cookbook; warning: did not override all methods */
	public static class TeePrintStream extends PrintStream {

		private PrintStream p2;

		public TeePrintStream(PrintStream p1, PrintStream p2) {
			super(p1);
			this.p2 = p2;
		}

		@Override
		public void print(String s) {
			for(int i = 0; i < s.length(); i++) {
				super.write(s.charAt(i));				
				p2.write(s.charAt(i));
			}
		}
		@Override
		public void println(String s) {
			print(s+"\n");
		}
		
	}
	
	private Class<?> execClass;
	private String configFileProp;
	private Logger summaryLogger;
	private Config config;
	private String version;

	public ExecHelper(Class<?> clazz, String version, Logger topLevelLogger, String configFile) {
		this.execClass = clazz;
		this.version = version;
		this.summaryLogger = topLevelLogger;
		this.configFileProp = configFile;
	}

	/**
	 * Load configuration from config file and command line arguments
	 * @param args
	 */
	public void loadConfig(String[] args) {
		try {
			String configFile = System.getProperty(configFileProp);
			String[] argsrest = Config.load(configFile,args);
			config = Config.instance();
			if(config.helpRequested()) exitUsage(false);
			if(config.versionRequested()) exitVersion();
			if(argsrest.length != 0) {
				exitUsage("Unknown command line arguments: "+Arrays.toString(argsrest));
			}
			config.checkOptions();
		} catch(Config.BadConfigurationException e) {
			exitUsage(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			bail("Loading configuration failed");
		}
		config.initConsoleLoggers(summaryLogger); /* Initialize Loggers */
		summaryLogger.info("Configuration:\n"+config.dumpConfiguration(4));
		summaryLogger.info("java.library.path: "+System.getProperty("java.library.path"));		
	}

    public void checkLibs() {
        try {
            VersionInfo v = LpSolve.lpSolveVersion();
            info("Using lp_solve for Java, v"+
                    v.getMajorversion()+"."+v.getMinorversion()+
                    " build "+v.getBuild()+" release "+v.getRelease());
        } catch(UnsatisfiedLinkError ule) {
            bail("Failed to load the lp_solve Java library: "+ule);
        }
        if(config.getOption(ProjectConfig.USE_UPPAAL)) {
            String vbinary = config.getOption(UppAalConfig.UPPAAL_VERIFYTA_BINARY);
            try {
                String version = WcetSearch.getVerifytaVersion(vbinary);
                info("Using uppaal/verifyta: "+vbinary+" version "+version);
            } catch(Exception fne) {
                bail("Failed to run uppaal verifier: "+fne);
            }
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
		printUsage(true);
		printSep();
		System.err.println("[USAGE ERROR] "+reason);
		printSep();
		System.exit(1);
	}

	public void exitUsage(boolean dumpConfig) {
		printUsage(dumpConfig);
		System.exit(1);
	}

	public void printUsage(boolean dumpConfig) {
		if(dumpConfig) System.err.println("Current configuration:\n"+Config.instance().dumpConfiguration(4));
		System.err.println(
			MessageFormat.format("" +
					"Usage:\n  java -D{0}=file://<path-to-config> {1} [OPTIONS]",
					configFileProp, execClass.getCanonicalName()));
		System.err.println(
			MessageFormat.format(
				"Example:\n    java -D{0}=file:///home/jop/myconf.props {1} -{2} {3}\n",
				 configFileProp, WCETAnalysis.class.getName(),
				 ProjectConfig.APP_CLASS_NAME.getKey(), "wcet.Method"));
		System.err.println("OPTIONS can be configured using system properties"+
		                   ", supplying a property file or as command line arguments");
		for(Option<?> o : Config.instance().availableOptions()) {
			System.err.println("    "+o.toString(15));
		}
		System.err.println("\nSee 'wcet.properties' for an example configuration");
	}

	public static double timeDiff(long nanoStart, long nanoStop) {
		return (((double)nanoStop-nanoStart) / 1.0E9);
	}

	public Logger getExecLogger() {
		return summaryLogger;
	}

	public void info(String string) {
		summaryLogger.info(string);
	}

	public void logException(String ctx, Throwable e) {
		e.printStackTrace();
		summaryLogger.error("Exception occured when "+ctx+": "+e);
	}

	public void bail(String msg) {
		printSep();
		System.err.println("[ERROR] "+msg);
		printSep();
		System.exit(1);
	}

	public void bail(Exception e) {
		printSep();
		e.printStackTrace();
		bail(e.getMessage());
	}

	private void printSep() {
		System.err.println("---------------------------------------------------------------");
	}


}
