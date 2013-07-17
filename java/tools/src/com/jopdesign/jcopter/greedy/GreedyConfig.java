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

package com.jopdesign.jcopter.greedy;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph.DUMPTYPE;
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.IntegerOption;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.JCopterConfig;
import com.jopdesign.jcopter.analysis.MethodCacheAnalysis.AnalysisType;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class GreedyConfig {

    public enum GreedyOrder { Global, Targets, WCAFirst, TopDown, BottomUp }

    private static final EnumOption<GreedyOrder> GREEDY_ORDER =
            new EnumOption<GreedyOrder>("order",
                    "sets the order in which the optimizer selects regions in the callgraph to optimize",
                    GreedyOrder.WCAFirst);

    // TODO maybe move this to JCopterConfig as global option? Could be used for other optimizations if we ever have them
    private static final StringOption TARGETS =
            new StringOption("targets",
                    "comma separated list of target methods for callgraph based optimizations or 'wca' for wca-targets or 'all' for whole app",
                    "main");

    private static final BooleanOption USE_WCEP =
            new BooleanOption("use-wcep", "Optimize only methods on the WCET path if WCA is enabled", true);

    private static final BooleanOption USE_WCA_EXEC_COUNT =
            new BooleanOption("use-wcep-ef", "Use execution frequencies from the WCA if the WCA is enabled", false);

    private static final BooleanOption USE_FREQUENCY_ONLY =
            new BooleanOption("use-freq-only", "Assume an execution count of 1 for every method", false);

    private static final EnumOption<AnalysisType> CACHE_ANALYSIS_TYPE =
            new EnumOption<AnalysisType>("cache-analysis",
                    "Select the cache analysis type",
                    AnalysisType.ALWAYS_HIT);

    private static final EnumOption<CacheCostCalculationMethod> WCA_CACHE_APPROXIMATION =
            new EnumOption<CacheCostCalculationMethod>("wca-cache-analysis",
                    "Set the cache analysis type to be used by the WCA",
                    CacheCostCalculationMethod.ALL_FIT_REGIONS);

    private static final EnumOption<DUMPTYPE> DUMP_TARGET_CALLGRAPH =
            new EnumOption<DUMPTYPE>("dump-target-callgraph", "Dump the callgraph of the target methods", DUMPTYPE.off);

    // This is for debugging, to find bad optimizations in log n iterations
    private static final IntegerOption MAX_STEPS =
            new IntegerOption("max-steps", "Optimize at most n candidates", true);

    private static final StringOption DUMP_STATS =
            new StringOption("dump-stats", "Filename of a CSV file to dump optimization stats into", true);

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".GreedyConfig");

    private final AppInfo appInfo;
    private final JCopter jcopter;

    private final OptionGroup options;

    private List<MethodInfo> targets;
    private boolean useWCEP;

    public static void registerOptions(OptionGroup options) {
        options.addOption(GREEDY_ORDER);
        options.addOption(TARGETS);
        options.addOption(USE_WCEP);
        options.addOption(USE_WCA_EXEC_COUNT);
        options.addOption(USE_FREQUENCY_ONLY);
        options.addOption(CACHE_ANALYSIS_TYPE);
        options.addOption(WCA_CACHE_APPROXIMATION);
        options.addOption(DUMP_TARGET_CALLGRAPH);
        options.addOption(MAX_STEPS);
        options.addOption(DUMP_STATS);
    }

    public GreedyConfig(JCopter jcopter, OptionGroup greedyOptions) throws BadConfigurationException {
        this.jcopter = jcopter;
        this.options = greedyOptions;
        appInfo = AppInfo.getSingleton();
        loadOptions();
    }

    private void loadOptions() throws BadConfigurationException {

        String targetNames = options.getOption(TARGETS);

        if ("all".equals(targetNames)) {
            targets = new ArrayList<MethodInfo>( AppInfo.getSingleton().getCallGraph().getRootMethods() );
        } else if ("wca".equals(targetNames)) {
            targets = jcopter.getJConfig().getWCATargets();
        } else {
            targets = Config.parseMethodList(targetNames);
        }

        useWCEP = useWCA() && options.getOption(USE_WCEP);

        GreedyOrder order = getOrder();
        if (useWCEP && (order == GreedyOrder.BottomUp || order == GreedyOrder.TopDown)) {
            if (options.isSet(USE_WCEP)) {
                logger.warn("WCEP selector does not work with order "+order+", falling back to local WCET selector");
            }
            useWCEP = false;
        }
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public JCopter getJCopter() {
        return jcopter;
    }

    public JCopterConfig getJConfig() {
        return jcopter.getJConfig();
    }

    public GreedyOrder getOrder() {
        return options.getOption(GREEDY_ORDER);
    }

    public List<MethodInfo> getTargetMethods() {
        return targets;
    }

    public Set<MethodInfo> getTargetMethodSet() {
        return new LinkedHashSet<MethodInfo>(targets);
    }

    public AnalysisType getCacheAnalysisType() {
        return options.getOption(CACHE_ANALYSIS_TYPE);
    }

    public boolean useMethodCacheStrategy() {
        return !options.hasValue(WCA_CACHE_APPROXIMATION);
    }

    public CacheCostCalculationMethod getCacheApproximation() {
        CacheCostCalculationMethod defaultValue = options.getOption(WCA_CACHE_APPROXIMATION);
        if (defaultValue != null) {
            return defaultValue;
        }
        AnalysisType analysisType = getCacheAnalysisType();
        if (analysisType == AnalysisType.ALWAYS_HIT) {
            return CacheCostCalculationMethod.ALWAYS_HIT;
        }
        if (analysisType == AnalysisType.ALWAYS_MISS) {
            return CacheCostCalculationMethod.ALWAYS_MISS;
        }
        if (analysisType == AnalysisType.ALWAYS_MISS_OR_HIT) {
            return CacheCostCalculationMethod.ALL_FIT_SIMPLE;
        }
        return CacheCostCalculationMethod.ALL_FIT_REGIONS;
    }

    public List<MethodInfo> getWCATargets() {
        // we could override this for this optimization
        return jcopter.getJConfig().getWCATargets();
    }

    public Set<MethodInfo> getWCATargetSet() {
        return new LinkedHashSet<MethodInfo>(getWCATargets());
    }

    public boolean useWCA() {
        return jcopter.useWCA();
    }

    public boolean useWCEP() {
        return useWCEP;
    }

    public boolean useWCAExecCount() {
        return options.getOption(USE_WCA_EXEC_COUNT);
    }

    public boolean useLocalExecCount() {
        return options.getOption(USE_FREQUENCY_ONLY);
    }

    public int getMaxCodesize() {
        // TODO make this configurable, set default maxCodesize in constructor
        return jcopter.getJConfig().getMaxCodesize();
    }

    public DUMPTYPE getTargetCallgraphDumpType() {
        return options.getOption(DUMP_TARGET_CALLGRAPH);
    }

    public int getMaxSteps() {
        return (options.getOption(MAX_STEPS, 0L)).intValue();
    }

    public boolean doDumpStats() {
        return options.isSet(DUMP_STATS);
    }

    public File getStatsFile() {
        return (doDumpStats()) ? new File(options.getOption(DUMP_STATS)) : null;
    }
}
