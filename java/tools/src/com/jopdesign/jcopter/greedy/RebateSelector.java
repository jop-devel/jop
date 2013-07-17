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
import com.jopdesign.common.ClassInfo;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.CallGraph.ContextEdge;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.graphutils.DFSTraverser;
import com.jopdesign.common.graphutils.DFSTraverser.DFSVisitor;
import com.jopdesign.common.graphutils.DFSTraverser.EmptyDFSVisitor;
import com.jopdesign.common.misc.AppInfoError;
import com.jopdesign.common.processormodel.ProcessorModel;
import com.jopdesign.jcopter.JCopter;
import com.jopdesign.jcopter.analysis.AnalysisManager;
import com.jopdesign.jcopter.analysis.ExecFrequencyProvider;
import com.jopdesign.jcopter.analysis.StacksizeAnalysis;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Stefan Hepp (stefan@stefant.org)
 */
public abstract class RebateSelector implements CandidateSelector {

    public class RebateRatio implements Comparable<RebateRatio> {

        private final Candidate candidate;
        private final long gain;
        private final float ratio;
        private final int order;

        protected RebateRatio(Candidate candidate, long gain, float ratio) {
            this.candidate = candidate;
            this.gain = gain;
            this.ratio = ratio;
            this.order = depthMap.get(candidate.getMethod());
        }

        public Candidate getCandidate() {
            return candidate;
        }

        public long getGain() {
            return gain;
        }

        public float getRatio() {
            return ratio;
        }

        public int getOrder() {
            return order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RebateRatio that = (RebateRatio) o;

            return Float.compare(that.getRatio(), ratio) == 0 && candidate.equals(that.getCandidate());
        }

        @Override
        public int hashCode() {
            int result = candidate.hashCode();
            result = 31 * result + (ratio != +0.0f ? Float.floatToIntBits(ratio) : 0);
            return result;
        }

        @Override
        public int compareTo(RebateRatio o) {
            if (ratio == o.getRatio()) {
                // since we want to keep different entries with the same ratio, use candidate as tiebreaker
                if (candidate.equals(o.getCandidate())) return 0;
                if (order == o.getOrder()) {
                    // same method, use candidate as tie-breaker
                    return candidate.hashCode() < o.getCandidate().hashCode() ? -1 : 1;
                }
                return order > o.getOrder() ? -1 : 1;
            }
            return ratio < o.getRatio() ? -1 : 1;
        }

        public Collection<Candidate> getCandidates() {
            return Collections.singleton(candidate);
        }
    }


    protected class MethodData {
        private List<Candidate> candidates;
        private List<RebateRatio> ratios;

        private MethodData() {
            candidates = new ArrayList<Candidate>();
            ratios = new ArrayList<RebateRatio>();
        }

        public void addCandidates(Collection<Candidate> c) {
            for (Candidate candidate : c) {
                if (checkConstraints(candidate)) {
                    candidates.add(candidate);
                }
            }
        }

        public List<Candidate> getCandidates() {
            return candidates;
        }

        public List<RebateRatio> getRatios() {
            return ratios;
        }
    }

    private static final Logger logger = Logger.getLogger(JCopter.LOG_OPTIMIZER+".RebateSelector");

    protected final AnalysisManager analyses;
    protected final ProcessorModel processorModel;

    protected final Map<MethodInfo, MethodData> methodData;
    // The death-map..
    private final Map<MethodInfo,Integer> depthMap;

    private boolean usesCodeRemover;
    private int maxGlobalSize;
    private int globalCodesize;

    private PrintWriter dump = null;

    public RebateSelector(AnalysisManager analyses, int maxGlobalSize) {
        this.analyses = analyses;
        this.maxGlobalSize = maxGlobalSize;
        this.processorModel = AppInfo.getSingleton().getProcessorModel();

        usesCodeRemover = analyses.getJCopter().getExecutor().useCodeRemover();
        methodData = new LinkedHashMap<MethodInfo, MethodData>();
        depthMap = new LinkedHashMap<MethodInfo, Integer>();
    }

