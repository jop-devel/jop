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

import com.jopdesign.dfa.DFATool;
import org.apache.bcel.generic.InstructionHandle;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Interpreter<K, V> {

    private Analysis<K, V> analysis;
    private DFATool dfaTool;

    public Interpreter(Analysis<K, V> a, DFATool p) {
        dfaTool = p;
        analysis = a;
    }

    public DFATool getDFATool() {
        return dfaTool;
    }

    public Map<InstructionHandle, ContextMap<K, V>> interpret(Context context,
                                                              InstructionHandle entry,
                                                              Map<InstructionHandle, ContextMap<K, V>> state,
                                                              boolean start)
    {

         LinkedList<FlowEdge> worklist = new LinkedList<FlowEdge>();

        for (FlowEdge f : dfaTool.getFlow().getOutEdges(entry)) {
            if (entry.equals(f.getTail())) {
                worklist.add(new FlowEdge(f, context));
            }
        }

        Map<InstructionHandle, ContextMap<K, V>> result = state;

        if (start) {
            for (InstructionHandle s : dfaTool.getStatements()) {
                result.put(s, analysis.bottom());
            }
            result.put(entry, analysis.initial(entry));
        }

        while (!worklist.isEmpty()) {

            FlowEdge edge = worklist.removeFirst();
            //System.out.println("computing: "+edge);
            InstructionHandle tail = edge.getTail();
            InstructionHandle head = edge.getHead();

            ContextMap<K, V> tailSet = result.get(tail);
            tailSet.setContext(edge.getContext());
            ContextMap<K, V> transferred = analysis.transfer(tail, edge, tailSet, this, result);
            ContextMap<K, V> headSet = result.get(head);

            if (!analysis.compare(transferred, headSet)) {

                ContextMap<K, V> joinedSet = analysis.join(headSet, transferred);
                result.put(head, joinedSet);

                Set<FlowEdge> outEdges = dfaTool.getFlow().getOutEdges(head);
                if (outEdges != null) {
                    for (FlowEdge outEdge : outEdges) {
                        FlowEdge f = new FlowEdge(outEdge, transferred.getContext());
                        if (worklist.isEmpty() || !worklist.getFirst().equals(f)) {
                            if (outEdges.size() > 1)
                                worklist.addLast(f);
                            else
                                worklist.addFirst(f);
                            //System.out.println("pushing: "+f);
                        }
                    }
                }
            }

            //System.out.println("worklist: "+worklist);
        }

        return result;
    }

}
