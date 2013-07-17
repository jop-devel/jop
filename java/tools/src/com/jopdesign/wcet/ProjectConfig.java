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
package com.jopdesign.wcet;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.type.MemberID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectConfig {
    public static final StringOption PROJECT_NAME =
            new StringOption("projectname","name of the 'project', used when generating reports",true);

    /* not used, should we use it?
    public static final StringOption WCET_OUT =
            new StringOption("wcet-out", "output base path for wcet results", "${outdir}");
    */

    public static final StringOption WCET_MODEL =
            new StringOption("wcet-model","which java processor to use (jamuth, JOP, allocObjs, allocHandles, allocHeaders, allocBlocks)","${arch}");

    public static final StringOption TARGET_METHOD =
            new StringOption("target-method",
                                             "the name (optional: class,signature) of the method to be analyzed",
                                             "measure");

    public static final StringOption TARGET_LIB_SOURCEPATH =
            new StringOption("splib","sourcepath of the library code, only used in '--sp' value.",
                    Config.mergePaths(new String[]{
                            "java/target/src/common",
                            "java/target/src/jdk_base",
                            "java/target/src/jdk11",
                            "java/target/src/rtapi"
                    }));

    public static final StringOption TARGET_SOURCEPATH =
            new StringOption("sp","the sourcepath",
                    Config.mergePaths(new String[]{
                            "${splib}",
                            "java/target/src/app",
                            "java/target/src/bench",
                            "java/target/src/test"
                    }));

    public static final StringOption TARGET_BINPATH =
            new StringOption("linkinfo-path", "directory holding linker info (.link.txt)","java/target/dist/bin");

    public static final BooleanOption DO_GENERATE_REPORTS =
            new BooleanOption("report-generation","whether reports should be generated",true);

    public static final BooleanOption WCET_PREPROCESS =
            new BooleanOption("wcet-preprocess", "Perform bytecode preprocessing (same as running WCETPreprocess first)", false);

    public static final BooleanOption BLOCKING_TIME_ANALYSIS =
            new BooleanOption("blocking-time-analysis","perform experimental synchronized block analysis",true);

    public static final BooleanOption OBJECT_CACHE_ANALYSIS =
            new BooleanOption("object-cache-analysis","perform experimental object cache analysis",false);

    public static final BooleanOption USE_UPPAAL =
            new BooleanOption("uppaal","perform uppaal-based WCET analysis",false);

    public static final StringOption RESULT_FILE =
            new StringOption("result-file","save analysis results to the given file (CSV)",true);

    public static final BooleanOption RESULTS_APPEND =
            new BooleanOption("results-append","append analysis results to the result file",false);

    public static final BooleanOption RESULTS_PERFORMANCE =
            new BooleanOption("results-performance", "Include target-app unrelated results such as solver times in the CSV file", true);

    public static final EnumOption<DUMPTYPE> DUMP_TARGET_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-target-callgraph", "Dump the target method callgraph (with or without callstrings)", CallGraph.DUMPTYPE.off);

    public static final BooleanOption LOAD_LINKINFO =
            new BooleanOption("load-linkinfo", "Load the link file generated by JOPizer", true);

    public static final BooleanOption DFA_ANALYZE_BOOT =
            new BooleanOption("dfa-analyze-boot",
                    "Analyze boot method by the DFA. Enable this to reuse cached DFA results of the optimizer", false);

    private static final Option<?>[] standaloneOptions =
    {
            TARGET_METHOD,
            BLOCKING_TIME_ANALYSIS,
            WCET_PREPROCESS,
            OBJECT_CACHE_ANALYSIS,
            LOAD_LINKINFO,
            DFA_ANALYZE_BOOT
    };
    private static final Option<?>[] projectOptions =
    {
            TARGET_LIB_SOURCEPATH, TARGET_SOURCEPATH, TARGET_BINPATH,
            WCET_MODEL
    };
    private static final Option<?>[] reportOptions = {
            PROJECT_NAME,
            DO_GENERATE_REPORTS,
            RESULT_FILE, RESULTS_APPEND, RESULTS_PERFORMANCE
    };
    private static final Option<?>[] debugOptions = {
            DUMP_TARGET_CALLGRAPH,
    };

    private Config config;
    private AppInfo appInfo;

    public static void registerOptions(Config config, boolean standalone, boolean uppaal, boolean reports) {
        config.addOptions(standaloneOptions, standalone);
        config.addOption (USE_UPPAAL, uppaal);
        config.addOptions(projectOptions);
        config.addOptions(reportOptions, reports);
        config.getDebugGroup().addOptions(debugOptions, standalone);
    }

    public ProjectConfig(Config config) {
        this.config = config;
        this.appInfo = AppInfo.getSingleton();
    }

    public Config getConfig() {
        return this.config;
    }

    /**
     * This function initializes configuration defaults, like the project name.
     * Must be called before any option is accessed which may refer to options whose defaults are
     * initialized here.
     *
     * @param mainMethodID the main method signature
     */
    public void initConfig(MemberID mainMethodID) {
        String projectName = MiscUtils.sanitizeFileName(mainMethodID.getClassName() + "_" + getTargetMethodName());
        config.setDefaultValue(PROJECT_NAME, projectName);
    }

    /**
     * @return the name of the application class defining the entry point main()
     */
    public String getAppClassName() {
        return appInfo.getMainMethod().getClassName();
    }

    /**
     * @see #getAppClassName
     * @return the name of the application class, unqualified
     */
    public String getUnqualifiedAppClassName() {
        String appClassName = getAppClassName();
        if(appClassName.indexOf('.') > 0) {
            appClassName = appClassName.substring(appClassName.lastIndexOf('.')+1);
        }
        return appClassName;
    }

    /**
     * @return the name of the method to be analyzed
     */
    public String getTargetMethodName() {
        return config.getOption(ProjectConfig.TARGET_METHOD);
    }

    public String getTargetClass() {
        MemberID sig = MemberID.parse(getTargetMethodName(),true);
        String measureClass = sig.getClassName();

        if(measureClass == null) return getAppClassName();
        else return measureClass;
    }

    public String getTargetMethod() {

        MemberID sig = MemberID.parse(getTargetMethodName(),true);
        return sig.getMethodSignature();
    }

    public MethodInfo getTargetMethodInfo() {
        try {
            return appInfo.getMethodInfo(getTargetClass(), getTargetMethod());
        } catch (MethodNotFoundException e) {
            throw new BadConfigurationError("Cannot find target method " + getTargetMethodName(), e);
        }
    }

    public String getProjectName() {
        return config.getOption(PROJECT_NAME);
    }

    public File getLinkInfoFile() {
        return new File(config.getOption(TARGET_BINPATH), getUnqualifiedAppClassName() + ".jop.link.txt");
    }

    public File getProjectDir() {
        return getConfig().getOutDir();
    }

    /**
     * Create and return output directory {@code OUT_DIR / getProjectName() / subdir}
     * @param subdir subdirectory of project output directory, created if necessary
     * @return the path to the subdirectory for output
     */
    public File getOutDir(String subdir) {
        File dir = new File(getProjectDir(), subdir);
        if(!dir.exists()) dir.mkdir();
        return dir;
    }

    public File getOutFile(String name) {
        File dir = getProjectDir();
        if(!dir.exists()) dir.mkdir();
        return new File(dir, MiscUtils.sanitizeFileName(name));
    }

    public File getOutFile(String subdir, String name) {
        return new File(getOutDir(subdir),MiscUtils.sanitizeFileName(name));
    }

    /**
     * @return A list of paths used for looking up sources
     */
    public String[] getSourcePaths() {
        return Config.splitPaths(config.getOption(TARGET_SOURCEPATH));
    }

    public List<File> getSourceSearchDirs(ClassInfo ci) {
        String[] paths = getSourcePaths();
        List<File> dirs = new ArrayList<File>();
        String pkgPath = File.separator + ci.getPackageName().replace('.', File.separatorChar);

        for (String sourcePath : paths) {
            sourcePath += pkgPath;
            dirs.add(new File(sourcePath));
        }
        return dirs;
    }

    public String getProcessorName() {
        return config.getOption(WCET_MODEL);
    }
    /**
     * @return Whether reports should be generated
     */
    public boolean doGenerateReport() {
        return config.getOption(DO_GENERATE_REPORTS);
    }

    public boolean doObjectCacheAnalysis() {
        return this.config.getOption(OBJECT_CACHE_ANALYSIS);
    }

    public boolean useUppaal() {
        return config.getOption(USE_UPPAAL);
    }

    public boolean saveResults() {
        return config.hasValue(RESULT_FILE);
    }

    public boolean appendResults() {
        return config.getOption(RESULTS_APPEND);
    }

    public boolean addPerformanceResults() {
        return config.getOption(RESULTS_PERFORMANCE);
    }

    public File getResultFile() {
        return new File(getConfig().getOption(ProjectConfig.RESULT_FILE));
    }

    public boolean isDebugMode() {
        return config.getOption(Config.DEBUG);
    }

    public boolean doPreprocess() {
        return config.getOption(WCET_PREPROCESS);
    }

    public boolean doLoadLinkInfo() {
        return config.getOption(LOAD_LINKINFO);
    }

    public boolean doAnalyzeBootMethod() {
        return config.getOption(DFA_ANALYZE_BOOT);
    }

	/**
	 * @return whether to perform analysis of synchronized blocks
	 */
	public boolean doBlockingTimeAnalysis() {
		return config.getOption(BLOCKING_TIME_ANALYSIS);
	}
}
