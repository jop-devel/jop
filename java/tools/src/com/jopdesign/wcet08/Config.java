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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Option.BooleanOption;
import com.jopdesign.wcet08.Option.StringOption;

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
	public class MissingConfigurationError extends Error {
		private static final long serialVersionUID = 1L;
		public MissingConfigurationError(String msg) { super(msg); }
	}
	public class BadConfigurationError extends Error {
		private static final long serialVersionUID = 1L;
		public BadConfigurationError(String msg) { super(msg); }
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
	
	/*
	 * Base Option
	 * ~~~~~~~~~~~~
	 */
	/**
	 * FIXME: Split library logic and base options into 2 classes
	 * TODO: We could add more elaborated option type (for classpath, methods, etc.) to improve error handling
	 */
	public static final StringOption APP_CLASS_NAME = new Option.StringOption("app-class",
			"the name of the class containing the method to be analyzed",false);
	public static final StringOption TARGET_METHOD =
		new Option.StringOption("target-method",
								"the name (optionally fully qualified and/or with signature) of the method to be analyzed",
								"measure");
	public static final StringOption PROJECT_NAME =
		new Option.StringOption("projectname","name of the 'project', used when generating reports",true);

	public static final StringOption CLASSPATH_PROPERTY =  new Option.StringOption("cp","the classpath",false);
	public static final StringOption SOURCEPATH_PROPERTY = new Option.StringOption("sp","the sourcepath",false);
	
	public static final StringOption REPORTDIR_PROPERTY =
		new Option.StringOption("reportdir",
				"the directory to write reports into (no report generation if neither this nor reportdir-root is set)",true);		
	public static final StringOption REPORTDIRROOT_PROPERTY =
		new Option.StringOption("reportdir-parent",
				"reports will be generated in config[reportdir-root]/config["+PROJECT_NAME.key+"]",true);
	public static final StringOption TEMPLATEDIR_PROPERTY =
		new Option.StringOption("templatedir",
				"directory with custom templates for report generation",true);
	
	public static final StringOption PROGRAM_DOT = 
		new Option.StringOption("program-dot","if graphs should be generated from java, the path to the 'dot' binary", true);
		
	public static final BooleanOption DUMP_ILP =
		new Option.BooleanOption("dump-ilp","whether the LP problems should be dumped to files",true);
	
	public static final BooleanOption DO_DFA =
		new Option.BooleanOption("dataflow-analysis","whether dataflow analysis should be performed",false);
	public static final BooleanOption SHOW_HELP =
		new Option.BooleanOption("help","show help",false);
	

	public static final Option<?>[] baseOptions =
	{ 
		APP_CLASS_NAME, TARGET_METHOD, PROJECT_NAME,
		CLASSPATH_PROPERTY, SOURCEPATH_PROPERTY,
		REPORTDIR_PROPERTY, REPORTDIRROOT_PROPERTY,
		TEMPLATEDIR_PROPERTY, PROGRAM_DOT,
		DUMP_ILP, DO_DFA,
		SHOW_HELP
	};
    /**
	 * The underlying Properties object
	 */
	protected Properties props;
	public void setProperty(String key, String val) { props.setProperty(key, val); }
	
	/* The name of the project (set once) */
	private String projectName;

	/* The output directory (set once) */
	private File outDir;
	private ConsoleAppender defaultAppender;
	private boolean genWCETReport = true;
	
	
	/**
	 * Create a new configuration, using @{link System.getProperties} 
	 */
	protected Config() { 
		theConfig = this; /* avoid potential recursive loop */
		props = new Properties();
		for(Entry<?,?> e : System.getProperties().entrySet()) {
			props.put(e.getKey(),e.getValue());
		}
		defaultAppender = new ConsoleAppender(new PatternLayout("[%c{1}] %m\n"),"System.err");
		defaultAppender.setName("ACONSOLE");
		defaultAppender.setThreshold(Level.WARN);
		Logger.getRootLogger().addAppender(defaultAppender);
		PropertyConfigurator.configure(this.props);
		optionList = new LinkedList<Option<? extends Object>>();
		optionSet  = new Hashtable<String, Option<? extends Object>>();		
		addOptions(Config.baseOptions);
	}		
	public <T> boolean hasOption(Option<T> option) {
		return (getOption(option) != null);
	}
	public <T> T getOption(Option<T> option) throws BadConfigurationError {
		String val = this.props.getProperty(option.getKey());
		if(val == null) {
			if(option.getDefaultValue() != null) return option.getDefaultValue();
			if(! option.isOptional()) throw new BadConfigurationError("Missing option: "+option);
			return null;
		} else {
			return option.parse(val);
		}
	}
	public<T> void checkPresent(Option<T> option) throws BadConfigurationException {
		if(getOption(option) == null) {
			throw new BadConfigurationException("Missing option: "+option);
		}
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

	public String getProjectName() {
		if(this.projectName == null) { /* Set ONCE ! */
			this.projectName = getOption(PROJECT_NAME);
			if(projectName == null) this.projectName = getTargetName();
		}
		return projectName;
	}
	public void setProjectName(String name) {
		this.projectName = name;
	}
	public String getTargetName() {
		return sanitizeFileName(this.getAppClassName()+"_"+this.getMeasureTarget());		
	}

	public String getAppClassName() {
		String rootClass = getOption(APP_CLASS_NAME);
		if(rootClass.indexOf('/') > 0) {
			rootClass = rootClass.replace('/','.');			
			props.put(APP_CLASS_NAME.getKey(),rootClass);
		}
		return rootClass;
	}
	public String getMeasureTarget() {
		return getOption(TARGET_METHOD);
	}
	public String getMeasuredClass() {
		return splitFQMethod(getMeasureTarget(),true);
	}
	public String getMeasuredMethod() {
		return splitFQMethod(getMeasureTarget(),false);
	}
	private String splitFQMethod(String s, boolean getClass) {		
		int sigIx = s.indexOf('(');
		if(sigIx > 0) s = s.substring(0,sigIx);
		int methIx = s.lastIndexOf('.');
		if(getClass) {
			if(methIx > 0) {
				return s.substring(0,methIx);
			} else {
				return getAppClassName();
			}
		} else {
			if(methIx > 0) {
				return s.substring(methIx + 1);
			} else {
				return s;
			}
		}
	}
	
	/** Return the configured classpath, a list of colon-separated paths
	 * @return the classpath to look for files
	 */
	public String getClassPath() {
		return getOption(CLASSPATH_PROPERTY);
	}

	public String getTemplatePath() {
		return getOption(TEMPLATEDIR_PROPERTY);
	}

	/**
	 * check whether a directory for generating reports is set
	 * @return true, if there is a report directory
	 */
	public boolean hasReportDir() {
		return this.hasOption(REPORTDIR_PROPERTY) ||
			   this.hasOption(REPORTDIRROOT_PROPERTY);
	}
	/**
	 * @return the directory to create output files in
	 */
	public File getOutDir() {
		if(this.outDir == null) {
			if(getOption(REPORTDIR_PROPERTY) != null) {
				this.outDir = new File(getOption(REPORTDIR_PROPERTY));
			} else if(getOption(REPORTDIRROOT_PROPERTY) != null){
				File outdirRoot = new File(getOption(REPORTDIRROOT_PROPERTY));
				this.outDir = new File(outdirRoot,getProjectName());
			} else {
				throw new MissingConfigurationError("Requesting out directory, but no report directory set: "+
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
		return getOption(SOURCEPATH_PROPERTY);		
	}
	
	public File getSourceFile(MethodInfo method) throws FileNotFoundException {
		return getSourceFile(method.getCli());
	}
	
	public File getSourceFile(ClassInfo ci) throws FileNotFoundException {
		StringTokenizer st = new StringTokenizer(this.getSourcePath(),File.pathSeparator);
		while (st.hasMoreTokens()) {
			String sourcePath = st.nextToken();
			String pkgPath = ci.clazz.getPackageName().replace('.', File.separatorChar);
			sourcePath += File.separator + pkgPath;
			File sourceDir = new File(sourcePath);
			File sourceFile = new File(sourceDir, ci.clazz.getSourceFileName());
			if(sourceFile.exists()) return sourceFile;	
		}
		throw new FileNotFoundException("Source for "+ci.clazz.getClassName()+" not found.");
	}
	public boolean doDataflowAnalysis() {
		return getOption(DO_DFA);
	}
	public String getDotBinary() {
		return getOption(PROGRAM_DOT);
	}
	public boolean doInvokeDot() {
		return (getDotBinary() != null);
	}
	public boolean doGenerateWCETReport() {
		return this.genWCETReport ;
	}
	public boolean doDumpIPL() {
		return this.hasReportDir() && this.getOption(DUMP_ILP);
	}
	public void setGenerateWCETReport(boolean generateReport) {
		this.genWCETReport = generateReport;
	}
	public boolean hasDotBinary() {
		if(getDotBinary() == null) return false;
		return new File(getDotBinary()).exists();
	}
	public boolean helpRequested() {
		return getOption(SHOW_HELP);
	}
	public Map<String,Object> getOptions() {
		Map<String,Object> opts = new HashMap<String, Object>();
		for(Option<?> o : optionList) {
			opts.put(o.key,getOption(o));
		}
		return opts;
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
		Vector<String> rest = new Vector<String>();
		while(i+1 < argv.length && argv[i].startsWith("-") && 
			  ! (argv[i].equals("-") || argv[i].equals("--"))) {
			String key,val; 
			if(argv[i].charAt(1) == '-') key = argv[i].substring(2);
			else key = argv[i].substring(1);
			val = argv[i+1];
			if(null != getOptionSpec(key)) {
				props.put(key, val);
			} else {
				System.err.println("Not in option set: "+key+" ("+Arrays.toString(argv));
				rest.add(key);
				rest.add(val);
			}
			i+=2;
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
}
