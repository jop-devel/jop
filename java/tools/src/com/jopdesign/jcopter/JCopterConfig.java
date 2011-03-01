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

import com.jopdesign.common.config.BooleanOption;
import com.jopdesign.common.config.Config;
import com.jopdesign.common.config.Option;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class JCopterConfig {

    public static final BooleanOption ALLOW_INCOMPLETE_APP =
            new BooleanOption("allow-incomplete", "Ignore missing classes", false);

    public static final Option[] options =
            { ALLOW_INCOMPLETE_APP };


    private final Config config;

    public JCopterConfig(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public boolean doAllowIncompleteApp() {
        return config.getOption(ALLOW_INCOMPLETE_APP);
    }
}