    @Override
    public void initialize(GreedyConfig config, boolean dumpStats) {
        // calculate current global codesize
        globalCodesize = 0;
        if (usesCodeRemover) {
            for (MethodInfo method : AppInfo.getSingleton().getCallGraph().getMethodInfos()) {
                if (!method.hasCode()) continue;
                globalCodesize += method.getCode().getNumberOfBytes();
            }
        } else {
            for (ClassInfo cls : AppInfo.getSingleton().getClassInfos()) {
                for (MethodInfo method : cls.getMethods()) {
                    if (!method.hasCode()) continue;
                    globalCodesize += method.getCode().getNumberOfBytes();
                }
            }
        }

        // we need a tie-breaker for the candidate selection, and to make the results deterministic
        // so we use a topological order of the (initial) callgraph
        DFSVisitor<ExecutionContext,ContextEdge> visitor = new EmptyDFSVisitor<ExecutionContext, ContextEdge>() {
            private int counter = 1;

            @Override
            public void postorder(ExecutionContext node) {
                depthMap.put(node.getMethodInfo(), counter++);
            }
        };

        DirectedGraph<ExecutionContext,ContextEdge> graph = analyses.getTargetCallGraph().getReversedGraph();
        DFSTraverser<ExecutionContext,ContextEdge> traverser = new DFSTraverser<ExecutionContext, ContextEdge>(visitor);
        traverser.traverse(graph);

        logger.info("Initial codesize: " + globalCodesize + " bytes");

        if (config.doDumpStats() && dumpStats) {
            try {
                File statsFile = config.getStatsFile();
                dump = new PrintWriter(statsFile);
                dump.print("total codesize, ");
                if (analyses.useWCAInvoker()) dump.print("WCET, ");
                dump.println("candidate, ratio, gain, local gain, cache, local cache, delta codesize, frequency");
            } catch (FileNotFoundException e) {
                throw new AppInfoError("Could not initialize dump file", e);
            }
        }
    }

    @Override
    public void clear() {
        methodData.clear();
    }

    @Override
    public void printStatistics() {
        logger.info("Codesize after optimization: " + globalCodesize + " bytes");

        dumpStats();
        if (dump != null) {
            dump.close();
        }
    }

    private void dumpStats() {
        if (dump == null) return;

        dump.print(globalCodesize+", ");
        if (analyses.useWCAInvoker()) {
            dump.print(analyses.getWCAInvoker().getLastWCET());
        }
    }

    public void addCandidates(MethodInfo method, Collection<Candidate> candidates) {
        MethodData data = methodData.get(method);
        if (data == null) {
            data = new MethodData();
            methodData.put(method, data);
        }
        data.addCandidates(candidates);
    }

    public void removeCandidate(Candidate candidate) {
        MethodData data = methodData.get(candidate.getMethod());
        data.getCandidates().remove(candidate);
    }

    @Override
    public void removeCandidates(MethodInfo method) {
        MethodData data = methodData.remove(method);
        if (data != null) {
            onRemoveMethodData(data);
        }
    }

    @Override
    public void removeCandidates(MethodInfo method, InstructionHandle start, InstructionHandle end) {
        // TODO go through all candidates of the method, remove all with overlapping range (use positions to check)
        // for now, we just assume that candidates do not overlap ..
        MethodData data = methodData.get(method);


    }

    @Override
    public Collection<Candidate> getCandidates(MethodInfo method) {
        return methodData.get(method).getCandidates();
    }

    @Override
    public void onSuccessfulOptimize(Candidate optimized, List<Candidate> newCandidates) {
        globalCodesize += getDeltaGlobalCodesize(optimized);

        // We can remove candidates from methods which are no longer reachable here already
        // This does not find everything, candidates which are only unreachable in the target graph are removed later
        Collection<MethodInfo> unreachable = optimized.getUnreachableMethods();
        if (unreachable != null && !unreachable.isEmpty()) {
            for (MethodInfo m : unreachable) {
                // codesize of removed candidates already handled above
                removeCandidates(m);
            }
        }

        // replace old candidates with new ones in range
        removeCandidate(optimized);
        removeCandidates(optimized.getMethod(), optimized.getStart(), optimized.getEnd());

        addCandidates(optimized.getMethod(),  newCandidates);
    }

