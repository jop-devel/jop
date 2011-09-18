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
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.graphutils.InvokeDot;
import com.jopdesign.common.logger.LogConfig;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.wcet.WCETTool;

import java.io.File;

public class ReportConfig {
    public static final StringOption TEMPLATEDIR =
            new StringOption("templatedir",
                    "directory with custom templates for report generation", "java/tools/src");

    public static final StringOption WCET_REPORTDIR =
            new StringOption("wcet-reportdir", "directory to write wcet reports to", "${reportdir}");

    public static final Option<?>[] reportOptions =
            {TEMPLATEDIR, WCET_REPORTDIR};

    /* dynamic configuration */
    private Config config;
    private File reportDir;
    private final LogConfig logConfig;

    public ReportConfig(WCETTool project, LogConfig logConfig) throws BadConfigurationException {
        this.logConfig = logConfig;
        this.config = project.getConfig();
        this.reportDir = new File(config.getOption(WCET_REPORTDIR));
        Config.checkDir(reportDir, true);
    }

    /**
     * get path for templates
     * @return the template path or null if not set
     */
    public String getTemplatePath() {
        return config.getOption(TEMPLATEDIR);
    }

    /**
     * @return the directory to create output files in
     */
    public File getReportDir() {
        return this.reportDir;
    }

    public File getReportFile(File file) {
        return new File(reportDir, file.getPath());
    }

    public File getReportFile(String filename) {
        return new File(reportDir, filename);
    }

    /**
     * get the filename for output files
     *
     * @param method    the method the outputfile should be created for
     * @param extension the filename extension (e.g. .xml)
     * @return the filename
     */
    public File getOutFile(MethodInfo method, String extension) {
        return new File(reportDir,
                MiscUtils.sanitizeFileName(method.getFQMethodName() + extension));
    }

    public boolean doInvokeDot() {
        return InvokeDot.doInvokeDot(config);
    }

    public File getErrorLogFile() {
        return MiscUtils.getRelativeFile(logConfig.getErrorLogFile(), getReportDir());
    }

    public File getInfoLogFile() {
        return MiscUtils.getRelativeFile(logConfig.getInfoLogFile(), getReportDir());
    }

}
