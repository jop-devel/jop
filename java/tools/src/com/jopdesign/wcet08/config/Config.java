/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet08.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

/** Configuration for WCET Analysis
 *  
 *  The configuration is a singleton, which is configured via
 *  properties, command line options and/or setters
 */
public class Config {
	/*
	 * Singleton
	 * ~~~~~~~~~
	 */
	private static Config theConfig = null;
	public static Config instance() {
		if(theConfig == null) theConfig = new Config();
		return theConfig;
	}

	/*
	 * Exception classes
	 * ~~~~~~~~~~~~~~~~~
	 */
	public static class MissingConfigurationError extends Error {
		private static final long serialVersionUID = 1L;
		public MissingConfigurationError(String msg) { super(msg); }
	}
	public static class BadConfigurationError extends Error {
		private static final long serialVersionUID = 1L;
		public BadConfigurationError(String msg) { super(msg); }
	}
	public static class BadConfigurationException extends Exception {
		private static final long serialVersionUID = 1L;
		public BadConfigurationException(String message) {
			super(message);
		}
		public BadConfigurationException(String message, Exception e) {
			super(message,e);
		}
	}
	/*
	 * Options
	 * ~~~~~~~ 
	 */
	private Map<String,Option<? extends Object>> optionSet;
	private List<Option<? extends Object>> optionList;
	public List<Option<? extends Object>> availableOptions() {
		return optionList;
	}
	public Option<? extends Object> getOptionSpec(String key) {
		return optionSet.get(key);
	}
	public void addOptions(Option<?>[] options) {
		for(Option<? extends Object> opt : options) {
			optionSet.put(opt.key,opt);
			optionList.add(opt);
		}
	}
	public void addOptions(Option<?>[][] options) {
		for(Option<?>[] optArray : options) addOptions(optArray);
	}

	/* Options which are always present */
	public static final BooleanOption SHOW_HELP =
		new BooleanOption("help","show help",false);
	public static final BooleanOption SHOW_VERSION =
		new BooleanOption("version","get version number",false);
	public static final BooleanOption DEBUG =
		new BooleanOption("debug","verbose debugging mode",false);
	public static final Option<?> standardOptions[] = { SHOW_HELP, SHOW_VERSION, DEBUG };
    /**
	 * The underlying Properties object
	 */
	protected Properties props;
	
	public void setProperty(String key, String val) { 
		props.setProperty(key, val); 
	}
	Properties getProperties() {
		return this.props;
	}


	/**
	 * Logging configuration
	 */
	LoggerConfig loggerConfig;
	
	
	/**
	 * Create a new configuration, using @{link System.getProperties} 
	 */
	protected Config() { 
		theConfig = this; /* avoid potential recursive loop */
		props = new Properties();
		for(Entry<?,?> e : System.getProperties().entrySet()) {
			props.put(e.getKey(),e.getValue());
		}
		loggerConfig = new LoggerConfig(this);
		PropertyConfigurator.configure(this.props);
		optionList = new LinkedList<Option<? extends Object>>();
		optionSet  = new Hashtable<String, Option<? extends Object>>();
		addOptions(standardOptions);
	}		
	
	public <T> boolean hasOption(Option<T> option) {
		return (getOption(option) != null);
	}
	
	private <T> T tryGetOption(Option<T> option) {
		String val = this.props.getProperty(option.getKey());
		if(val == null) {
			if(option.getDefaultValue() != null) return option.getDefaultValue();
			return null;
		} else {
			return option.parse(val);
		}		
	}

	public <T> T getOptionWithDefault(Option<T> option, T def) {
		String val = this.props.getProperty(option.getKey());
		if(val == null) {
			return def;
		} else {
			return option.parse(val);
		}		
	}
	
	public <T> T getOption(Option<T> option) throws BadConfigurationError {
		T opt = tryGetOption(option);
		if(opt == null && ! option.isOptional()) throw new BadConfigurationError("Missing option: "+option.getKey());
		return opt;
	}
	
	public<T> void checkPresent(Option<T> option) throws BadConfigurationException {
		if(getOption(option) == null) {
			throw new BadConfigurationException("Missing option: "+option);
		}
	}
	
	public<T> void setOption(Option<T> option, T value) {
		this.props.setProperty(option.getKey(), value.toString());
	}

	/**
	 * Initialize the configuration.
	 * After this method has been executed, the configuration is available via 
	 * {@code instance()}
	 * @param configURL The URL to initialize the project from
	 * @throws BadConfigurationException 
	 * @throws IOException 
	 */
	public static void load(String configURL) throws BadConfigurationException {
		if(configURL != null) instance().loadConfig(configURL);
		PropertyConfigurator.configure(instance().props);
	}
	public static String[] load(String configURL, String[] argv) throws BadConfigurationException {
		Config c = instance();
		if(configURL != null) c.loadConfig(configURL);		
		String[] argvrest = c.consumeOptions(argv);
		PropertyConfigurator.configure(c.props);
		return argvrest;
	}
		
