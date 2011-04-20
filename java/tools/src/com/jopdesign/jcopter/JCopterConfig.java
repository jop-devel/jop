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
import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Config.BadConfigurationException;
import com.jopdesign.common.config.Option;
import com.jopdesign.common.config.OptionGroup;
import com.jopdesign.common.config.StringOption;

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

    private static final StringOption OPTIMIZE =
            new StringOption("optimize", "can be one of 's[ize]', '1' or '2'", 'O', "1");

    private static final StringOption MAX_CODE_SIZE =
            new StringOption("max-code-size", "maximum total code size", true);

    private static final Option[] optionList =
            { ASSUME_REFLECTION, OPTIMIZE, MAX_CODE_SIZE };

    public static void registerOptions(OptionGroup options) {
        options.addOptions(JCopterConfig.optionList);
    }

    private final OptionGroup options;

    public JCopterConfig(OptionGroup options) throws BadConfigurationException {
        this.options = options;
        loadOptions();
    }

    private void loadOptions() throws BadConfigurationException {
        
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

    /**
     * @return true if we need to assume that the class hierarchy is not fully known
     */
    public boolean doAssumeIncompleteApp() {
        return getAppInfo().doIgnoreMissingClasses();
    }

    public int getCallstringLength() {
        return (int) getConfig().getOption(Config.CALLSTRING_LENGTH).longValue();
    }
}
