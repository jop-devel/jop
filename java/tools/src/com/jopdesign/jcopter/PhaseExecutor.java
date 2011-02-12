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
import com.jopdesign.common.code.CallGraph;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationError;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.common.graphutils.InvokeDot;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.tools.ClinitOrder;
import com.jopdesign.common.tools.ConstantPoolRebuilder;
import com.jopdesign.common.tools.UsedCodeFinder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This is just a helper class to execute various optimizations and analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class PhaseExecutor {

    public enum DUMPTYPE { off, full, merged, both }

    public static final Logger logger = Logger.getLogger(JCopter.LOG_ROOT + ".PhaseExecutor");

    public static final BooleanOption REMOVE_UNUSED_MEMBERS =
            new BooleanOption("remove-unused-members", "Remove unreachable code", true);

    public static final EnumOption<DUMPTYPE> DUMP_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-callgraph", "Dump the app callgraph (with or without callstrings)", DUMPTYPE.merged);

    public static final EnumOption<DUMPTYPE> DUMP_JVM_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-jvm-callgraph", "Dump the jvm callgraph (with or without callstrings)", DUMPTYPE.off);

    public static final BooleanOption DUMP_NOIM_CALLS =
            new BooleanOption("dump-noim-calls", "Include calls to JVMHelp.noim() in the jvm callgraph dump", false);

    public static final StringOption CALLGRAPH_DIR =
            new StringOption("cgdir", "Directory to put the callgraph files into", "${outdir}/callgraph");


    public static final Option[] options = {
            REMOVE_UNUSED_MEMBERS,
            DUMP_CALLGRAPH, DUMP_JVM_CALLGRAPH, CALLGRAPH_DIR
            };

    private final JCopter jcopter;
    private final AppInfo appInfo;

    public PhaseExecutor(JCopter jcopter) {
        this.jcopter = jcopter;
        appInfo = AppInfo.getSingleton();
    }

    public Config getConfig() {
        return jcopter.getConfig().getConfig();
    }

    @SuppressWarnings({"AccessStaticViaInstance"})
    public void registerOptions(OptionGroup options) {
        options.addOptions(this.options);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Dump Callgraph
    /////////////////////////////////////////////////////////////////////////////////////

    public void dumpCallgraph(String graphName) {
        if (getConfig().getOption(DUMP_CALLGRAPH) == DUMPTYPE.off &&
            getConfig().getOption(DUMP_JVM_CALLGRAPH) == DUMPTYPE.off)
        {
            return;
        }

        try {
            File outDir = getConfig().getOutDir(CALLGRAPH_DIR);

            // Dumping the full graph is a bit much, we split it into several graphs
            Set<ExecutionContext> appRoots = new HashSet<ExecutionContext>();
            Set<ExecutionContext> jvmRoots = new HashSet<ExecutionContext>();
            Set<ExecutionContext> clinitRoots = new HashSet<ExecutionContext>();

            Set<String> jvmClasses = new HashSet<String>();
            if (appInfo.getProcessorModel() != null) {
                jvmClasses.addAll( appInfo.getProcessorModel().getJVMClasses() );
                jvmClasses.addAll( appInfo.getProcessorModel().getNativeClasses() );
            }

            for (ExecutionContext ctx : appInfo.getCallGraph().getRootNodes()) {
                if (ctx.getMethodInfo().getMemberSignature().equals(ClinitOrder.clinitSig)) {
                    clinitRoots.add(ctx);
                } else if (jvmClasses.contains(ctx.getMethodInfo().getClassName())) {
                    jvmRoots.add(ctx);
                } else {
                    appRoots.add(ctx);
                }
            }

            dumpCallgraph(outDir, graphName, "app", appRoots, getConfig().getOption(DUMP_CALLGRAPH), false);
            dumpCallgraph(outDir, graphName, "clinit", clinitRoots, getConfig().getOption(DUMP_CALLGRAPH), false);
            dumpCallgraph(outDir, graphName, "jvm", jvmRoots, getConfig().getOption(DUMP_JVM_CALLGRAPH),
                                                              !getConfig().getOption(DUMP_NOIM_CALLS));

        } catch (BadConfigurationException e) {
            throw new BadConfigurationError("Could not create output dir "+getConfig().getOption(CALLGRAPH_DIR), e);
        } catch (IOException e) {
            throw new AppInfoError("Unable to export to .dot file", e);
        }
    }

    private void dumpCallgraph(File outDir, String graphName, String suffix, Set<ExecutionContext> roots,
                               DUMPTYPE type, boolean skipNoim)
            throws IOException
    {
        if (roots.isEmpty()) return;

        CallGraph subGraph = appInfo.getCallGraph().getSubGraph(roots);
        
        if (type == DUMPTYPE.merged || type == DUMPTYPE.both) {
            dumpCallgraph(outDir, graphName, suffix, subGraph, true, skipNoim);
        }
        if (type == DUMPTYPE.full || type == DUMPTYPE.both) {
            dumpCallgraph(outDir, graphName, suffix, subGraph, false, skipNoim);
        }
        
        appInfo.getCallGraph().removeSubGraph(subGraph);
    }

    private void dumpCallgraph(File outDir, String graphName, String type, CallGraph graph, 
                               boolean merged, boolean skipNoim) throws IOException 
    {
        String suffix = (merged) ? type+"-merged" : type+"-full";
        
        File dotFile = new File(outDir, graphName+"-"+suffix+".dot");
        File pngFile = new File(outDir, graphName+"-"+suffix+".png");

        logger.info("Dumping "+suffix+" callgraph to "+dotFile);

        FileWriter writer = new FileWriter(dotFile);

        graph.exportDOT(writer, merged, false, skipNoim);

        InvokeDot.invokeDot(getConfig(), dotFile, pngFile);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Perform analyses
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Reduce the callgraph stored with AppInfo.
     * {@link AppInfo#buildCallGraph(boolean)} must have been called first.
     */
    public void reduceCallGraph() {
        // TODO perform callgraph thinning analysis
        // logger.info("Starting callgraph reduction");

        // logger.info("Finished callgraph reduction");
    }

    /**
     * Mark all InvokeSites which are safe to inline, or store info
     * about what needs to be done in order to inline them.
     * To get better results, reduce the callgraph first as much as possible.
     */
    public void markInlineCandidates() {
        // TODO call invoke candidate finder
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // Perform optimizations
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Inline all methods which do not increase the code size.
     * {@link #markInlineCandidates()} must have been run first.
     */
    public void performSimpleInline() {
    }

    /**
     * Inline all InvokeSites which are marked for inlining by an inline strategy.
     */
    public void performInline() {
    }

    /**
     * Run some simple optimizations to cleanup the bytecode without increasing its size.
     */
    public void cleanupMethodCode() {
        // TODO optimize load/store
        // TODO perform some simple peephole optimizations
        // (more complex optimizations (dead-code elimination, constant-folding,..) should
        //  go into another method..)
    }

    /**
     * Find and remove unused classes, methods and fields
     */
    public void removeUnusedMembers() {
        logger.info("Starting removal of unused members");

        UsedCodeFinder ucf = new UsedCodeFinder();
        ucf.resetMarks();
        ucf.markUsedMembers();
        ucf.removeUnusedMembers();

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