    @Override
    public void updateCandidates(MethodInfo method, ExecFrequencyProvider ecp, StacksizeAnalysis stacksizeAnalysis) {
        MethodData data = methodData.get(method);
        if (data == null) return;

        Iterator<Candidate> it = data.getCandidates().iterator();
        while (it.hasNext()) {
            Candidate c = it.next();
            if (!c.recalculate(analyses, stacksizeAnalysis)) {
                it.remove();
            }
            /* we check this in selectCandidate anyway..
            if (!checkConstraints(c)) {
                it.remove();
            }
            */
        }
    }

    @Override
    public void sortCandidates(ExecFrequencyProvider ecp, Set<MethodInfo> changedMethods) {

        for (MethodInfo method : changedMethods) {
            MethodData data = methodData.get(method);
            // changed methods which are not optimized or not reachable anymore are skipped
            if (data == null) continue;

            // remove candidates of methods which are no longer reachable
            if (!analyses.getTargetCallGraph().containsMethod(method)) {
                removeCandidates(method);
                continue;
            }

            sortMethodData(ecp, data);
        }

    }

    protected boolean checkConstraints(Candidate candidate) {
        // check local and global codesize

        int size = candidate.getMethod().getCode().getNumberOfBytes();
        size += candidate.getDeltaLocalCodesize();

        if (size > processorModel.getMaxMethodSize()) return false;

        if (maxGlobalSize > 0) {
            int newGlobalSize = globalCodesize + getDeltaGlobalCodesize(candidate);
            if (newGlobalSize > maxGlobalSize) return false;
        }

        return true;
    }

    protected RebateRatio createRatio(GainCalculator gc, ExecFrequencyProvider ecp, Candidate candidate, long gain) {
        float codesize = getDeltaGlobalCodesize(candidate);

        float ratio;
        if (codesize > 0) {
            ratio = gc.improveGain(ecp, candidate, gain) / codesize;
        } else {
            // little hack: if we have no codesize increase, use just the gain as factor
            ratio = gc.improveGain(ecp, candidate, gain);
        }

        return new RebateRatio(candidate, gain, ratio);
    }

    protected void logSelection(ExecFrequencyProvider ecp, RebateRatio ratio) {
        if (ratio == null) return;

        long localGain = ratio.getCandidate().getLocalGain();
        long cache = analyses.getMethodCacheAnalysis().getDeltaCacheMissCosts(ecp, ratio.getCandidate());
        long localCache = ratio.getCandidate().getDeltaCacheMissCosts(analyses, ecp);
        int codesize = ratio.getCandidate().getDeltaLocalCodesize();
        long execCount = ecp.getExecCount(ratio.getCandidate().getMethod(), ratio.getCandidate().getEntry());

        logger.info("Selected ratio " + ratio.getRatio() + ", gain " + ratio.getGain() +
                    " local " + localGain + " cache " + cache + " cache local " + localCache +
                    " codesize " + codesize + " freq " + execCount);

        // logging stats from last iteration/initial stats
        dumpStats();

        // logging selection
        if (dump != null) {
            dump.print(", "+ratio.getCandidate().toString());
            dump.print(", "+ratio.getRatio()+", "+ratio.getGain()+", "+ localGain);
            dump.print(", "+cache +", "+localCache+", "+codesize+", "+execCount);
            dump.println();
        }
    }

    public int getDeltaGlobalCodesize(Candidate candidate) {
        int size = candidate.getDeltaLocalCodesize();

        if (usesCodeRemover) {
            Collection<MethodInfo> removed = candidate.getUnreachableMethods();
            if (removed != null) {
                for (MethodInfo m : removed) {
                    size -= m.getCode().getNumberOfBytes();
                }
            }
        }

        return size;
    }

    protected abstract void onRemoveMethodData(MethodData data);

    protected abstract void sortMethodData(ExecFrequencyProvider ecp, MethodData data);

}
