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

package com.jopdesign.jcopter.analysis;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.jcopter.JCopter;

import java.util.HashMap;
import java.util.Map;

/**
 * Container for various analyses, provide some methods to invalidate/.. all analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnalysisManager {

    private final JCopter jcopter;

    private WCAInvoker wcaInvoker;
    private ExecCountAnalysis execCountAnalysis;
    private MethodCacheAnalysis methodCacheAnalysis;
    private Map<MethodInfo,StacksizeAnalysis> stacksizeMap;

    public AnalysisManager(JCopter jcopter) {
        this.jcopter = jcopter;
        stacksizeMap = new HashMap<MethodInfo, StacksizeAnalysis>();
    }

    public JCopter getJCopter() {
        return jcopter;
    }

    public void createWCAInvoker() {

    }

    public ExecCountAnalysis getExecCountAnalysis() {
        return execCountAnalysis;
    }

    public MethodCacheAnalysis getMethodCacheAnalysis() {
        return methodCacheAnalysis;
    }


    public StacksizeAnalysis getStacksizeAnalysis(MethodInfo methodInfo) {
        StacksizeAnalysis stacksize = stacksizeMap.get(methodInfo);
        if (stacksize == null) {
            stacksize = new StacksizeAnalysis(methodInfo);
            stacksize.analyze();
            stacksizeMap.put(methodInfo, stacksize);
        }
        return stacksize;
    }

    public void clearChangeSets() {
        if (execCountAnalysis != null) execCountAnalysis.clearChangeSet();
        if (methodCacheAnalysis != null) methodCacheAnalysis.clearChangeSet();
    }
}
