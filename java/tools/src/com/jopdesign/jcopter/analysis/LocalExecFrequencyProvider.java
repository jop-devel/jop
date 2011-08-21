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
import com.jopdesign.common.code.InvokeSite;
import org.apache.bcel.generic.InstructionHandle;

import java.util.Collections;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class LocalExecFrequencyProvider extends ExecFrequencyProvider {

    private final ExecFrequencyProvider ecp;

    public LocalExecFrequencyProvider(ExecFrequencyProvider ecp) {
        this.ecp = ecp;
    }

    @Override
    public long getExecCount(MethodInfo method) {
        return 1;
    }

    @Override
    public long getExecFrequency(InvokeSite invokeSite, MethodInfo invokee) {
        return ecp.getExecFrequency(invokeSite, invokee);
    }

    @Override
    public long getExecFrequency(MethodInfo method, InstructionHandle ih) {
        return ecp.getExecFrequency(method, ih);
    }

    @Override
    public Set<MethodInfo> getChangeSet() {
        return Collections.emptySet();
    }
}
