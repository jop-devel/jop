/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Wolfgang Puffitsch
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

package com.jopdesign.dfa.framework;

import org.apache.bcel.generic.InstructionHandle;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Flow {

    private Map<InstructionHandle, Set<FlowEdge>> flow;

    public Flow() {
        flow = new LinkedHashMap<InstructionHandle, Set<FlowEdge>>();
    }

    public void clear() {
        flow.clear();
    }

    public void addEdge(FlowEdge f) {
        Set<FlowEdge> set = flow.get(f.getTail());
        if (set == null) {
            set = new LinkedHashSet<FlowEdge>();
            flow.put(f.getTail(), set);
        }
        set.add(f);
    }

    public Set<FlowEdge> getOutEdges(InstructionHandle h) {
        return flow.get(h);
    }

    public String toString() {
        return flow.toString();
    }

}
