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

/**
 * Container for various analyses, provide some methods to invalidate/.. all analyses.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class AnalysisManager {

    private ExecCountAnalysis execCountAnalysis;
    private MethodCacheAnalysis methodCacheAnalysis;

    public AnalysisManager(ExecCountAnalysis execCountAnalysis, MethodCacheAnalysis methodCacheAnalysis) {
        this.execCountAnalysis = execCountAnalysis;
        this.methodCacheAnalysis = methodCacheAnalysis;
    }

    public ExecCountAnalysis getExecCountAnalysis() {
        return execCountAnalysis;
    }

    public MethodCacheAnalysis getMethodCacheAnalysis() {
        return methodCacheAnalysis;
    }


}
