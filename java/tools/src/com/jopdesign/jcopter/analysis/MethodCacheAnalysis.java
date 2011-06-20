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

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.InvokeSite;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.wcet.WCETProcessorModel;
import com.jopdesign.wcet.jop.MethodCache;

/**
 * This analysis keeps track of the number of bytes and blocks of code reachable from a method and
 * provides estimations of cache miss numbers.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class MethodCacheAnalysis {

    private MethodCache cache;
    private final JCopter jcopter;

    public MethodCacheAnalysis(JCopter jcopter) {
        this.jcopter = jcopter;
        this.cache = jcopter.getMethodCache();
    }

    public int getInvokeMissCount(InvokeSite invokeSite) {
        return 0;
    }

    public int getReturnMissCount(InvokeSite invokeSite) {
        return 0;
    }

    public long getTotalInvokeReturnMissCosts(InvokeSite invokeSite) {
        return getTotalInvokeReturnMissCosts(new CallString(invokeSite));
    }

    public long getTotalInvokeReturnMissCosts(CallString callString) {

        int size = 0;
        for (MethodInfo method : AppInfo.getSingleton().findImplementations(callString)) {
            size = Math.max(size, method.getCode().getNumberOfWords());
        }

        WCETProcessorModel pm = jcopter.getWCETProcessorModel();
        int sizeInvoker = callString.top().getInvoker().getCode().getNumberOfWords();

        return getInvokeMissCount(callString.top()) * pm.getMethodCacheMissPenalty(size, true)
             - getReturnMissCount(callString.top()) * pm.getMethodCacheMissPenalty(sizeInvoker, false);
    }
}
