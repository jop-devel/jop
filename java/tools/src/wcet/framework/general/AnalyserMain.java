package wcet.framework.general;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import wcet.framework.exceptions.InitException;
import wcet.framework.exceptions.TaskException;
import wcet.framework.interfaces.general.IAnalyser;
import wcet.framework.interfaces.general.IDataStoreKeys;
import wcet.framework.util.ClassLoaderSingleton;

/**
 * Defines some usefull functions needed in most analyser executions.
 * 
 * @author Elena Axamitova
 * @version 0.3
 */
public final class AnalyserMain{

	/**
	 * the analyser to run
	 */
	private IAnalyser analyser = null;

	/**
	 * list of application's entry points
	 */
	private ArrayList<String> classes = new ArrayList<String>();

	/**
	 * the name of the file where the application should be witten
	 */
	private String output = null;

	/**
	 * the full path to jars/directories of the analyser, when not already in
	 * the java classpath
	 */
	private String librarypath = null;

	/**
	 * the full path to jars/directories containing class files of the
	 * application
	 */
	private String classpath = null;

	/**
	 * the full path to jars/directories containing source files of the
	 * application
	 */
	private String sourcepath = null;

	/**
	 * the name of the main method of the application
	 */
	private String mainMethodName = null;

	/**
	 * the description of the main method of the application
	 */
	private String mainMethodDescriptor = null;

	/**
	 * the path to the property file containing arguments and settings for the 
	 * analyser. If an argument is specified both in the property file and in the 
	 * command line, command line value is used.
	 */
	private String propertyFile = null;
	/**
	 * arguments specified either in command line or in the property file
	 */
	private Properties arguments = null;

	private String analyserClass;
	/**
	 * Process the command line arguments
	 * 
	 * @param args -
	 *            the command line arguments
	 */
	private void parseArguments(String[] args) {

		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-lp")) {
				this.librarypath = args[++i];
			} else if (args[i].equals("-cp")) {
			    this.classpath = args[++i];
			} else if (args[i].equals("-sp")) {
			    this.sourcepath = args[++i];
			} else if (args[i].equals("-o")) {
			    this.output = args[++i];
			} else if (args[i].equals("-mn")) {
			    this.mainMethodName = args[++i];
			} else if (args[i].equals("-md")) {
			    this.mainMethodDescriptor = args[++i];
			} else if (args[i].equals("-pf")) {
			    this.propertyFile = args[++i];
			} else {
			    this.classes.add(args[i]);
			}
		}
	}

	/**
	 * Start the analyser for all given application's entry points (the class
	 * command line arguments).
	 */
	private void processTasks() {
		try {
		    this.init(this.arguments);
			Iterator<String> iterator = this.classes.iterator();
			while (iterator.hasNext()) {
			    this.analyser.setTask(iterator.next());
			    this.analyser.analyse();
			}
		} catch (InitException e) {
			System.err.println("Problems occurred during init of "
					+ this.analyser.getClass().getCanonicalName() + ":");
			e.printStackTrace();
		} catch (TaskException e) {
			System.err.println("Problems occurred during execution of "
					+ this.analyser.getClass().getCanonicalName() + ":");
			e.printStackTrace();
		}
	}

	private void getAllArguments(){
		this.arguments = new Properties ();
		if (this.propertyFile != null) {
			try {
			    this.arguments.loadFromXML(new FileInputStream(propertyFile));
			} catch (Exception e) {
				System.err.println("Bad property file specified:");
				e.printStackTrace();
			}
		}
		if (this.classpath != null) {
		    this.arguments.put(IDataStoreKeys.CLASSPATH_KEY, this.classpath);
		}
		if (this.sourcepath != null) {
		    this.arguments.put(IDataStoreKeys.SOURCEPATH_KEY, this.sourcepath);
		}
		if (this.output != null) {
		    this.arguments.put(IDataStoreKeys.OUTPUT_KEY, this.output);
		}
		if (this.mainMethodName != null) {
		    this.arguments.put(IDataStoreKeys.MAINMETHOD_NAME_KEY, this.mainMethodName);
		}
		if (this.mainMethodDescriptor!= null) {
		    this.arguments.put(IDataStoreKeys.MAINMETHOD_DESCRIPTOR_KEY, this.mainMethodDescriptor);
		} 
		if (this.librarypath != null) {
		    this.arguments.put(IDataStoreKeys.LIBRARYPATH_KEY, librarypath);
		}
	}

	/**
	 * @param args -
	 *            Arguments passed to the analyser. Call without any arguments
	 *            to get the usage.
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err
			.println("Usage: <analyser class> [-cp classpath] [-sp sourcepath] [-o <output file>] [-mn <main method name>] [-md <main method descriptor>] [-pf <property file path>] class [class]*");
		} else {
			AnalyserMain mainObject = new AnalyserMain();
			mainObject.parseArguments(args);
			mainObject.getAllArguments();
			mainObject.analyserClass= args[0];
			mainObject.processTasks();
		}
	}

   
    private void init(Properties arguments) throws InitException {
	try {
	    if(this.librarypath!=null) 
		this.loadJars();
	    Class clazz = ClassLoaderSingleton.getInstance().loadClass(
		    this.analyserClass);
	    this.analyser = (IAnalyser) clazz.newInstance();
	    this.analyser.init(arguments);
	} catch (Exception e) {
	    throw new InitException(e);
	}

    }

    private void loadJars() throws InitException {
	StringTokenizer strtok = new StringTokenizer(this.librarypath, File.pathSeparator);
	while(strtok.hasMoreTokens()){
	    ClassLoaderSingleton.addLibrary(strtok.nextToken());
	}
    }
}
