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
import com.jopdesign.common.code.CallString;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.InvokeSite;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class ExecFrequencyProvider {

    public abstract long getExecCount(MethodInfo method);

    public long getExecCount(ExecutionContext context) {

        CallString cs = context.getCallString();

        if (cs.isEmpty()) {
            return getExecCount(context.getMethodInfo());
        }

        // if we have a callstring, we need to start at the beginning of the callstring, then multiply the
        // frequencies up to the invoked method along the callstring
        long count = getExecCount(cs.first().getInvoker());

        for (int i = 0; i < cs.length(); i++) {
            InvokeSite is = cs.get(i);
            MethodInfo invokee = i+1 < cs.length() ? cs.get(i+1).getInvoker() : context.getMethodInfo();
            count *= getExecFrequency(is, invokee);
        }

        return count;
    }

    public long getExecCount(InvokeSite invokeSite) {
        return getExecCount(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public long getExecCount(InvokeSite invokeSite, MethodInfo invokee) {
        return getExecCount(invokeSite.getInvoker()) * getExecFrequency(invokeSite, invokee);
    }

    public long getExecCount(MethodInfo method, InstructionHandle ih) {
        return getExecCount(method) * getExecFrequency(method, ih);
    }

    public long getExecFrequency(InvokeSite invokeSite) {
        return getExecFrequency(invokeSite.getInvoker(), invokeSite.getInstructionHandle());
    }

    public abstract long getExecFrequency(InvokeSite invokeSite, MethodInfo invokee);

    public abstract long getExecFrequency(MethodInfo method, InstructionHandle ih);

    public abstract Set<MethodInfo> getChangeSet();

}
