/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.jcopter;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.code.DefaultCallgraphBuilder;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.graphutils.MethodTraverser;
import com.jopdesign.common.graphutils.MethodTraverser.MethodVisitor;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.tools.ClinitOrder;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.dfa.DFATool;
import com.jopdesign.dfa.framework.DFACallgraphBuilder;
import com.jopdesign.jcopter.greedy.GreedyConfig;
import com.jopdesign.jcopter.greedy.GreedyOptimizer;
import com.jopdesign.jcopter.inline.InlineConfig;
import com.jopdesign.jcopter.inline.InlineOptimizer;
import com.jopdesign.jcopter.inline.SimpleInliner;
import com.jopdesign.jcopter.optimizer.LoadStoreOptimizer;
import com.jopdesign.jcopter.optimizer.PeepholeOptimizer;
import com.jopdesign.jcopter.optimizer.RelinkInvokesuper;
import com.jopdesign.jcopter.optimizer.UnusedCodeRemover;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class executes various optimizations and analyses in the appropriate order, depending on the configuration.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class PhaseExecutor {

    public static final Logger logger = Logger.getLogger(JCopter.LOG_ROOT + ".PhaseExecutor");

    public static final BooleanOption REMOVE_UNUSED_MEMBERS =
            new BooleanOption("remove-unused-members", "Remove unreachable code", true);

    public static final EnumOption<DUMPTYPE> DUMP_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-callgraph", "Dump the app callgraph (with or without callstrings)", CallGraph.DUMPTYPE.merged);

    public static final EnumOption<DUMPTYPE> DUMP_JVM_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-jvm-callgraph", "Dump the jvm callgraph (with or without callstrings)", CallGraph.DUMPTYPE.off);

    public static final BooleanOption DUMP_NOIM_CALLS =
            new BooleanOption("dump-noim-calls", "Include calls to JVMHelp.noim() in the jvm callgraph dump", false);


    public static final Option[] phaseOptions = {
            DUMP_CALLGRAPH, DUMP_JVM_CALLGRAPH, DUMP_NOIM_CALLS, CallGraph.CALLGRAPH_DIR,
        };
    public static final Option[] optimizeOptions = {
            REMOVE_UNUSED_MEMBERS
        };

    public static final String GROUP_OPTIMIZE = "opt";
    public static final String GROUP_GREEDY = "greedy";
    public static final String GROUP_INLINE   = "inline";

    public static void registerOptions(OptionGroup options) {
        // Add phase options
        options.addOptions(phaseOptions);

        // Add options of all used optimizations
        OptionGroup opt = options.getGroup(GROUP_OPTIMIZE);
        opt.addOptions(optimizeOptions);
        opt.addOptions(UnusedCodeRemover.optionList);

        OptionGroup greedy = options.getGroup(GROUP_GREEDY);
        GreedyConfig.registerOptions(greedy);

        OptionGroup inline = options.getGroup(GROUP_INLINE);
        InlineConfig.registerOptions(inline);
    }

    private final JCopter jcopter;
    private final OptionGroup options;
    private final AppInfo appInfo;

    private GreedyConfig greedyConfig;
    private InlineConfig inlineConfig;

    public PhaseExecutor(JCopter jcopter, OptionGroup options) throws BadConfigurationException {
        this.jcopter = jcopter;
        this.options = options;
        appInfo = AppInfo.getSingleton();
        loadOptions();
    }

    private void loadOptions() throws BadConfigurationException {
        if (getJConfig().doOptimizeNormal()) {
            greedyConfig = new GreedyConfig(jcopter, getGreedyOptions());
        }
        inlineConfig = new InlineConfig(jcopter, getInlineOptions());
    }

    public Config getConfig() {
        return options.getConfig();
    }

    public JCopterConfig getJConfig() {
        return jcopter.getJConfig();
    }

    public OptionGroup getPhaseOptions() {
        return options;
    }

    public OptionGroup getOptimizeOptions() {
        return options.getGroup(GROUP_OPTIMIZE);
    }

    public OptionGroup getGreedyOptions() {
        return options.getGroup(GROUP_GREEDY);
    }

    public OptionGroup getInlineOptions() {
        return options.getGroup(GROUP_INLINE);
    }

    public boolean useCodeRemover() {
        return getOptimizeOptions().getOption(REMOVE_UNUSED_MEMBERS) && !getJConfig().doAssumeReflection();
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Dump Callgraph
    /////////////////////////////////////////////////////////////////////////////////////

    public void dumpCallgraph(String graphName) {
        if (getConfig().getOption(DUMP_CALLGRAPH) == CallGraph.DUMPTYPE.off &&
            getConfig().getOption(DUMP_JVM_CALLGRAPH) == CallGraph.DUMPTYPE.off)
        {
            return;
        }

        try {
            // Dumping the full graph is a bit much, we split it into several graphs
            Set<ExecutionContext> appRoots = new HashSet<ExecutionContext>();
            Set<ExecutionContext> jvmRoots = new HashSet<ExecutionContext>();
            Set<ExecutionContext> clinitRoots = new HashSet<ExecutionContext>();

            Set<String> jvmClasses = new HashSet<String>();
            if (appInfo.getProcessorModel() != null) {
                jvmClasses.addAll( appInfo.getProcessorModel().getJVMClasses() );
                jvmClasses.addAll( appInfo.getProcessorModel().getNativeClasses() );
            }

            CallGraph graph = appInfo.getCallGraph();

            for (ExecutionContext ctx : graph.getRootNodes()) {
                if (ctx.getMethodInfo().getMethodSignature().equals(ClinitOrder.clinitSig)) {
                    clinitRoots.add(ctx);
                } else if (jvmClasses.contains(ctx.getMethodInfo().getClassName())) {
                    jvmRoots.add(ctx);
                } else if (appInfo.isJVMThread(ctx.getMethodInfo().getClassInfo())) {
                    // This is to add Runnables like Scheduler and RtThread to the JVM classes.
                    jvmRoots.add(ctx);
                } else {
                    appRoots.add(ctx);
                }
            }

            Config config = getConfig();

            // TODO to keep the CG size down, we could add options to exclude methods (like '<init>') or packages
            // from dumping and skip dumping methods reachable only over excluded methods

            graph.dumpCallgraph(config, graphName, "app", appRoots, config.getOption(DUMP_CALLGRAPH), false);
            graph.dumpCallgraph(config, graphName, "clinit", clinitRoots, config.getOption(DUMP_CALLGRAPH), false);
            graph.dumpCallgraph(config, graphName, "jvm", jvmRoots, config.getOption(DUMP_JVM_CALLGRAPH),
                                                                   !config.getOption(DUMP_NOIM_CALLS));

        } catch (IOException e) {
            throw new AppInfoError("Unable to export to .dot file", e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Perform analyses
    /////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public void dataflowAnalysis(boolean loopBounds) {
        int callstringLength = appInfo.getCallstringLength();

        // TODO this code is the same as in WCETTool ..

        DFATool dfaTool = jcopter.getDfaTool();

        logger.info("Starting DFA analysis");
        dfaTool.setAnalyzeBootMethod(true);
        dfaTool.load();

        logger.info("Receiver analysis");
        dfaTool.runReceiverAnalysis(callstringLength);

        if (loopBounds) {
            logger.info("Loop bound analysis");
            dfaTool.runLoopboundAnalysis(callstringLength);
        }

        dfaTool.cleanup();
    }

    public void buildCallGraph(boolean useDFA) {

        if (useDFA) {
            // build the callgraph using DFA results
            DFACallgraphBuilder builder = new DFACallgraphBuilder(jcopter.getDfaTool(), appInfo.getCallstringLength());
            builder.setSkipNatives(true);
            appInfo.buildCallGraph(builder);
        } else {
            DefaultCallgraphBuilder builder = new DefaultCallgraphBuilder();
            builder.setSkipNatives(true);
            // rebuild without using the existing callgraph, because the callstrings are not updated by SimpleInliner
            builder.setUseCallgraph(false);
            appInfo.buildCallGraph(builder);
            // reduce the callgraph old-school
            reduceCallGraph();
        }
    }

    /**
     * Reduce the callgraph stored with AppInfo.
     * Called by {@link AppInfo#buildCallGraph(boolean)}.
     */
    public void reduceCallGraph() {
        // TODO perform callgraph thinning analysis
        // logger.info("Starting callgraph thinning");

        // logger.info("Finished callgraph thinning");
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // Perform optimizations
    /////////////////////////////////////////////////////////////////////////////////////

    public void relinkInvokesuper() {
        appInfo.iterate(new RelinkInvokesuper());
    }

    /**
     * Inline all methods which do not increase the code size.
     */
    public void performSimpleInline() {
        // We never increase codesize, we do not copy methods, we are quite fast.. so just do this always ..

        // .. well, almost always.
        if (getJConfig().doAssumeDynamicClassLoader()) {
            logger.info("Skipping simple-inliner since dynamic class loading is assumed.");
            return;
        }
        logger.info("Starting simple-inliner");

        new SimpleInliner(jcopter, inlineConfig).optimize();

        logger.info("Finished simple-inliner");
    }

    /**
     * Inline all InvokeSites which are marked for inlining by an inline strategy.
     */
    public void performGreedyOptimizer() {

        // this is a more elaborate optimization which may increase codesize.
        if (!getJConfig().doOptimizeNormal()) return;

        GreedyOptimizer optimizer = new GreedyOptimizer( greedyConfig );

        if (getJConfig().doAssumeDynamicClassLoader()) {
            logger.info("Skipping inliner since dynamic class loading is assumed.");
            // we do not have any other CodeOptimizers for now, remove return when we have some more
            return;
        } else {
            optimizer.addOptimizer( new InlineOptimizer(jcopter, inlineConfig ) );
        }
        logger.info("Starting greedy optimizer");

        optimizer.optimize();

        logger.info("Finished greedy optimizer");
    }

    /**
     * Run some simple optimizations to cleanup the bytecode without increasing its size.
     */
    public void cleanupMethodCode() {
        logger.info("Starting code cleanup");

        // perform some simple and safe peephole optimizations
        new PeepholeOptimizer(jcopter).optimize();
        
        // optimize load/store
        // TODO implement this ..
        new LoadStoreOptimizer(jcopter).optimize();

        // (more complex optimizations (dead-code elimination, constant-folding,..) should
        //  go into another method..)
        logger.info("Finished code cleanup");
    }

    public void removeDebugAttributes() {
        logger.info("Starting removal of debug attributes");

        MethodVisitor visitor = new MethodVisitor() {
            @Override
            public void visitMethod(MethodInfo method) {
                method.getCode().removeDebugAttributes();
            }
        };
        appInfo.iterate(new MethodTraverser(visitor, true));

        logger.info("Finished removal of debug attributes");
    }

    /**
     * Find and remove unused classes, methods and fields
     */
    public void removeUnusedMembers() {

        if (!getOptimizeOptions().getOption(REMOVE_UNUSED_MEMBERS)) {
            return;
        }
        // If reflection is used, we cannot remove unreferenced code since we might miss references by reflection
        if (getJConfig().doAssumeReflection()) {
            logger.info("Skipping removal of unused code because usage of reflection is assumed.");
            return;
        }

        logger.info("Starting removal of unused members");

        new UnusedCodeRemover(jcopter, getOptimizeOptions()).execute();

        logger.info("Finished removal of unused members");
    }

    /**
     * Rebuild all constant pools.
     */
    public void cleanupConstantPool() {
        logger.info("Starting cleanup of constant pools");

        appInfo.iterate(new ConstantPoolRebuilder());

        logger.info("Finished cleanup of constant pools");
    }

}
