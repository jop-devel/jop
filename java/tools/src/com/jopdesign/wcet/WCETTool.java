/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
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

import com.jopdesign.common.AppEventHandler;
import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyTool;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.annotations.SourceAnnotationReader;
import com.jopdesign.wcet.annotations.SourceAnnotations;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.jop.JOPConfig;
import com.jopdesign.wcet.jop.LinkerInfo;
import com.jopdesign.wcet.jop.LinkerInfo.LinkInfo;
import com.jopdesign.wcet.report.Report;
import com.jopdesign.wcet.uppaal.UppAalConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class WCETTool extends EmptyTool<AppEventHandler> {

    private ProjectConfig projectConfig;

    private String projectName;

    private AppInfo appInfo;
    private CallGraph callGraph;

    private Map<ClassInfo, SourceAnnotations> annotationMap;

    private boolean genWCETReport;
    private Report results;
    private WCETProcessorModel processor;
    private SourceAnnotationReader sourceAnnotations;
    private File resultRecord;
    private LinkerInfo linkerInfo;
    private boolean hasDfaResults;

    public WCETTool() {
    }

    @Override
    public String getToolVersion() {
        return "0.1";
    }

    @Override
    public void registerOptions(OptionGroup options) {

    }

    @Override
    public void onSetupConfig(AppSetup setup) throws BadConfigurationException {
        appInfo = setup.getAppInfo();

    }

    @Override
    public void initialize(Config config) {

    }

    @Override
    public void run(Config config) {

    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public WCETProcessorModel getProcessorModel() {
        return processor;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public Config getConfig() {
        return projectConfig.getConfig();
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public ClassInfo getTargetClass() {
        return appInfo.getClassInfo(projectConfig.getTargetClass());
    }

    public String getProjectName() {
        return this.projectName;
    }

    public boolean reportGenerationActive() {
        return this.genWCETReport ;
    }

    public void setGenerateWCETReport(boolean generateReport) {
        this.genWCETReport = generateReport;
    }

    public Report getReport() {
        return results;
    }

    public boolean doWriteReport() {
        return projectConfig.getReportDir() != null;
    }

    /**
     * Get link info for a given class
     * @param cli
     * @return the linker info
     */
    public LinkInfo getLinkInfo(ClassInfo cli) {
        return this.linkerInfo.getLinkInfo(cli);
    }

    public LinkerInfo getLinkerInfo() {
        return this.linkerInfo;
    }

    /**
     * Convenience delegator to get the flowgraph of the given method
     *
     * @param mi the method to get the CFG for
     * @return the CFG for the method.
     */
    public ControlFlowGraph getFlowGraph(MethodInfo mi) {
        return mi.isAbstract() ? null : mi.getCode().getControlFlowGraph();
    }

    /**
     * Convenience delegator to get the size of the given method
     */
    public int getSizeInWords(MethodInfo mi) {
        return this.getFlowGraph(mi).getNumberOfWords();
    }


    public void writeReport() throws Exception {
        this.results.addStat( "classpath", projectConfig.getClassPath());
        this.results.addStat( "application", projectConfig.getAppClassName());
        this.results.addStat( "class", projectConfig.getTargetClass());
        this.results.addStat( "method", projectConfig.getTargetMethod());
        this.results.writeReport();
    }

    public File getOutDir(String sub) {
        File outDir = projectConfig.getOutDir();
        File subDir = new File(outDir,sub);
        if(! subDir.exists()) subDir.mkdir();
        return subDir;
    }
    public File getOutFile(String file) {
        return new File(projectConfig.getOutDir(),file);
    }

    /* FIXME: Slow, caching is missing */
    public int computeCyclomaticComplexity(MethodInfo m) {
        ControlFlowGraph g = getFlowGraph(m);
        int nLocal = g.getGraph().vertexSet().size();
        int eLocal = g.getGraph().edgeSet().size();
        int pLocal = g.getLoopBounds().size();
        int ccLocal = eLocal - nLocal + 2 * pLocal;
        int ccGlobal = 0;
        for(ExecutionContext n: this.getCallGraph().getReferencedMethods(m)) {
            MethodInfo impl = n.getMethodInfo();
            ccGlobal += 2 + computeCyclomaticComplexity(impl);
        }
        return ccLocal + ccGlobal;
    }

    /* recording for scripted evaluation */
    public void recordResult(WcetCost wcet, double timeDiff, double solverTime) {
        if(resultRecord == null) return;
        Config c = projectConfig.getConfig();
        recordCVS("wcet","ipet",wcet,timeDiff,solverTime,
                    c.getOption(JOPConfig.CACHE_IMPL),
                    c.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    c.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(IPETConfig.STATIC_CACHE_APPROX),
                    c.getOption(IPETConfig.ASSUME_MISS_ONCE_ON_INVOKE));

    }

    public void recordResultUppaal(WcetCost wcet,
                                   double timeDiff, double searchtime,double solvertimemax) {
        if(resultRecord == null) return;
        Config c = projectConfig.getConfig();
        recordCVS("wcet","uppaal",wcet,timeDiff,searchtime,solvertimemax,
                c.getOption(JOPConfig.CACHE_IMPL),
                c.getOption(JOPConfig.CACHE_SIZE_WORDS),
                c.getOption(JOPConfig.CACHE_BLOCKS),
                c.getOption(UppAalConfig.UPPAAL_CACHE_APPROX),
                c.getOption(UppAalConfig.UPPAAL_COMPLEXITY_TRESHOLD),
                c.getOption(UppAalConfig.UPPAAL_COLLAPSE_LEAVES),
                c.getOption(UppAalConfig.UPPAAL_CONVEX_HULL),
                c.getOption(UppAalConfig.UPPAAL_TIGHT_BOUNDS),
                c.getOption(UppAalConfig.UPPAAL_PROGRESS_MEASURE),
                c.getOption(UppAalConfig.UPPAAL_SUPERGRAPH_TEMPLATE),
                c.getOption(UppAalConfig.UPPAAL_EMPTY_INITIAL_CACHE)
                );
    }

    public void recordSpecialResult(String metric, WcetCost cost) {
        if(resultRecord == null) return;
        if(projectConfig.appendResults()) return;
        recordCVS("metric",metric,cost);
    }

    public void recordMetric(String metric, Object... params) {
        if(resultRecord == null) return;
        if(projectConfig.appendResults()) return;
        recordCVS("metric",metric,null,params);
    }

    private void recordCVS(String key, String subkey, WcetCost cost, Object... params) {
        Object fixedCols[] = { key, subkey };
        try {
            FileWriter fw = new FileWriter(resultRecord,true);
            fw.write(MiscUtils.joinStrings(fixedCols, ";"));
            if(cost != null) {
                Object costCols[] = { cost.getCost(), cost.getNonCacheCost(),cost.getCacheCost() };
                fw.write(";");
                fw.write(MiscUtils.joinStrings(costCols, ";"));
            }
            if(params.length > 0) {
                fw.write(";");
                fw.write(MiscUtils.joinStrings(params, ";"));
            }
            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
