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
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.BasicBlockNode;
import com.jopdesign.common.code.ControlFlowGraph.CFGEdge;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.misc.BadGraphError;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.MethodNotFoundException;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.analyses.LoopBounds;
import com.jopdesign.dfa.framework.ContextMap;
import com.jopdesign.dfa.framework.DFACallgraphBuilder;
import com.jopdesign.dfa.framework.FlowEdge;
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
import com.sun.xml.internal.bind.v2.TODO;
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
import java.util.Map;
import java.util.Set;

/**
 * Purpose: This class provides the interface to JOP's WCET tool
 *
 * @author Stefan Hepp (stefan@stefant.org)
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 */
public class WCETTool extends EmptyTool<WCETEventHandler> {

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

    private static final Option<?>[][] optionList = {
            ProjectConfig.projectOptions,
            IPETConfig.ipetOptions,
            UppAalConfig.uppaalOptions,
            ReportConfig.reportOptions
    };

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
    private Map<InstructionHandle, ContextMap<CallString, Set<String>>> receiverAnalysis = null;

    public WCETTool() {
        super(VERSION);
        eventHandler = new WCETEventHandler(this);
    }

    @Override
    public WCETEventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public void registerOptions(Config config) {
        config.addOptions(CallGraph.dumpOptions);
        // TODO maybe put some of the options into OptionGroups to make '--help' a bit clearer
        config.addOptions(WCETTool.optionList);
    }

    @Override
    public void onSetupConfig(AppSetup setup) throws BadConfigurationException {
        appInfo = setup.getAppInfo();
        Config config = setup.getConfig();

        projectConfig = new ProjectConfig(config);
        projectConfig.initConfig(setup.getMainSignature());

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

    public void initialize() throws BadConfigurationException {

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

        linkerInfo = new LinkerInfo(this);
        try {
            linkerInfo.loadLinkInfo();
        } catch (IOException e) {
            throw new BadConfigurationException("Could not load link infos", e);
        } catch (ClassNotFoundException e) {
            throw new BadConfigurationException("Could not load link infos", e);
        }

        /* run dataflow analysis */
        if (doDataflowAnalysis()) {
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
        DefaultCallgraphBuilder callGraphBuilder = new DefaultCallgraphBuilder(projectConfig.callstringLength());
        callGraphBuilder.setSkipNatives(true); // we do not want natives in the callgraph
        callGraph = CallGraph.buildCallGraph(getTargetMethod(), callGraphBuilder);

    	callGraph.checkAcyclicity();
        return callGraph;
    }

    public void dumpCallGraph(String graphName) {
    	
        if (callGraph == null) return;

        Config config = projectConfig.getConfig();

        try {
            callGraph.dumpCallgraph(config, graphName, "target", null,
                    config.getOption(ProjectConfig.DUMP_TARGET_CALLGRAPH), false);
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
        if (!mi.hasCode()) return null;
        ControlFlowGraph cfg;
        try {
            // We want the resolved invokes to be consistent with our callgraph so we use
            // that callgraph to resolve invokes (although if this.callgraph is not modified,
            // there is no difference to the appInfo callgraph). We also do not want to store
            // modifications of the graph back to the code.

            // TODO do we want to keep the graphs internally? however we would need a way to clear the cache
            //      when code gets changed, e.g. by removing all kept callgraphs manually or by using AppEventHandler.onMethodModified()

            cfg = new ControlFlowGraph(mi, CallString.EMPTY, callGraph);
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

    public LoopBound getLoopBound(CFGNode node, CallString cs) {

    	LoopBound globalBound = node.getLoopBound();
        ExecutionContext eCtx = new ExecutionContext(node.getControlFlowGraph().getMethodInfo(), cs);
    	// TODO move somewhere else?
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
        int nLocal = g.getGraph().vertexSet().size();
        int eLocal = g.getGraph().edgeSet().size();
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
        if (projectConfig.addPerformanceResults()) {
            recordCVS("wcet", "ipet", wcet, timeDiff, solverTime,
                    c.getOption(JOPConfig.CACHE_IMPL),
                    c.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    c.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(IPETConfig.STATIC_CACHE_APPROX),
                    c.getOption(IPETConfig.ASSUME_MISS_ONCE_ON_INVOKE));
        } else {
            recordCVS("wcet", "ipet", wcet,
                    c.getOption(JOPConfig.CACHE_IMPL),
                    c.getOption(JOPConfig.CACHE_SIZE_WORDS),
                    c.getOption(JOPConfig.CACHE_BLOCKS),
                    c.getOption(IPETConfig.STATIC_CACHE_APPROX),
                    c.getOption(IPETConfig.ASSUME_MISS_ONCE_ON_INVOKE));
        }

    }

    public void recordResultUppaal(WcetCost wcet,
                                   double timeDiff, double searchtime, double solvertimemax) {
        if (resultRecord == null) return;
        Config c = projectConfig.getConfig();
        if (projectConfig.addPerformanceResults()) {
            recordCVS("wcet", "uppaal", wcet, timeDiff, searchtime, solvertimemax,
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
        } else {
            recordCVS("wcet", "uppaal", wcet,
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

    @SuppressWarnings("unchecked")
    public void dataflowAnalysis() {
        int callstringLength = (int) projectConfig.callstringLength();

        // Moved DFA tool cache config to the DFA tool, but still ...
        // FIXME: At the moment, we do not have a nice directory structure respecting
        //        the fact that we perform many WCET analyses for one Application

        // TODO this is the same code as in JCopter PhaseExecutor
        dfaTool.load();
        
        topLevelLogger.info("Receiver analysis");
        this.receiverAnalysis = dfaTool.runReceiverAnalysis(callstringLength);

        topLevelLogger.info("Loop bound analysis");
        dfaTool.runLoopboundAnalysis(callstringLength);

        this.hasDfaResults = true;
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
                BasicBlockNode head = ControlFlowGraph.getHandleNode(e.getHead());
                BasicBlockNode tail = ControlFlowGraph.getHandleNode(e.getTail());
                CFGEdge edge = cfg.getGraph().getEdge(tail, head);
                if (edge != null) { // edge does not seem to exist any longer
                    retval.add(edge);
                }
            }
        }
        return retval;
    }

}
