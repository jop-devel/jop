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
import org.apache.bcel.generic.InstructionHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO maybe also analyze the types on the stack and optionally the value-mapping if needed.
 *
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class StacksizeAnalysis {

    private MethodInfo method;
    private Map<InstructionHandle, Integer> stacksize;

    public StacksizeAnalysis(MethodInfo method) {
        this.method = method;
        stacksize = new HashMap<InstructionHandle, Integer>();
    }

    public MethodInfo getMethod() {
        return method;
    }

    public int getStacksizeBefore(InstructionHandle ih) {
        return stacksize.get(ih);
    }

    public void analyze() {


    }

}
