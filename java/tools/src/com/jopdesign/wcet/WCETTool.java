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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.EmptyTool;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.BasicBlock;
import com.jopdesign.common.code.CFGCallgraphBuilder;
import com.jopdesign.common.code.CFGProvider;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InfeasibleEdgeProvider;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.misc.AppInfoException;
import com.jopdesign.common.misc.BadGraphError;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.framework.DFACallgraphBuilder;
import com.jopdesign.dfa.framework.FlowEdge;
import com.jopdesign.timing.TimingTable;
import com.jopdesign.timing.jop.JOPTimingTable;
import com.jopdesign.wcet.allocation.BlockAllocationModel;
import com.jopdesign.wcet.allocation.HandleAllocationModel;
import com.jopdesign.wcet.allocation.HeaderAllocationModel;
import com.jopdesign.wcet.allocation.ObjectAllocationModel;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.annotations.BadAnnotationException;
import com.jopdesign.wcet.annotations.SourceAnnotations;
import com.jopdesign.wcet.ipet.IPETConfig;
import com.jopdesign.wcet.jop.JOPWcetModel;
import com.jopdesign.wcet.jop.LinkerInfo;
import com.jopdesign.wcet.jop.LinkerInfo.LinkInfo;
import com.jopdesign.wcet.report.Report;
import com.jopdesign.wcet.report.ReportConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>Purpose: This class provides the interface to JOP's WCET tool</p>
 * <p>
 * <p>Note that it is currently not possible to create multiple instances of this tool, because the
 * annotation loader would be registered more than once in this case. To analyze multiple WCA targets,
 * the WCA tool must support this feature itself (e.g. by creating one callgraph per target internally).</p> 
 *
 * For a typical usage, you would
 * <ul>
 * <li/> Create a tool instance:
 *  <pre>WCETTool wcetTool = new WCETTool();</pre>
 * <li/> Register and Initialize the WCET tool:
 *  <pre>setup.registerTool("wcet", wcetTool);</pre>
 *  <pre>wcetTool.initialize(); </pre>
 * <li/> Create analysis instance:
 * <pre> RecursiveWcetAnalysis an = 
 *         new RecursiveWcetAnalysis(wcetTool,ipetConfig,strategy);</pre>
 * <li/> Run Analysis:
 * <pre> wcet = an.computeCost(wcetTool.getTargetMethod(), analysisContex); </pre>
 * </ul>
 * </p>
 * 
 * @see WCETAnalysis
 * @author Stefan Hepp (stefan@stefant.org)
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class WCETTool extends EmptyTool<WCETEventHandler> implements CFGProvider, InfeasibleEdgeProvider {

    public static final String VERSION = "1.0.1";

    // root logger for wcet tool
    public static final String LOG_WCET = "wcet";
    // logger for project related stuff (main, config, eventhandler,..)
    public static final String LOG_WCET_PROJECT = "wcet.project";
    // logger for various sub-packages
    public static final String LOG_WCET_ANALYSIS = "wcet.analysis";
    public static final String LOG_WCET_CACHE = "wcet.analysis.cache";
    public static final String LOG_WCET_ANNOTATIONS = "wcet.annotations";
    public static final String LOG_WCET_REPORT = "wcet.report";
    public static final String LOG_WCET_IPET = "wcet.ipet";
    public static final String LOG_WCET_UPPAAL = "wcet.uppaal";

    public static final Logger logger = Logger.getLogger(LOG_WCET_PROJECT + ".WCETTool");
    /* special logger */
    // TODO we could set this to something like 'console.wcet', and setup the console logger to
    //      print INFO for 'console.*' and WARN/ERROR only for the rest if '-q' and '-d' are not given 
    private Logger topLevelLogger = Logger.getLogger(LOG_WCET);

    private WCETEventHandler eventHandler;
    private ProjectConfig projectConfig;
    private String projectName;

    private AppInfo appInfo;
    private CallGraph callGraph;
    
    private DFATool dfaTool;

    private boolean genWCETReport;
    private Report results;
    private WCETProcessorModel processor;
    private File resultRecord;
    private LinkerInfo linkerInfo;
    private boolean hasDfaResults;

    private boolean standaloneOptions = true;
    private boolean ipetOptions = true;
    private boolean uppaalOptions = true;
    private boolean reportOptions = true;

    public WCETTool() {
        super(VERSION);
        eventHandler = new WCETEventHandler(this);
    }

    /**
     * Set which options should be exposed to the user.
     * @param standalone if true, add options to set target method and to enable report generation
     * @param ipet if true, add IPET options
     * @param uppaal if true, add UPPAAL options
     * @param reports if true, add report generation options
     */
    public void setAvailableOptions(boolean standalone, boolean ipet, boolean uppaal, boolean reports) {
        standaloneOptions = standalone;
        ipetOptions = ipet;
        uppaalOptions = uppaal;
        reportOptions = reports;
    }

    @Override
    public WCETEventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public void registerOptions(Config config) {
        CallGraph.registerOptions(config);
        // TODO maybe put some of the options into OptionGroups to make '--help' a bit clearer
        ProjectConfig.registerOptions(config, standaloneOptions, uppaalOptions, reportOptions);
        config.addOptions(IPETConfig.ipetOptions, ipetOptions);
        config.addOptions(UppAalConfig.uppaalOptions, uppaalOptions);
        config.addOptions(ReportConfig.reportOptions, reportOptions);
    }

    @Override
    public void onSetupConfig(AppSetup setup) throws BadConfigurationException {
        appInfo = setup.getAppInfo();
        Config config = setup.getConfig();

        projectConfig = new ProjectConfig(config);
        projectConfig.initConfig(setup.getMainMethodID());

        this.projectName = projectConfig.getProjectName();

        if (projectConfig.doGenerateReport()) {
            this.results = new Report(this, setup.getLoggerConfig());
            try {
                this.results.initVelocity();
            } catch (Exception e) {
                throw new BadConfigurationException("Error initializing Velocity: "+e, e);
            }
            this.genWCETReport = true;
        } else {
            this.genWCETReport = false;
        }
    }

    @SuppressWarnings({"LiteralAsArgToStringEquals"})
    @Override
    public void onSetupAppInfo(AppSetup setup, AppInfo appInfo) throws BadConfigurationException {

        // We only do setup stuff here, but do not perform any preprocessing (this is done in initialize())

        if (projectConfig.getProcessorName().equals("allocObjs")) {
            this.processor = new ObjectAllocationModel(this);
        } else if (projectConfig.getProcessorName().equals("allocHandles")) {
            this.processor = new HandleAllocationModel(this);
        } else if (projectConfig.getProcessorName().equals("allocHeaders")) {
            this.processor = new HeaderAllocationModel(this);
        } else if (projectConfig.getProcessorName().equals("allocBlocks")) {
            this.processor = new BlockAllocationModel(this);
        } else if (projectConfig.getProcessorName().equals("jamuth")) {
            this.processor = new JamuthWCETModel(this);
        } else if (projectConfig.getProcessorName().equals("JOP")) {
            try {
                this.processor = new JOPWcetModel(this);
            } catch (IOException e) {
                throw new BadConfigurationException("Unable to initialize JopWcetModel: " + e.getMessage(), e);
            }
        } else {
            throw new BadConfigurationException("Unknown WCET model: " + projectConfig.getProcessorName());
        }

        // create output dir only after initialization is successful
        File outDir = projectConfig.getProjectDir();
        Config.checkDir(outDir, true);
    }

    public void initialize(boolean loadLinkInfo, boolean initDFA) throws BadConfigurationException {

        if (projectConfig.saveResults()) {
            this.resultRecord = projectConfig.getResultFile();
            if (!projectConfig.appendResults()) {
                // TODO remove existing file if we do not append?
                //resultRecord.delete();
                recordMetric("problem", this.getProjectName());
                if (projectConfig.addPerformanceResults()) {
                    recordMetric("date", new Date());
                }
            }
        }

        if (loadLinkInfo) {
            linkerInfo = new LinkerInfo(this);
            try {
                linkerInfo.loadLinkInfo();
            } catch (IOException e) {
                throw new BadConfigurationException("Could not load link infos", e);
            } catch (ClassNotFoundException e) {
                throw new BadConfigurationException("Could not load link infos", e);
            }
        }

        /* run dataflow analysis */
        if (doDataflowAnalysis() && initDFA) {
            topLevelLogger.info("Starting DFA analysis");
            dataflowAnalysis();
            topLevelLogger.info("DFA analysis finished");
        }

        if (!appInfo.hasCallGraph()) {
            DefaultCallgraphBuilder callGraphBuilder;
            /* build callgraph for the whole program */
            if (doDataflowAnalysis()) {
                // build the callgraph using DFA results
                callGraphBuilder = new DFACallgraphBuilder(getDfaTool(), appInfo.getCallstringLength());
            } else {
                callGraphBuilder = new DefaultCallgraphBuilder();
            }
            callGraphBuilder.setSkipNatives(true); // we do not want natives in the callgraph
            appInfo.buildCallGraph(callGraphBuilder);
        }

        /* build callgraph for target method */
        rebuildCallGraph();

        if (projectConfig.doPreprocess()) {
            WCETPreprocess.preprocess(appInfo);
        }

        dumpCallGraph("callgraph");        
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public ProcessorModel getProcessorModel() {
        return appInfo.getProcessorModel();
    }

    public WCETProcessorModel getWCETProcessorModel() {
        return processor;
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public Config getConfig() {
        return projectConfig.getConfig();
    }
    
	public int getCallstringLength() {

		return appInfo.getCallstringLength();
	}


    /**
     * Rebuild the WCET callgraph, starting at the target method.
     * The new callgraph will be based on (but not backed by) the AppInfo callgraph, if available.
     *
     * @return the new callgraph.
     */
    public CallGraph rebuildCallGraph() {
    	
    	/* This would be the ideal solution, but this way the root
    	 * does NOT have an empty callstring
    	 */
        // callGraph = appInfo.getCallGraph().getSubGraph(projectConfig.getTargetMethodInfo());

        /* Instead, we create a new "subgraph" based on the appInfo callgraph (which has been created using
         * DFA results if available in initialize() or by some other tool), where the target method has an empty
         * callstring, using the callstring length configured for the WCET tool (which is currently the same
         * as the global setting).
         */
        DefaultCallgraphBuilder callGraphBuilder = new CFGCallgraphBuilder(getCallstringLength());
        callGraphBuilder.setSkipNatives(true); // we do not want natives in the callgraph
        callGraph = CallGraph.buildCallGraph(getTargetMethod(), callGraphBuilder);

        try {
            callGraph.checkAcyclicity();
        } catch (AppInfoException e) {
            throw new AssertionError(e);
        }
        return callGraph;
    }

    public void dumpCallGraph(String graphName) {
    	
        if (callGraph == null) return;

        Config config = projectConfig.getConfig();

        try {
            callGraph.dumpCallgraph(config, graphName, "target", null,
                    config.getDebugGroup().getOption(ProjectConfig.DUMP_TARGET_CALLGRAPH), false);
        } catch (IOException e) {
            logger.warn("Unable to dump the target method callgraph", e);
        }
    }
    
    /** @return the precomputed callgraph for WCET analysis */
    public CallGraph getCallGraph() {
        return callGraph;
    }

    public DFATool getDfaTool() {
        return dfaTool;
    }

    public void setTopLevelLogger(Logger topLevelLogger) {
        this.topLevelLogger = topLevelLogger;
    }

    public void setDfaTool(DFATool dfaTool) {
        this.dfaTool = dfaTool;
    }

    public ClassInfo getTargetClass() {
        return appInfo.getClassInfo(projectConfig.getTargetClass());
    }

    public String getTargetName() {
        return MiscUtils.sanitizeFileName(projectConfig.getAppClassName()
                + "_" + projectConfig.getTargetMethodName());
    }

    public MethodInfo getTargetMethod() {
        try {
            return appInfo.getMethodInfo(projectConfig.getTargetClass(),
                    projectConfig.getTargetMethod());
        } catch (MethodNotFoundException e) {
            throw new AssertionError("Target method not found: " + e);
        }
    }

    public String getProjectName() {
        return this.projectName;
    }

    public boolean reportGenerationActive() {
        return this.genWCETReport;
    }

    public void setGenerateWCETReport(boolean generateReport) {
        this.genWCETReport = generateReport;
    }

    public Report getReport() {
        return results;
    }

    /**
     * Get link info for a given class
     *
     * @param cli the class
     * @return the linker info
     */
    public LinkInfo getLinkInfo(ClassInfo cli) {
        return this.linkerInfo.getLinkInfo(cli);
    }

    public LinkerInfo getLinkerInfo() {
        return this.linkerInfo;
    }

    /**
     * Get the flowgraph of the given method.
     * <p>
     * A new callgraph is constructed when this method is called, changes to this graph are not
     * automatically stored back to MethodCode. If you want to keep changes to the graph you need to keep a
     * reference to this graph yourself.
     * </p>
     *
     * @param mi the method to get the CFG for
     * @return the CFG for the method.
     */
    public ControlFlowGraph getFlowGraph(MethodInfo mi) {
        if (!mi.hasCode()) {
        	throw new AssertionError("No CFG for MethodInfo "+mi);
        }
        ControlFlowGraph cfg;
        try {
            /* TODO We need to make sure that changes to the CFG are not compiled back automatically
             * but CFG#compile() is not yet fully implemented anyways.
             * We could add an option to MethodCode#getControlFlowGraph() to get a graph which will not be compiled back.
             *
             * We could also create a new graph instead, would allow us to create CFGs per callstring and be consistent
             * with this.callgraph. But as long as neither this.callgraph and appInfo.callgraph are not modified
             * after rebuildCallGraph() has been called, there is no difference.
             *
             * However, if we create a new graph we have to
             * - keep the new graph, if we just recreate them modifications by analyses will be lost and some things
             *   like SuperGraph will break if we return new graphs every time.
             * - provide a way to rebuild the CFGs if the code is modified, either by implementing WCETEventHandler.onMethodModified
             *   or by providing a dispose() method which must be called before the analysis starts after any code modifications.
             * - when we do not use a CFG anymore (e.g. because we recreate it) we need to call CFG.dispose() so that it
             *   is not attached to the instruction handles anymore.
             */
            //cfg = new ControlFlowGraph(mi, CallString.EMPTY, callGraph);

            cfg = mi.getCode().getControlFlowGraph(false);

            cfg.resolveVirtualInvokes();
            cfg.insertReturnNodes();
            cfg.insertContinueLoopNodes();
//    	    cfg.insertSplitNodes();
//    	    cfg.insertSummaryNodes();

        } catch (BadGraphException e) {
            // TODO handle this somehow??
            throw new BadGraphError(e.getMessage(), e);
        }
        
        return cfg;
    }

	// TODO move somewhere else?
    public LoopBound getLoopBound(CFGNode node, CallString cs) {

    	LoopBound globalBound = node.getLoopBound();
        ExecutionContext eCtx = new ExecutionContext(node.getControlFlowGraph().getMethodInfo(), cs);
        if (node.getBasicBlock() != null) {
            return this.getEventHandler().dfaLoopBound(node.getBasicBlock(), eCtx, globalBound);
        } else {
            return globalBound;
        }
    }

    /**
     * Convenience delegator to get the size of the given method
     * @param mi method to get the size of
     * @return size of the method in words, or 0 if abstract
     */
    public int getSizeInWords(MethodInfo mi) {
        if (!mi.hasCode()) return 0;
        return mi.getCode().getNumberOfWords();
    }


    public void writeReport() throws Exception {
        this.results.addStat("classpath", getAppInfo().getClassPath());
        this.results.addStat("application", projectConfig.getAppClassName());
        this.results.addStat("class", projectConfig.getTargetClass());
        this.results.addStat("method", projectConfig.getTargetMethod());
        this.results.writeReport();
    }

    public File getOutDir(String sub) {
    	return projectConfig.getOutDir(sub);
    }

//    public File getOutFile(String file) {
//        return new File(projectConfig.getOutDir(), file);
//    }

    /* FIXME: Slow, caching is missing */

    public int computeCyclomaticComplexity(MethodInfo m) {
        ControlFlowGraph g = getFlowGraph(m);
        int nLocal = g.vertexSet().size();
        int eLocal = g.edgeSet().size();
        int pLocal = g.buildLoopBoundMap().size();
        int ccLocal = eLocal - nLocal + 2 * pLocal;
        int ccGlobal = 0;
        for (ExecutionContext n : this.getCallGraph().getReferencedMethods(m)) {
            MethodInfo impl = n.getMethodInfo();
            ccGlobal += 2 + computeCyclomaticComplexity(impl);
        }
        return ccLocal + ccGlobal;
    }

    /* recording for scripted evaluation */

    public void recordResult(WcetCost wcet, double timeDiff, double solverTime) {
        if (resultRecord == null) return;
        Config c = projectConfig.getConfig();
        OptionGroup o = JOPConfig.getOptions(c);
        if (projectConfig.addPerformanceResults()) {
            recordCVS("wcet", "ipet", wcet, timeDiff, solverTime,
                    o.getOption(JOPConfig.CACHE_IMPL),
                    o.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    o.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(IPETConfig.STATIC_CACHE_APPROX),
                    c.getOption(IPETConfig.ASSUME_MISS_ONCE_ON_INVOKE));
        } else {
            recordCVS("wcet", "ipet", wcet,
                    o.getOption(JOPConfig.CACHE_IMPL),
                    o.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    o.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(IPETConfig.STATIC_CACHE_APPROX),
                    c.getOption(IPETConfig.ASSUME_MISS_ONCE_ON_INVOKE));
        }

    }

    public void recordResultUppaal(WcetCost wcet,
                                   double timeDiff, double searchtime, double solvertimemax) {
        if (resultRecord == null) return;
        Config c = projectConfig.getConfig();
        OptionGroup o = JOPConfig.getOptions(c);
        if (projectConfig.addPerformanceResults()) {
            recordCVS("wcet", "uppaal", wcet, timeDiff, searchtime, solvertimemax,
                    o.getOption(JOPConfig.CACHE_IMPL),
                    o.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    o.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(UppAalConfig.UPPAAL_CACHE_APPROX),
                    c.getOption(UppAalConfig.UPPAAL_COMPLEXITY_TRESHOLD),
                    c.getOption(UppAalConfig.UPPAAL_COLLAPSE_LEAVES),
                    c.getOption(UppAalConfig.UPPAAL_CONVEX_HULL),
                    c.getOption(UppAalConfig.UPPAAL_TIGHT_BOUNDS),
                    c.getOption(UppAalConfig.UPPAAL_PROGRESS_MEASURE),
                    c.getOption(UppAalConfig.UPPAAL_SUPERGRAPH_TEMPLATE),
                    c.getOption(UppAalConfig.UPPAAL_EMPTY_INITIAL_CACHE)
            );
        } else {
            recordCVS("wcet", "uppaal", wcet,
                    o.getOption(JOPConfig.CACHE_IMPL),
                    o.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    o.getOption(JOPConfig.CACHE_BLOCKS),
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
    }

    public void recordSpecialResult(String metric, WcetCost cost) {
        if (resultRecord == null) return;
        if (projectConfig.appendResults()) return;
        recordCVS("metric", metric, cost);
    }

    public void recordMetric(String metric, Object... params) {
        if (resultRecord == null) return;
        if (projectConfig.appendResults()) return;
        recordCVS("metric", metric, null, params);
    }

    private void recordCVS(String key, String subkey, WcetCost cost, Object... params) {
        Object[] fixedCols = {key, subkey};
        try {
            FileWriter fw = new FileWriter(resultRecord, true);
            fw.write(MiscUtils.joinStrings(fixedCols, ";"));
            if (cost != null) {
                Object[] costCols = {cost.getCost(), cost.getNonCacheCost(), cost.getCacheCost()};
                fw.write(";");
                fw.write(MiscUtils.joinStrings(costCols, ";"));
            }
            if (params.length > 0) {
                fw.write(";");
                fw.write(MiscUtils.joinStrings(params, ";"));
            }
            fw.write("\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getSourceFile(ClassInfo ci) throws FileNotFoundException {
        // TODO maybe move to AppInfo?
        List<File> dirs = projectConfig.getSourceSearchDirs(ci);
        for (File sourceDir : dirs) {
            File sourceFile = new File(sourceDir, ci.getSourceFileName());
            if (sourceFile.exists()) return sourceFile;
        }
        throw new FileNotFoundException("Source for " + ci.getClassName() + " not found in " + dirs);
    }

    /**
     * Get flow fact annotations for a class, lazily.
     *
     * @param cli
     * @return
     * @throws IOException
     * @throws BadAnnotationException
     */
    public SourceAnnotations getAnnotations(ClassInfo cli) throws IOException, BadAnnotationException {
        return eventHandler.getAnnotations(cli);
    }

    /* Data flow analysis
         * ------------------
         */

    public boolean doDataflowAnalysis() {
        return dfaTool != null;
    }

    public void dataflowAnalysis() {

    	// Moved DFA tool cache config to the DFA tool, but still ...
        // FIXME: At the moment, we do not have a nice directory structure respecting
        //        the fact that we perform many WCET analyses for one Application

        dfaTool.setAnalyzeBootMethod(projectConfig.doAnalyzeBootMethod());

        // TODO this is the same code as in JCopter PhaseExecutor
        dfaTool.load();
        
        topLevelLogger.info("Receiver analysis");
        dfaTool.runReceiverAnalysis(getCallstringLength());

        topLevelLogger.info("Loop bound analysis");
        dfaTool.runLoopboundAnalysis(getCallstringLength());

        this.hasDfaResults = true;
    }

    public void setHasDfaResults(boolean hasDfaResults) {
        this.hasDfaResults = hasDfaResults;
    }

    /**
     * @return the loop bounds found by dataflow analysis
     */
    public LoopBounds getDfaLoopBounds() {
        if (!hasDfaResults) return null;
        return dfaTool.getLoopBounds();
    }

    /**
     * Find Implementations of the method called with the given instruction handle
     * Uses receiver type analysis to refine the results
     *
     * @see #findImplementations(MethodInfo, InstructionHandle, CallString) 
     * @param invokerM invoking method
     * @param ih       the invoke instruction
     * @return list of possibly invoked methods
     */
    public Collection<MethodInfo> findImplementations(MethodInfo invokerM, InstructionHandle ih) {
        return findImplementations(invokerM, ih, CallString.EMPTY);
    }

    /**
     * Find Implementations of the method called with the given instruction handle
     * Uses receiver type analysis to refine the results
     *
     * @param invokerM invoking method
     * @param ih       the invoke instruction
     * @param ctx      the call context
     * @return list of possibly invoked methods
     */
    public Collection<MethodInfo> findImplementations(MethodInfo invokerM, InstructionHandle ih, CallString ctx) {
        InvokeSite is = invokerM.getCode().getInvokeSite(ih);
        // We do not use appInfo.findImplementations here so that the result is always consistent with the callgraph
        return callGraph.findImplementations(ctx.push(is));
    }

    /**
     * Get infeasible edges for certain call string
     *
     * @param cfg the controlflowgraph of the method
     * @param cs the callstring of the method
     * @return The infeasible edges
     */
    public List<CFGEdge> getInfeasibleEdges(ControlFlowGraph cfg, CallString cs) {
        List<CFGEdge> edges = new ArrayList<CFGEdge>();
        for (BasicBlock b : cfg.getBlocks()) {
            List<CFGEdge> edge = dfaInfeasibleEdge(cfg, b, cs);
            edges.addAll(edge);
        }
        return edges;
    }

	@Override
	public boolean isInfeasibleReceiver(MethodInfo method, CallString cs) {
		
		return ! this.getCallGraph().hasNode(method, cs);
	}

    /**
     * Get infeasible edges for certain basic block call string
     * @param cfg the CFG containing the block
     * @param block get infeasible outgoing edges for the block
     * @param cs the callstring
     * @return The infeasible edges for this basic block
     */
    private List<CFGEdge> dfaInfeasibleEdge(ControlFlowGraph cfg, BasicBlock block, CallString cs) {
        List<CFGEdge> retval = new LinkedList<CFGEdge>();
        if (getDfaLoopBounds() != null) {
            LoopBounds lbs = getDfaLoopBounds();
            Set<FlowEdge> edges = lbs.getInfeasibleEdges(block.getLastInstruction(), cs);
            for (FlowEdge e : edges) {
                BasicBlockNode head = cfg.getHandleNode(e.getHead());
                BasicBlockNode tail = cfg.getHandleNode(e.getTail());
                CFGEdge edge = cfg.getEdge(tail, head);
                if (edge != null) { 
                    retval.add(edge); 
                } else {
                	// edge does was removed from the CFG
                	// logger.warn("The infeasible edge between "+head+" and "+tail+" does not exist");                	
                }
            }
        }
        return retval;
    }


}
