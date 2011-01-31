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
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.type.Signature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectConfig {
    public static final StringOption PROJECT_NAME =
            new StringOption("projectname","name of the 'project', used when generating reports",true);

    public static final StringOption WCET_MODEL =
            new StringOption("wcet-model","which java processor to use (jamuth, JOP, allocObjs, allocHandles, allocHeaders, allocBlocks)","${arch}");

    public static final StringOption TARGET_METHOD =
            new StringOption("target-method",
                                             "the name (optional: class,signature) of the method to be analyzed",
                                             "measure");

    public static final StringOption TARGET_LIB_SOURCEPATH =
            new StringOption("splib","sourcepath of the library code, helper option used in 'sp' option defaultvalue.", "java/target/src/jdk_base;java/target/src/common;java/target/src/jdk16");

    public static final StringOption TARGET_SOURCEPATH =
            new StringOption("sp","the sourcepath","${splib};java/target/src/app");

    public static final StringOption TARGET_BINPATH =
            new StringOption("linkinfo-path", "directory holding linker info (.link.txt)","java/target/dist/bin");

    private static final BooleanOption DO_GENERATE_REPORTS =
            new BooleanOption("report-generation","whether reports should be generated",true);

    public static final BooleanOption WCET_PREPROCESS =
            new BooleanOption("wcet-preprocess", "Perform bytecode preprocessing (same as running WCETPreprocess first)", false);

    public static final BooleanOption OBJECT_CACHE_ANALYSIS =
            new BooleanOption("object-cache-analysis","perform experimental object cache analysis",false);

    public static final BooleanOption USE_UPPAAL =
            new BooleanOption("uppaal","perform uppaal-based WCET analysis",false);

    public static final StringOption RESULT_FILE =
            new StringOption("result-file","save analysis results to the given file (CVS)",true);

    public static final BooleanOption RESULTS_APPEND =
            new BooleanOption("results-append","append analysis results to the result file",false);

    public static final Option<?>[] projectOptions =
    {
            TARGET_METHOD, PROJECT_NAME,
            TARGET_LIB_SOURCEPATH, TARGET_SOURCEPATH, TARGET_BINPATH,
            WCET_MODEL,
            WCET_PREPROCESS,
            OBJECT_CACHE_ANALYSIS,
            USE_UPPAAL,
            DO_GENERATE_REPORTS,
            RESULT_FILE, RESULTS_APPEND,
    };


    private Config config;
    private AppInfo appInfo;

    public ProjectConfig(Config config) {
        this.config = config;
        this.appInfo = AppInfo.getSingleton();
    }

    public Config getConfig() {
        return this.config;
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
        Signature sig = Signature.parse(getTargetMethodName(),true);
        String measureClass = sig.getClassName();
        if(measureClass == null) return getAppClassName();
        else return measureClass;
    }

    public String getTargetMethod() {
        Signature sig = Signature.parse(getTargetMethodName(),true);
        return sig.getMemberSignature();
    }

    public MethodInfo getTargetMethodInfo() {
        try {
            return appInfo.getMethodInfo(getTargetClass(), getTargetMethod());
        } catch (MethodNotFoundException e) {
            throw new BadConfigurationError("Cannot find target method " + getTargetMethodName(), e);
        }
    }

    public String getProjectName() {
        return config.getOption(PROJECT_NAME,
                        MiscUtils.sanitizeFileName(getAppClassName() + "_" + getTargetMethodName()));
    }

    public File getLinkInfoFile() {
        return new File(config.getOption(TARGET_BINPATH), getUnqualifiedAppClassName() + ".jop.link.txt");
    }

    public File getOutDir() {
        return new File(config.getOption(Config.WRITE_PATH),getProjectName());
    }

    /**
     * Create and return output directory {@code OUT_DIR / getProjectName() / subdir}
     * @param subdir subdirectory of project output directory, created if necessary
     * @return the path to the subdirectory for output
     */
    public File getOutDir(String subdir) {
        File dir = new File(getOutDir(), subdir);
        dir.mkdir();
        return dir;
    }

    public File getOutFile(String filename) {
        return new File(getOutDir(), MiscUtils.sanitizeFileName(filename));
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

    public int callstringLength() {
        return config.getOption(Config.CALLSTRING_LENGTH).intValue();
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

    public File getResultFile() {
        return new File(getConfig().getOption(ProjectConfig.RESULT_FILE));
    }
    
    public boolean isDebugMode() {
        return config.getOption(Config.DEBUG);
    }

    public boolean doPreprocess() {
        return config.getOption(WCET_PREPROCESS);
    }

}