	/** Load a configuration file
	 * @throws BadConfigurationException if an error occurs while reading the configuration file
	 */
	public void loadConfig(String configURL) throws BadConfigurationException  {
		if(configURL == null) 
			throw new BadConfigurationException("No URL to configuration file supplied (configURL == null)");
		URL file;
		try {
			file = new URL(configURL);
	        InputStream fileStream = file.openStream();
			loadConfig(fileStream);
			fileStream.close();
		} catch (MalformedURLException e) {
			throw new BadConfigurationException("configFile: Malformed URL",e);
		} catch (IOException e) {
			throw new BadConfigurationException("IO Error while reading config file",e);
		}
	}
	/**
	 * load a configuration
	 * @param propStream an open InputStream serving the properties
	 * @throws IOException 
	 */
	public void loadConfig(InputStream propStream) throws IOException {
		Properties p = new Properties();
		p.load(propStream);
		props.putAll(p);
	}
	
	public void checkOptions() throws BadConfigurationException {
		for(Option<?> o : optionList) {
			try {
				this.getOption(o);
			} catch(BadConfigurationError missing) {
				throw new BadConfigurationException(missing.getMessage());
			} catch(IllegalArgumentException ex ){
				throw new BadConfigurationException("Bad format for option: "+o.key+"="+this.props.getProperty(o.key),ex);
			}
		}
	}
	public void initHtmlLoggers(File errorLog, File infoLog, Level consoleLevel) throws IOException {
		this.loggerConfig.setReportLoggers(errorLog, infoLog, consoleLevel);
	}
	public boolean helpRequested() {
		return getOption(SHOW_HELP);
	}
	public boolean versionRequested() {
		return getOption(SHOW_VERSION);
	}
	public Map<String,Object> getOptions() {
		Map<String,Object> opts = new HashMap<String, Object>();
		for(Option<?> o : optionList) {
			opts.put(o.key,getOption(o));
		}
		return opts;
	}
	
	/**
	 * Dump configuration for debugging purposes
	 * @return
	 */
	public String dumpConfiguration(int indent) {
		StringBuilder sb = new StringBuilder();
		for(Option<?> o : optionList) {
			Object val = tryGetOption(o);
			sb.append(String.format("%"+indent+"s%-20s ==> %s\n", "",o.getKey(),val == null ? "<not set>": val));
		}
		return sb.toString();
	}
	
	/**
	 * Consume all command line options and turn them into properties.<br/>
	 * 
	 * <p>The arguments are processed as follows: If an argument is of the form
	 * "-option" or "--option", it is considered to be an option.
	 * If an argument is an option, the next argument is considered to be the parameter,
	 * unless the option is boolean and the next argument is missing or an option as well.
	 * W add the pair to our properties, consuming both arguments.
	 * The first non-option or the argument string {@code --} terminates the option list.
	 * @param argv The argument list
	 * @param props The properties to update
	 * @return An array of unconsumed arguments
	 * @throws Exception 
	 */
	public String[] consumeOptions(String[] argv) throws BadConfigurationException {
		int i = 0;
		Vector<String> rest = new Vector<String>();
		while(i < argv.length && argv[i].startsWith("-") && 
			  ! (argv[i].equals("-") || argv[i].equals("--"))) {
			String key; 
			if(argv[i].charAt(1) == '-') key = argv[i].substring(2);
			else key = argv[i].substring(1);
			if(null != getOptionSpec(key)) {
				Option<? extends Object> spec = getOptionSpec(key);
				String val = null;
				if(i+1 < argv.length) {
					try {
						spec.checkFormat(argv[i+1]);
						val = argv[i+1];
					} catch(IllegalArgumentException ex) {
					}
				}
				if(spec instanceof BooleanOption && val == null) {
					val = "true";
				} else if(val == null){
					throw new BadConfigurationException("Missing argument for option: "+spec);
				} else {
					i++;
				}
				props.put(key, val);
			} else {
				throw new BadConfigurationException("Not in option set: "+key+" ("+optionSet.keySet().toString()+")");
			}
			i++;
		}
		for(;i < argv.length;i++) rest.add(argv[i]);
		String[] restArray = new String[rest.size()];
		return rest.toArray(restArray);
	}

	/** 
	 * Remove problematic characters from a method name 
     * Note that fully qualified methods might become non-unique,
     * so use an additional unique identifier if you need unique names. */
	public static String sanitizeFileName(String str) {
		StringBuffer sanitized = new StringBuffer(str.length());
		for(int i = 0; i < str.length(); i++) {
			if(Character.isLetterOrDigit(str.charAt(i)) || str.charAt(i) == '.') {
				sanitized.append(str.charAt(i));
			} else {
				sanitized.append('_');
			}
		}
		return sanitized.toString();
	}
	
	/** Check whether the given file is a directory, possibly creating it if
	 * non existing
	 * @param dir the path to the directory
	 * @param createIfNonExist whether the directory should be created, if it doesn't exist yet
	 * @throws IOException 
	 */
	public static void checkDir(File outDir, boolean createIfNonExist) throws IOException {
		if(outDir.exists()) {
			if(! outDir.isDirectory()) {
				throw new IOException("Not a directory: "+outDir);
			}
		} else if(createIfNonExist) {
			outDir.mkdirs();
		} else {
			throw new IOException("Directory does not exist: "+outDir);
		}		
	}
}
