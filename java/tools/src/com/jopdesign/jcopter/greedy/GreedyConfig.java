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
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.EnumOption;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.JCopterConfig;
import com.jopdesign.jcopter.analysis.MethodCacheAnalysis.AnalysisType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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


    private final AppInfo appInfo;
    private final JCopter jcopter;

    private final OptionGroup options;

    private List<MethodInfo> targets;

    public static void registerOptions(OptionGroup options) {
        options.addOption(GREEDY_ORDER);
        options.addOption(TARGETS);
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

    public AnalysisType getCacheAnalysisType() {
        return AnalysisType.ALWAYS_MISS_OR_HIT;
    }

    public Collection<MethodInfo> getWCATargets() {
        // we could override this for this optimization
        return jcopter.getJConfig().getWCATargets();
    }

    public boolean useWCA() {
        return jcopter.useWCET();
    }

    public int getMaxCodesize() {
        // TODO make this configurable, set default maxCodesize in constructor
        return jcopter.getJConfig().getMaxCodesize();
    }
}
