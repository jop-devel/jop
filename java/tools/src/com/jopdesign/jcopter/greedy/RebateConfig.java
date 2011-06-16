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
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.JCopterConfig;
import com.jopdesign.jopui.core.OptionGroup;

import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class RebateConfig {

    private final AppInfo appInfo;
    private final JCopterConfig jConfig;

    public RebateConfig(JCopterConfig jConfig, OptionGroup rebateOptions) {
        this.jConfig = jConfig;
        appInfo = AppInfo.getSingleton();
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public JCopterConfig getJConfig() {
        return jConfig;
    }


    public Set<MethodInfo> getRootMethods() {
        return null;
    }

    public Set<MethodInfo> getWCATargets() {
        return null;
    }
}
