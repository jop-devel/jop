package com.jopdesign.wcet08;

import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jopdesign.wcet08.Config.BadConfigurationError;
import com.jopdesign.wcet08.Config.MissingConfigurationError;

public class ExecHelper {
	private Class<?> execClass;
	private String configFile;
	private Logger tlLogger;

	public ExecHelper(Class<?> clazz, Logger topLevelLogger, String configFile) {
		this.execClass = clazz;
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
			Config c = Config.instance();
			if(c.helpRequested()) exitUsage();
			if(argsrest.length != 0) {
				exitUsage("Unknown command line arguments: "+Arrays.toString(argsrest));
			}
			tlLogger.info("Configuration: "+Config.instance().getOptions());
			tlLogger.info("java.library.path: "+System.getProperty("java.library.path"));
			c.checkOptions();
			if(Config.instance().hasReportDir()) {
				tlLogger.info("Initializing Output");
				Config.instance().initializeReport();
			}
		} catch (MissingConfigurationError e) {
			exitUsage("Missing option: "+e.getMessage());
		} catch(BadConfigurationError e) { 
			exitUsage("Bad option: "+e.getMessage());					
		} catch (Exception e) {
			e.printStackTrace();
			bail("Loading configuration failed");
		}
	}
	public void exitUsage(String reason) {
		printSep();
		System.err.println("[USAGE ERROR] "+reason);
		printSep();
		exitUsage();
	}

	public void exitUsage() {
		System.err.println(
			MessageFormat.format("" +
					"Usage:\n  java -D{0}=file://<path-to-config> {1} [OPTIONS]", 
					configFile, execClass.getCanonicalName()));
		System.err.println(
			MessageFormat.format(
				"Example:\n    java -D{0}=file:///home/jop/myconf.props {1} -{2} {3}\n",
				 configFile, WCETAnalysis.class.getName(), 
				 Config.APP_CLASS_NAME, "wcet.Method"));
		System.err.println("OPTIONS can be configured using system properties"+
		                   ", supplying a property file or as command line arguments");
		for(Option<?> o : Config.instance().availableOptions()) {
			System.err.println("    "+o.toString(15));
		}
		System.err.println("\nSee 'wcet.properties' for an example configuration");
		System.err.println("Current configuration: "+Config.instance().getOptions());
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
}
