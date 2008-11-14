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

	public static final String PROGRAM_DOT = "program-dot";

	/* report triggers */
	public static final String DUMP_ILP = "dump-ilp";
	private final static boolean DUMP_ILP_DEFAULT = true;
	
	public static final String DO_DFA = "dataflow-analysis";
	public static final boolean DO_DFA_DEFAULT = false;

	/*
	 * Options
	 * ~~~~~~~ 
	 */
	private static Map<String,Option<? extends Object>> optionSet;
	private static List<Option<? extends Object>> optionList;
	private static void optionSetInit() {
		if(optionList != null) return;
		optionList = new LinkedList<Option<? extends Object>>();
		optionSet  = new Hashtable<String, Option<? extends Object>>();		
	}
	public static List<Option<? extends Object>> availableOptions() {
		optionSetInit();
		return optionList;
	}
	public static Option<? extends Object> getOptionSpec(String key) {
		optionSetInit();
		return optionSet.get(key);
	}
	public static void addOptions(Option[] options) {
		optionSetInit();
		for(Option<? extends Object> opt : options) {
			optionSet.put(opt.key,opt);
			optionList.add(opt);
		}
	}
	/**
	 * TODO: We could add more elaborated option type (for classpath, methods, etc.) to improve error handling
	 */
	public static final Option[] baseOptions =
	{ 
		new Option.StringOption(ROOT_CLASS_NAME,"the name of the class containing the method to be analyzed",false),
		new Option.StringOption(ROOT_METHOD_NAME,"the name (and optionally signature) of the method to be analyzed","measure"),
		new Option.StringOption(PROJECT_NAME,"name of the 'project', used when generating reports (generated if missing)",true),

		new Option.StringOption(CLASSPATH_PROPERTY,"the classpath",false),
		new Option.StringOption(SOURCEPATH_PROPERTY,"the sourcepath",false),
		
		new Option.StringOption(REPORTDIR_PROPERTY,"the directory to write reports into (no report generation if neither this nor "+REPORTDIRROOT_PROPERTY+" is set)",true),
		new Option.StringOption(REPORTDIRROOT_PROPERTY,"reports will be generated in config["+REPORTDIRROOT_PROPERTY+"]/config["+PROJECT_NAME+"]",true),
		new Option.StringOption(PROGRAM_DOT,"if graphs should be generated from java, the path to the 'dot' binary", true),
		
		new Option.BooleanOption(DUMP_ILP,"whether the LP problems should be dumped to files","yes"),
		
		new Option.BooleanOption(DO_DFA,"whether dataflow analysis should be performed","no")
	};
    /**
	 * The underlying Properties object
	 */
	protected Properties options;
	
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
		options = new Properties();
		for(Entry e : System.getProperties().entrySet()) {
			options.put(e.getKey(),e.getValue());
		}
		defaultAppender = new ConsoleAppender(new PatternLayout("[%c{1}] %m\n"),"System.err");
		defaultAppender.setName("ACONSOLE");
		defaultAppender.setThreshold(Level.WARN);
		Logger.getRootLogger().addAppender(defaultAppender);
		PropertyConfigurator.configure(this.options);
	}		
	protected boolean hasProperty(String key) {
		return this.options.containsKey(key);		
	}
	protected String getProperty(String key) {
		return this.options.getProperty(key);
	}
	protected String getProperty(String key, String defaultVal) {
		return getProperty(key) == null ? defaultVal : getProperty(key);
	}
	protected String forceProperty(String key) {
		String val = this.options.getProperty(key);
		if(val == null) throw new MissingConfigurationError("Missing property: " + key);
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
		PropertyConfigurator.configure(instance().options);
	}
	public static String[] load(String configURL, String[] argv) throws BadConfigurationException {
		Config c = instance();
		if(configURL != null) c.loadConfig(configURL);		
		String[] argvrest = c.consumeOptions(argv);
		PropertyConfigurator.configure(c.options);
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
		PropertyConfigurator.configure(this.options);
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
		options.putAll(p);
	}
	
	public void checkOptions() throws BadConfigurationException {
		for(Option o : Config.optionList) {
			if(! o.isOptional() && ! this.hasProperty(o.key)) {
				throw new BadConfigurationException("Missing Option: "+o.key);
			}
			String p = this.getProperty(o.key);
			if(p != null) {
				try {
					o.checkFormat(p);
				} catch(IllegalArgumentException ex ){
					throw new BadConfigurationException("Bad format for option: "+o.key+"="+p,ex);
				}
			}
		}
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
		return getProperty(ROOT_METHOD_NAME, DEFAULT_ROOT_METHOD);
	}

	public String getRootClassName() {
		String rootClass = forceProperty(ROOT_CLASS_NAME);
		if(rootClass.indexOf('/') > 0) return updRootClassName(rootClass);
		else return rootClass;
	}
	
	private String updRootClassName(String rc) {
		if(rc.indexOf('/') > 0) { // sanitize
			rc = rc.replace('/','.');
		}
		options.put(ROOT_CLASS_NAME,rc);
		return rc;
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
		return forceProperty(SOURCEPATH_PROPERTY);		
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
		return getBooleanOption(DO_DFA, DO_DFA_DEFAULT);
	}
	public String getDotBinary() {
		return getProperty(PROGRAM_DOT);
	}
	public boolean doInvokeDot() {
		return (getDotBinary() != null);
	}
	public boolean doGenerateWCETReport() {
		return this.genWCETReport ;
	}
	public boolean doDumpIPL() {
		return this.hasReportDir() && this.getBooleanOption(DUMP_ILP, DUMP_ILP_DEFAULT);
	}
	public void setGenerateWCETReport(boolean generateReport) {
		this.genWCETReport = generateReport;
	}
	public boolean hasDotBinary() {
		if(getDotBinary() == null) return false;
		return new File(getDotBinary()).exists();
	}
	public boolean helpRequested() {
		return getBooleanOption("help",false);
	}
	public Properties getOptions() {
		return this.options;
	}

	public boolean getBooleanOption(String key, boolean def) {
		String v= this.options.getProperty(key);
		if(v == null) return def;
		return Option.BooleanOption.parse(v);
	
	}
	public int getIntOption(String key, int def) {
		String v = this.options.getProperty(key);
		if(v== null) return def;
		return Integer.parseInt(v);
	}
		
	
	/**
	 * Set root class and method
	 * @param string
	 */
	public void setTarget(String fqmethodname) {
		int i = fqmethodname.lastIndexOf(".");
		if(i < 0) throw new AssertionError("setTarget("+fqmethodname+"): not a fully qualified method name");
		String clazz = fqmethodname.substring(0,i);
        String method = fqmethodname.substring(i+1,fqmethodname.length());
        this.options.put(Config.ROOT_CLASS_NAME, clazz);
        this.options.put(Config.ROOT_METHOD_NAME, method);
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
	 * @param options The properties to update
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
			if(optionSet.containsKey(key)) {
				options.put(key, val);
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
