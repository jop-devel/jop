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
package com.jopdesign.wcet08;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;

/*    
*  Sections:
*  * Input/Output: 
*           classpath, the sourcepath, root directory for output
*  * Problem analysis:
*           Dump the call graph, CFGs
*  * Optimizations (per analysis):
*           Fold blocks, unfold loops
*  * Analysis specific options (aggregated)
*           IPET
*           UPPAAL
*           Cache Analysis
*/

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
	public class MissingPropertyError extends Error {
		private static final long serialVersionUID = 1L;
		public MissingPropertyError(String msg) { super(msg); }
	}
	public class BadConfigurationException extends Exception {
		private static final long serialVersionUID = 1L;
		public BadConfigurationException(String message) {
			super(message);
		}
		public BadConfigurationException(String message, Exception e) {
			super(message,e);
		}
	}
	/*
	 * Constants
	 * ~~~~~~~~~
	 */
	public static final String PROJECT_NAME = "projectname";
	public static final String ROOT_CLASS_NAME = "rootclass";

	public static final String ROOT_METHOD_NAME = "rootmethod";
	private static final String DEFAULT_ROOT_METHOD = "measure";
	
	public static final String CLASSPATH_PROPERTY = "cp";	
	public static final String SOURCEPATH_PROPERTY = "sp";

	public static final String REPORTDIR_PROPERTY = "reportdir";
	public static final String REPORTDIRROOT_PROPERTY = "reportdir-parent";
	
	public static final String TEMPLATEDIR_PROPERTY = "templatedir";

	private static final String PROGRAM_DOT = "program-dot";
    private static final String DEFAULT_DOT_PROGRAM = "/usr/bin/dot";

    /*
	public static final String DO_UPPAAL = "do-uppaal";
	public static final String DO_IPET   = "do-ipet";
    */

    public static final String[][] optionDescrs = {
    	{CLASSPATH_PROPERTY,"the classpath [mandatory]"},
    	{SOURCEPATH_PROPERTY,"the sourcepath [mandatory]"},
    	{ROOT_CLASS_NAME,"the name of the class containing the method to be analyzed [mandatory]"},
    	{ROOT_METHOD_NAME,"the name (and optionally signature) of the method to be analyzed [default: measure]"},    	
    	{PROGRAM_DOT,"the path to the dot binary [default: /usr/bin/dot]"},
    	{PROJECT_NAME," the name of the project [default: fully qualified name of root method]"},
    	{REPORTDIR_PROPERTY,"if reports should be generated, the directory to write them to [optional]"},
    	{REPORTDIRROOT_PROPERTY,"if reports should be generated, the parent directory to write them to [optional]"},
    	{TEMPLATEDIR_PROPERTY,"directory with additional velocity templates [optional]"}    		    	
    };

    /**
	 * The underlying Properties object
	 */
	protected Properties props;
	
	/* The name of the project (set once) */
	private String projectName;

	/* The output directory (set once) */
	private File outDir;
	private ConsoleAppender defaultAppender;
	
	
	/**
	 * Create a new configuration, using @{link System.getProperties} 
	 */
	protected Config() { 
		theConfig = this; /* avoid potential recursive loop */
		props = new Properties(System.getProperties());
		defaultAppender = new ConsoleAppender(new PatternLayout("[%c{1}] %m\n"),"System.err");
		defaultAppender.setName("ACONSOLE");
		defaultAppender.setThreshold(Level.ERROR);
		Logger.getRootLogger().addAppender(defaultAppender);
		PropertyConfigurator.configure(this.props);
	}		
	protected boolean hasProperty(String key) {
		return this.props.containsKey(key);		
	}
	protected String getProperty(String key) {
		return this.props.getProperty(key);
	}
	protected String getProperty(String key, String defaultVal) {
		return getProperty(key) == null ? defaultVal : getProperty(key);
	}
	protected String forceProperty(String key) {
		String val = this.props.getProperty(key);
		if(val == null) throw new MissingPropertyError("Missing property: " + key);
		return val;
	}
	public void checkPresent(String prop) {
		forceProperty(prop);
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
	
	public void initializeReport() throws IOException {
		initReport();
		addReportLoggers();
	}

	/* initialize report */
	private void initReport() throws IOException {
		File outDir = getOutDir();
		if(outDir.exists()) {
			if(! outDir.isDirectory()) {
				throw new IOException("The output directory "+outDir+" exists, but isn't a directory");
			}
		} else {
			outDir.mkdir();
		}
	}	
	private void addReportLoggers() throws IOException {
		File elog = getErrorLogFile();
		elog.delete();
		FileAppender eapp = new FileAppender(new HTMLLayout(), elog.getPath());
		eapp.setName("AERROR");
		eapp.setThreshold(Level.ERROR);
		File ilog = getInfoLogFile();
		ilog.delete();
		FileAppender iapp = new FileAppender(new HTMLLayout(), ilog.getPath());
		iapp.setThreshold(Level.ALL);
		iapp.setName("AINFO");
		Logger.getRootLogger().addAppender(eapp);
		Logger.getRootLogger().addAppender(iapp);
		PropertyConfigurator.configure(this.props);
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
		props.load(propStream);
	}
	
	public String getProjectName() {
		if(this.projectName == null) { /* Set ONCE ! */
			if(getProperty(PROJECT_NAME) != null) {
				this.projectName = getProperty(PROJECT_NAME); 
			} else {
				this.projectName = sanitizeFileName(this.getRootClassName()+"_"+this.getRootMethodName());
			}
		}
		return projectName;
	}
	public void setProjectName(String name) {
		this.projectName = name;
	}

	public String getRootMethodName() {
		if(this.props.get(ROOT_METHOD_NAME) != null) {
			return this.props.getProperty(ROOT_METHOD_NAME);
		} else {
			return DEFAULT_ROOT_METHOD;
		}
	}

	public String getRootMethodSig() {
		return getRootMethodName();
	}

	public String getRootClassName() {
		return forceProperty(ROOT_CLASS_NAME);
	}
	
	/** Return the configured classpath, a list of colon-separated paths
	 * @return the classpath to look for files
	 */
	public String getClassPath() {
		return forceProperty(CLASSPATH_PROPERTY);
	}

	public String getTemplatePath() {
		return getProperty(TEMPLATEDIR_PROPERTY);
	}

	/**
	 * check whether a directory for generating reports is set
	 * @return true, if there is a report directory
	 */
	public boolean hasReportDir() {
		return this.hasProperty(REPORTDIR_PROPERTY) ||
			   this.hasProperty(REPORTDIRROOT_PROPERTY);
	}
	/**
	 * @return the directory to create output files in
	 */
	public File getOutDir() {
		if(this.outDir == null) {
			if(getProperty(REPORTDIR_PROPERTY) != null) {
				this.outDir = new File(getProperty(REPORTDIR_PROPERTY));
			} else if(getProperty(REPORTDIRROOT_PROPERTY) != null){
				File outdirRoot = new File(getProperty(REPORTDIRROOT_PROPERTY));
				this.outDir = new File(outdirRoot,getProjectName());
			} else {
				throw new MissingPropertyError("Requesting out directory, but no report directory set: "+
											   "neither '" + REPORTDIR_PROPERTY + "' nor '"+
											   REPORTDIRROOT_PROPERTY + "' present");	
			}
		}
		return this.outDir;
	}
	
	public File getErrorLogFile() {
		return new File(this.getOutDir(),"error.log.html");
	}

	public File getInfoLogFile() {
		return new File(this.getOutDir(),"info.log.html");
	}

	public File getOutFile(File file) {
		return new File(this.getOutDir(),file.getPath());
	}

	public File getOutFile(String filename) {
		return new File(this.getOutDir(), filename);
	}

	/**
	 * get the filename for output files
	 * @param method the method the outputfile should be created for
	 * @param extension the filename extension (e.g. .xml)
	 * @return the filename
	 */
	public File getOutFile(MethodInfo method, String extension) {
		return new File(this.getOutDir(),
				        sanitizeFileName(method.getFQMethodName()+extension));
	}

	/**
	 * @return the path to source directories
	 */
	public String getSourcePath() {
		return forceProperty(SOURCEPATH_PROPERTY);		
	}
	
	public File getSourceFile(MethodInfo method) throws FileNotFoundException {
		return getSourceFile(method.getCli());
	}
	
	public File getSourceFile(ClassInfo ci) throws FileNotFoundException {
		for(String sourcePath : this.getSourcePath().split(":")) {
			File sourceDir = new File(sourcePath);
			for(String pkgDir : ci.clazz.getPackageName().split("\\.")) {
				sourceDir = new File(sourceDir,pkgDir);			
			}
			File sourceFile = new File(sourceDir, ci.clazz.getSourceFileName());
			if(sourceFile.exists()) return sourceFile;	
		}
		throw new FileNotFoundException("Source for "+ci.clazz.getClassName()+" not found.");
	}

	public String getDotBinary() {
		return getProperty(PROGRAM_DOT,DEFAULT_DOT_PROGRAM);
	}
	public boolean hasDotBinary() {
		return new File(getDotBinary()).exists();
	}

	public boolean getBoolProperty(String key, boolean def) {
		String v= this.props.getProperty(key);
		if(v == null) return def;
		return (v.toLowerCase().startsWith("t") ||
		        v.toLowerCase().startsWith("y"));
	
	}
	public int getIntProperty(String key, int def) {
		String v= this.props.getProperty(key);
		if(v== null) return def;
		return Integer.parseInt(v);
	}
		
	
	/**
	 * Set root class and method
	 * @param string
	 */
	public void setTarget(String fqmethodname) {
		int i = fqmethodname.lastIndexOf(".");
		String clazz = fqmethodname.substring(0,i);
        String method = fqmethodname.substring(i+1,fqmethodname.length());
        this.props.put(Config.ROOT_CLASS_NAME, clazz);
        this.props.put(Config.ROOT_METHOD_NAME, method);
    }
	
	/**
	 * Consume all command line options and turn them into properties.<br/>
	 * 
	 * <p>The arguments are processed as follows: If an argument is of the form
	 * "-option" or "--option", it is considered to be an option.
	 * If an argument is an option, the next argument is considered to be the parameter,
	 * and we add the pair to our properties, consuming both arguments.
	 * The first non-option or the argument string {@code --} terminates the option list.
	 * @param argv The argument list
	 * @param props The properties to update
	 * @return An array of unconsumed arguments
	 */
	public String[] consumeOptions(String[] argv) {
		int i = 0;
		while(i+1 < argv.length && argv[i].startsWith("-") && 
			  ! (argv[i].equals("-") || argv[i].equals("--"))) {
			String key,val; 
			if(argv[i].charAt(1) == '-') key = argv[i].substring(2);
			else key = argv[i].substring(1);
			val = argv[i+1];
			props.put(key, val);
			i+=2;
		}
		String [] rest = new String[argv.length - i];
		for(int j = 0; j < (argv.length-i); j++) rest[j] = argv[i+j];
		return rest;
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
}
