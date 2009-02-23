package com.jopdesign.wcet.config;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

public class LoggerConfig {
	
	private Config config;
	private ConsoleAppender defaultAppender;
	public LoggerConfig(Config config) {
		this.config = config;
		defaultAppender = new ConsoleAppender(new PatternLayout("[%c{1}] %m\n"),"System.err");
		defaultAppender.setName("ACONSOLE");
		if(config.getOption(Config.DEBUG)) {
			defaultAppender.setThreshold(Level.INFO);
		} else {
			defaultAppender.setThreshold(Level.WARN);			
		}
		Logger.getRootLogger().addAppender(defaultAppender);
	}
	public void setReportLoggers(File errorLog, File infoLog, Level consoleLevel) 
		throws IOException {
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
			PropertyConfigurator.configure(config.getProperties());
	}
}
