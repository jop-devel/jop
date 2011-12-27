/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2011, Stefan Hepp (stefan@stefant.org).
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

package com.jopdesign.common.code;

import com.jopdesign.common.KeyManager;
import com.jopdesign.common.KeyManager.CustomKey;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.JavaClassFormatError;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.JSR;
import org.apache.bcel.generic.JSR_W;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.UnconditionalBranch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is basically the same as the Interpreter of the DFA, except that we do not use a flow graph
 * and that a ContextMap is not supported by the interpreter itself (instead it must be handled by the analysis).
 *
 * TODO maybe merge those two implementations
 *
 * @author Stefan Hepp (stefan@stefant.org)
 * @author Wolfgang Puffitsch
 */
public class InstructionInterpreter<T> {

    public enum EdgeType { NORMAL_EDGE, TRUE_EDGE, FALSE_EDGE, EXIT_EDGE }

    public static class Edge {
        private InstructionHandle tail, head;
        private EdgeType type;

        public Edge(InstructionHandle tail, InstructionHandle head, EdgeType type) {
            this.tail = tail;
            this.head = head;
            this.type = type;
        }

        public InstructionHandle getTail() {
            return tail;
        }

        public InstructionHandle getHead() {
            return head;
        }

        public EdgeType getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return 31 * tail.hashCode() + head.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Edge)) return false;
            Edge e = (Edge) obj;
            return tail.equals(e.getTail()) && head.equals(e.getHead());
        }
    }

    /**
     * This key is used to attach a NOP instruction handle to each method which is used as handle for
     * the result state of a method.
     */
    private static final CustomKey KEY_NOP = KeyManager.getSingleton().registerStructKey("InstructionInterpreter.NOP");

    private final MethodInfo methodInfo;
    private final InstructionAnalysis<T> analysis;

    private final Map<InstructionHandle, T> results;

    private boolean startAtExceptionHandlers = false;

    public InstructionInterpreter(MethodInfo methodInfo, InstructionAnalysis<T> analysis) {
        this.methodInfo = methodInfo;
        this.analysis = analysis;
        results = new HashMap<InstructionHandle, T>();
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public boolean doStartAtExceptionHandlers() {
        return startAtExceptionHandlers;
    }

    public void setStartAtExceptionHandlers(boolean startAtExceptionHandlers) {
        this.startAtExceptionHandlers = startAtExceptionHandlers;
    }

    public void reset(InstructionHandle from, InstructionHandle to) {
        InstructionHandle ih = from;
        while (ih != to) {
            // we remove the entry, initial value will be set by interpret()
            results.remove(ih);
            ih = ih.getNext();
        }
        results.put(ih, analysis.bottom());
    }

    public void interpret(boolean initialize) {
        InstructionList il = methodInfo.getCode().getInstructionList(true, false);
        InstructionHandle entry = il.getStart();

        Map<InstructionHandle,T> start = new HashMap<InstructionHandle, T>();
        start.put(entry, initialize ? analysis.initial(entry) : analysis.bottom());

        // start at exception handler entries too?
        if (startAtExceptionHandlers) {
            for (CodeExceptionGen eg : methodInfo.getCode().getExceptionHandlers()) {
                InstructionHandle ih = eg.getHandlerPC();
                start.put(ih, initialize ? analysis.initial(eg) : analysis.bottom());
            }
        }

        interpret(il, start, initialize);
    }

    public void interpret(InstructionHandle entry, boolean initialize) {
        // TODO we could use the CFG instead if it exists ?
        InstructionList il = methodInfo.getCode().getInstructionList(true, false);

        Map<InstructionHandle,T> start = new HashMap<InstructionHandle, T>(1);
        start.put(entry, initialize ? analysis.initial(entry) : analysis.bottom());
        interpret(il, start, initialize);
    }

    private void interpret(InstructionList il, Map<InstructionHandle, T> start, boolean initialize) {

        if (initialize) {
            InstructionHandle ih = il.getStart();
            // set initial value for all instructions
            while (ih != null) {
                results.put(ih, analysis.bottom());
                ih = ih.getNext();
            }

            results.putAll(start);
        } else {
            // Set initial values for new instructions
            for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
                if (results.containsKey(ih)) continue;

                if (ih.getPrev() == null) {
                    // entry instruction, must always be merged with the initial state once, even
                    // if this has an ingoing edge
                    results.put(ih, analysis.initial(ih));
                } else {
                    results.put(ih, analysis.bottom());
                    // check for exception handler entry
                    if (ih.hasTargeters()) {
                        for (InstructionTargeter targeter : ih.getTargeters()) {
                            if (targeter instanceof CodeExceptionGen &&
                                ((CodeExceptionGen)targeter).getHandlerPC().equals(ih))
                            {
                                results.put(ih, analysis.initial((CodeExceptionGen) targeter));
                                break;
                            }
                        }
                    }
                }
            }
        }

        LinkedList<Edge> worklist = new LinkedList<Edge>();

        // setup the worklist
        for (InstructionHandle ih : start.keySet()) {
            if (initialize) {
                // when initializing, we start from the initial values, not from ingoing edges
                worklist.addAll(getOutEdges(ih));
            } else {
                // we continue from existing results
                List<Edge> inEdges = getInEdges(il, ih);
                if (inEdges.isEmpty()) {
                    // entry instruction without ingoing edges, nothing to continue from
                    worklist.addAll(getOutEdges(ih));
                } else {
                    worklist.addAll(inEdges);
                }
            }
        }

        while (!worklist.isEmpty()) {

            Edge edge = worklist.removeFirst();
            InstructionHandle tail = edge.getTail();
            InstructionHandle head = edge.getHead();

            T tailValue = results.get(tail);
            T headValue = results.get(head);
            T transferred = analysis.transfer(tailValue, edge);

            if (!analysis.compare(transferred, headValue)) {

                T newValue = analysis.join(transferred, headValue);
                results.put(head, newValue);

                if (edge.getType() == EdgeType.EXIT_EDGE) {
                    continue;
                }

                List<Edge> outEdges = getOutEdges(head);
                for (Edge outEdge : outEdges) {
                    if (worklist.contains(outEdge)) {
                        continue;
                    }
                    if (outEdges.size() > 1) {
                        worklist.addLast(outEdge);
                    } else {
                        worklist.addFirst(outEdge);
                    }
                }

            }
        }

    }

    public T getResult(InstructionHandle ih) {
        return results.get(ih);
    }

    public Map<InstructionHandle,T> getResults() {
        return results;
    }

    public InstructionHandle getExitInstruction() {
        InstructionHandle exit = (InstructionHandle) methodInfo.getCustomValue(KEY_NOP);
        if (exit == null) {
            exit = new InstructionList().append(new NOP());
            methodInfo.setCustomValue(KEY_NOP, exit);
        }
        return exit;
    }

    private List<Edge> getOutEdges(InstructionHandle ih) {
        List<Edge> edges = new LinkedList<Edge>();

        Instruction instr = ih.getInstruction();
        if (instr instanceof BranchInstruction) {
            if (instr instanceof Select) {
                Select s = (Select) instr;
                InstructionHandle[] target = s.getTargets();
                for (InstructionHandle aTarget : target) {
                    edges.add(new Edge(ih, aTarget, EdgeType.TRUE_EDGE));
                }
                edges.add(new Edge(ih, s.getTarget(), EdgeType.FALSE_EDGE));
            } else {
                BranchInstruction b = (BranchInstruction) instr;
                edges.add(new Edge(ih, b.getTarget(), EdgeType.TRUE_EDGE));
            }
        }
        // Check if we can fall through to the next instruction
        if (ih.getNext() != null
            && !(instr instanceof UnconditionalBranch
                 || instr instanceof Select || instr instanceof ReturnInstruction))
        {
            if (instr instanceof BranchInstruction) {
                edges.add(new Edge(ih, ih.getNext(), EdgeType.FALSE_EDGE));
            } else {
                edges.add(new Edge(ih, ih.getNext(), EdgeType.NORMAL_EDGE));
            }
        }
        if (instr instanceof ReturnInstruction) {
            edges.add(new Edge(ih, getExitInstruction(), EdgeType.EXIT_EDGE));
        }
        if (instr instanceof ATHROW) {
            // TODO should we handle this somehow? Insert edges to the exception handlers or to an return-by-exception
            //      exit instruction?
            // for now, just ignore them
        }

        // TODO handle JSR (jump to subroutine, continue after JSR) and RET (tricky.. jump back to corresponding JSR)
        //      but for now, we just ignore them too.. in a safe way :)
        if (instr instanceof RET || instr instanceof JSR || instr instanceof JSR_W) {
            throw new JavaClassFormatError("Unsupported instruction "+instr+" in "+methodInfo);
        }

        return edges;
    }

    private List<Edge> getInEdges(InstructionList il, InstructionHandle ih) {
        List<Edge> edges = new LinkedList<Edge>();

        InstructionHandle prev = ih.getPrev();
        if (prev != null) {
            // check if we can fall through from prev instruction
            Instruction instr = prev.getInstruction();
            if (!(instr instanceof UnconditionalBranch
                 || instr instanceof Select || instr instanceof ReturnInstruction))
            {
                if (instr instanceof BranchInstruction) {
                    edges.add(new Edge(prev, ih, EdgeType.FALSE_EDGE));
                } else {
                    edges.add(new Edge(prev, ih, EdgeType.NORMAL_EDGE));
                }
            }
        }

        // This is bad: because we do not get the instruction handles of the targeters, we need to search the
        // whole instruction list. There is no other way to find the ingoing edges except than constructing the
        // flow graph explicitly
        for (InstructionHandle targeter = il.getStart(); targeter != null; targeter = targeter.getNext()) {
            // TODO we do not care about CodeExceptionGen targeters.. or should we? We should set the initial value
            //      for the instruction for the exception handler
            if (!(targeter.getInstruction() instanceof BranchInstruction)) continue;

            BranchInstruction bi = (BranchInstruction) targeter.getInstruction();
            if (bi.containsTarget(ih)) {
                edges.add(new Edge(targeter, ih, EdgeType.TRUE_EDGE));
            }
        }

        return edges;
    }

}
