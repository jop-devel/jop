package com.jopdesign.wcet08.report;

import static com.jopdesign.wcet08.config.Config.sanitizeFileName;

import java.io.File;

import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.config.BooleanOption;
import com.jopdesign.wcet08.config.Config;
import com.jopdesign.wcet08.config.Option;
import com.jopdesign.wcet08.config.StringOption;

public class ReportConfig {
	public static final StringOption ERROR_LOG_FILE =
		new StringOption("error-log","the error log file","error.log.html");
	public static final StringOption INFO_LOG_FILE =
		new StringOption("info-log","the info log file","info.log.html");
	public static final StringOption TEMPLATEDIR =
		new StringOption("templatedir",
				"directory with custom templates for report generation",true);	
	public static final StringOption PROGRAM_DOT = 
		new StringOption("program-dot","if graphs should be generated from java, the path to the 'dot' binary", true);
	public static final StringOption REPORTDIR =
		new StringOption("reportdir",
				"the directory to write reports into",true);		
	public static final BooleanOption DUMP_ILP =
		new BooleanOption("dump-ilp","whether the LP problems should be dumped to files",true);
	
		
	public static final Option<?> options[] = 
		{ REPORTDIR, TEMPLATEDIR, 
		  ERROR_LOG_FILE, INFO_LOG_FILE, PROGRAM_DOT, DUMP_ILP };	

	/* dynamic configuration */
	private Config config;
	private File outDir;
	public ReportConfig(File outDir, Config configData) {
		this.outDir = outDir;
		this.config = configData;
	}	
	/** get path for templates */
	public String getTemplatePath() {
		return config.getOption(TEMPLATEDIR);
	}
	/**
	 * get the directory to create output files in
	 */
	public File getOutDir() {
		return this.outDir;
	}
	public File getOutFile(File file) {
		return new File(outDir,file.getPath());
	}

	public File getOutFile(String filename) {
		return new File(outDir, filename);
	}

	/**
	 * get the filename for output files
	 * @param method the method the outputfile should be created for
	 * @param extension the filename extension (e.g. .xml)
	 * @return the filename
	 */
	public File getOutFile(MethodInfo method, String extension) {
		return new File(outDir,
				        sanitizeFileName(method.getFQMethodName()+extension));
	}
	public String getDotBinary() {
		return config.getOption(PROGRAM_DOT);
	}
	public boolean doInvokeDot() {
		return (getDotBinary() != null);
	}
	public boolean hasDotBinary() {
		if(getDotBinary() == null) return false;
		return new File(getDotBinary()).exists();
	}
	public File getErrorLogFile() {
		return new File(this.getOutDir(),config.getOption(ERROR_LOG_FILE));
	}

	public File getInfoLogFile() {
		return new File(this.getOutDir(),config.getOption(INFO_LOG_FILE));
	}

}
