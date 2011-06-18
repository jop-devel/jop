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
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;
import sun.reflect.generics.tree.ReturnType;

import java.util.List;

/**
 * This class contains all generic options for JCopter.
 *
 * Options of optimizations are defined in their respective classes and are added to the config
 * by the PhaseExecutor.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JCopterConfig {

    private static final BooleanOption ASSUME_REFLECTION =
            new BooleanOption("assume-reflection",
                    "Assume that reflection is used. If not set, check the code for reflection code.", false);

    private static final BooleanOption ASSUME_DYNAMIC_CLASSLOADING =
            new BooleanOption("assume-dynloader", "Assume that classes can be loaded or replaced at runtime.", false);

    private static final StringOption OPTIMIZE =
            new StringOption("optimize", "can be one of: 's' (size), '1' (fast optimizations only), '2' (most optimizations), '3' (bring on the big guns)", 'O', "1");

    private static final BooleanOption ALLOW_EXPERIMENTAL =
            new BooleanOption("experimental", "Enable experimental optimizations which are still in development", false);

    private static final StringOption MAX_CODE_SIZE =
            new StringOption("max-code-size", "maximum total code size, 'kb' or 'mb' can be used as suffix", true);

    private static final StringOption WCA_TARGETS =
            new StringOption("wca-targets", "comma separated list of target-methods if the WCA is used (main class is used if the classname is omitted)", "measure");

    private static final Option[] optionList =
            { OPTIMIZE, ALLOW_EXPERIMENTAL, MAX_CODE_SIZE,
              ASSUME_REFLECTION, ASSUME_DYNAMIC_CLASSLOADING,
              WCA_TARGETS };

    public static void registerOptions(OptionGroup options) {
        options.addOptions(JCopterConfig.optionList);
    }

    private final OptionGroup options;
    
    private byte optimizeLevel;
    private int  maxCodesize;
    private List<MethodInfo> wcaTargets;

    public JCopterConfig(OptionGroup options) throws BadConfigurationException {
        this.options = options;
        loadOptions();
    }

    private void loadOptions() throws BadConfigurationException {
        if ("s".equals(options.getOption(OPTIMIZE))) {
            optimizeLevel = 0;
        } else if ("1".equals(options.getOption(OPTIMIZE))) {
            optimizeLevel = 1;
        } else if ("2".equals(options.getOption(OPTIMIZE))) {
            optimizeLevel = 2;
        } else if ("3".equals(options.getOption(OPTIMIZE))) {
            optimizeLevel = 3;
        } else {
            throw new BadConfigurationException("Invalid optimization level '"+options.getOption(OPTIMIZE)+"'");
        }

        String max = options.getOption(MAX_CODE_SIZE);
        if (max != null) {
            max = max.toLowerCase();
            if (max.endsWith("kb")) {
                maxCodesize = Integer.parseInt(max.substring(0,max.length()-2)) * 1024;
            } else if (max.endsWith("mb")) {
                maxCodesize = Integer.parseInt(max.substring(0,max.length()-2)) * 1024 * 1024;
            } else {
                maxCodesize = Integer.parseInt(max);
            }
        } else {
            // TODO if we do not have max size: should we use some heuristics to limit codesize?

        }
    }

    /**
     * Check the options, check if the assumptions on the code hold.
     */
    public void checkOptions() {
        // TODO implement reflection check, implement incomplete code check
    }

    public AppInfo getAppInfo() {
        return AppInfo.getSingleton();
    }

    public Config getConfig() {
        return options.getConfig();
    }

    /**
     * @return true if we need to assume that reflection is used in the code.
     */
    public boolean doAssumeReflection() {
        return options.getOption(ASSUME_REFLECTION);
    }

    public boolean doAssumeDynamicClassLoader() {
        return options.getOption(ASSUME_DYNAMIC_CLASSLOADING);
    }

    /**
     * @return true if we need to assume that the class hierarchy is not fully known
     */
    public boolean doAssumeIncompleteAppInfo() {
        // TODO Reflection may lead to loading of additional code not referenced explicitly in ConstantPool
        //      on the other hand, it might not.. We could distinguish this using additional options.
        return getAppInfo().doIgnoreMissingClasses() || doAssumeReflection() || doAssumeDynamicClassLoader();
    }

    public int getCallstringLength() {
        return (int) getConfig().getOption(Config.CALLSTRING_LENGTH).longValue();
    }

    public int getMaxCodesize() {
        return maxCodesize;
    }

    /**
     * Includes optimizations which are more expensive, excludes optimization which increase size.
     * @return true if we should only perform optimizations which reduce the total codesize.
     */
    public boolean doOptimizeCodesizeOnly() {
        return optimizeLevel == 0;
    }

    /**
     * Includes optimizations which may increase size, excludes optimizations which are more expensive.
     * @return true if we only want fast optimizations.
     */
    public boolean doOptimizeFastOnly() {
        return optimizeLevel == 1;
    }

    /**
     * Includes all optimizations which are more expensive, may increase size. Excludes optimizations which may take
     * a long time.
     * @return true if we want most optimizations to execute.
     */
    public boolean doOptimizeNormal() {
        return optimizeLevel >= 2;
    }

    /**
     * Includes everything.
     * @return true if we want to try really hard to optimize, even if it may take a long time.
     */
    public boolean doOptimizeHard() {
        return optimizeLevel >= 3;
    }

    public boolean doAllowExperimental() {
        return options.getOption(ALLOW_EXPERIMENTAL);
    }
}
