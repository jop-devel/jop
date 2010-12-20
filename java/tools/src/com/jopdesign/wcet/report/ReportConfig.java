/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.wcet.report;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.misc.MiscUtils;

import java.io.File;

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
		
	public static final Option<?> reportOptions[] =
		{ REPORTDIR, TEMPLATEDIR, 
		  ERROR_LOG_FILE, INFO_LOG_FILE, PROGRAM_DOT };	

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
				        MiscUtils.sanitizeFileName(method.getFQMethodName() + extension));
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
