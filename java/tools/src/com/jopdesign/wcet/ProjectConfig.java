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
package com.jopdesign.wcet;

import java.io.File;

import com.jopdesign.wcet.config.BooleanOption;
import com.jopdesign.wcet.config.Config;
import com.jopdesign.wcet.config.IntegerOption;
import com.jopdesign.wcet.config.Option;
import com.jopdesign.wcet.config.StringOption;
import com.jopdesign.wcet.graphutils.MiscUtils;

public class ProjectConfig {
	public static final StringOption PROJECT_NAME =
		new StringOption("projectname","name of the 'project', used when generating reports",true);

	public static final StringOption JAVA_PROCESSOR =
		new StringOption("java-processor","which java processor to use (jamuth, JOP, alloc)","JOP");

	public static final StringOption APP_CLASS_NAME =
		new StringOption("app-class",
			             "the name of the class containing the main entry point of the RTJava application",
			             false);

	public static final StringOption TARGET_METHOD =
		new StringOption("target-method",
						 "the name (optional: class,signature) of the method to be analyzed",
						 "measure");

	public static final StringOption TARGET_CLASSPATH =
		new StringOption("cp","the classpath",false);

	public static final StringOption TARGET_SOURCEPATH =
		new StringOption("sp","the sourcepath",false);

	public static final StringOption TARGET_BINPATH =
		new StringOption("linkinfo-path", "directory holding linker info (.link.txt)","java/target/dist/bin");

	public static final StringOption OUT_DIR =
		 new StringOption("outdir","directory for output of the analysis tool","java/target/wcet/");

	private static final BooleanOption DO_GENERATE_REPORTS =
		new BooleanOption("report-generation","whether reports should be generated",true);

	public static final BooleanOption DO_DFA =
		new BooleanOption("dataflow-analysis","whether dataflow analysis should be performed",false);

	/* Here, because it both affects the DFA and the IPET */
	public static final IntegerOption CALLSTRING_LENGTH =
		new IntegerOption("callstring-length", "Length of the callstring for DFA and IPET", 0);

	public static final BooleanOption USE_UPPAAL =
		new BooleanOption("uppaal","perform uppaal-based WCET analysis",false);

	public static final StringOption RESULT_FILE =
		new StringOption("result-file","save analysis results to the given file (CVS)",true);

	public static final BooleanOption RESULTS_APPEND =
		new BooleanOption("results-append","append analysis results to the result file",false);

	public static final Option<?>[] projectOptions =
	{
		OUT_DIR,
		APP_CLASS_NAME, TARGET_METHOD, PROJECT_NAME,
		TARGET_CLASSPATH, TARGET_SOURCEPATH, TARGET_BINPATH,
		JAVA_PROCESSOR, DO_DFA, CALLSTRING_LENGTH, USE_UPPAAL,
		RESULT_FILE, RESULTS_APPEND
	};


	public static File getOutDir(String subdir) {
		File dir = new File(
				new ProjectConfig(Config.instance()).getOutDir(),
				subdir);
		dir.mkdir();
		return dir;
	}

	/** If no instance of project is available, us this one to get a path for
	 * writing a file (not recommended, but sometimes useful for debugging stuff)
	 * @param subdir
	 * @param name
	 */
	public static File getOutFile(String subdir, String name) {
		return new File(getOutDir(subdir),MiscUtils.sanitizeFileName(name));
	}

	private Config config;

	public Config getConfigManager() {
		return this.config;
	}

	public ProjectConfig(Config config) {
		this.config = config;
	}

	/** Return the configured classpath, a list of path separated by {@see File.pathSeparatorChar}
	 * @return the classpath used for looking up compiled class files
	 */
	public String getClassPath() {
		return config.getOption(TARGET_CLASSPATH);
	}

	/**
	 * Get the name of the application class defining the entry point main()
	 * @return
	 */
	public String getAppClassName() {
		String appClass = config.getOption(APP_CLASS_NAME);
		if(appClass.indexOf('/') > 0) {
			appClass = appClass.replace('/','.');
		}
		return appClass;
	}

	/**
	 * Get the name of the application class, unqualified
	 * @see getAppClassName
	 */
	public String getUnqualifiedAppClassName() {
		String appClassName = getAppClassName();
		if(appClassName.indexOf('.') > 0) {
			appClassName = appClassName.substring(appClassName.lastIndexOf('.')+1);
		}
		return appClassName;
	}

	/**
	 * get the name of the method to be analyzed
	 * @return
	 */
	public String getTargetMethodName() {
		return config.getOption(ProjectConfig.TARGET_METHOD);
	}

	public String getTargetClass() {
		String measureClass = splitFQMethod(getTargetMethodName(),true);
		if(measureClass == null) return getAppClassName();
		else return measureClass;
	}

	public String getTargetMethod() {
		return splitFQMethod(getTargetMethodName(),false);
	}

	public String getProjectName() {
		return config.getOptionWithDefault(PROJECT_NAME,
				MiscUtils.sanitizeFileName(getAppClassName() + "_" + getTargetMethodName()));
	}

	public File getLinkInfoFile() {
		return new File(config.getOption(TARGET_BINPATH), getUnqualifiedAppClassName() + ".jop.link.txt");
	}

	public File getOutDir() {
		return new File(config.getOption(OUT_DIR),getProjectName());
	}

	/**
	 * A list of paths (seperated by pathSeparatorChar) used for looking up sources
	 * @return the path to source directories
	 */
	public String getSourcePath() {
		return config.getOption(TARGET_SOURCEPATH);
	}

	public String getProcessorName() {
		return config.getOption(JAVA_PROCESSOR);
	}
	/**
	 * Whether reports should be generated
	 * @return
	 */
	public boolean doGenerateReport() {
		return config.getOption(DO_GENERATE_REPORTS);
	}

	public File getReportDir() {
		return new File(getOutDir(),"report");
	}

	public boolean doDataflowAnalysis() {
		return config.getOption(DO_DFA);
	}

	public long callstringLength() {
		return config.getOption(CALLSTRING_LENGTH);
	}

	public boolean useUppaal() {
		return config.getOption(USE_UPPAAL);
	}

	public boolean saveResults() {
		return config.hasOption(RESULT_FILE);
	}

	public boolean appendResults() {
		return config.getOption(RESULTS_APPEND);
	}

	public static String splitFQMethod(String s, boolean getClass) {
		return splitClassName(s)[getClass ? 0 : 1];
	}

	public static String[] splitClassName(String s) {
		int sigIx = s.indexOf('(');
		String sWithoutSig;
		if(sigIx > 0) {
			sWithoutSig = s.substring(0,sigIx);
		} else {
			sWithoutSig = s;
		}
		int nameIx = sWithoutSig.lastIndexOf('.');
		String[] splittedName = new String[2];
		if(nameIx > 0) {
			splittedName[0] = s.substring(0,nameIx);
			splittedName[1] = s.substring(nameIx + 1);
		} else {
			splittedName[0] = null;
			splittedName[1] = s;
		}
		return splittedName;
	}
}
