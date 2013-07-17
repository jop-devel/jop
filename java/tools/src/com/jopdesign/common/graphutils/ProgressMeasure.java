/*
 * This file is part of JOP, the Java Optimized Processor
 * see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)
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

package com.jopdesign.common.graphutils;

import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.MiscUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * Module to compute relative progess measure
 * Fun fact: While implementing, I suddenly recognized this is a
 * generalized tree based WCET computation :)
 */
public class ProgressMeasure<V, E> {
    public static class RelativeProgress<V> {
        public RelativeProgress(long staticDiff, Map<V, Long> loopDiff) {
            this.staticDiff = staticDiff;
            this.loopDiff = loopDiff;
        }

        public final long staticDiff;
        public final Map<V, Long> loopDiff;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(staticDiff);
            for (Entry<V, Long> entry : loopDiff.entrySet()) {
                sb.append(String.format(" - %d * iter(%s)", entry.getValue(), entry.getKey()));
            }
            return sb.toString();
        }
    }

    /* input */
    private LoopColoring<V, E> loopColors;
    private FlowGraph<V, E> cfg;
    private Map<V, Long> loopBounds;
    private Map<V, Long> blockMeasure;
    /* state */
    private HashMap<V, Long> pMaxMap = null;
    private HashMap<V, Long> loopProgress = null;

    public long getLoopProgress(V target) {
        getMaxProgress();
        return this.loopProgress.get(target);
    }

    public ProgressMeasure(FlowGraph<V, E> cfg,
                           LoopColoring<V, E> loopColors,
                           Map<V, Long> loopBounds,
                           Map<V, Long> blockMeasure) {
        this.cfg = cfg;
        this.loopBounds = loopBounds;
        this.loopColors = loopColors;
        this.blockMeasure = blockMeasure;
    }


    /**
     * The fun algorithm to compute relative progress measures.
     *
     * @return
     */
    public Map<V, Long> getMaxProgress() {
        if (pMaxMap != null) return pMaxMap;
        /* pmax: maximal progress upto the given node */
        pMaxMap = new HashMap<V, Long>();
        loopProgress = new HashMap<V, Long>();
        /* for all loops in reverse topo order of the loop nest tree */
        List<V> reverseTopo = MiscUtils.reverseTopologicalOrder(loopColors.getLoopNestDAG());

        for (V hol : reverseTopo) {
            /* set progress of loop header to 0 */
            pMaxMap.put(hol, 0L);
            /* get linear subgraph */
            DirectedGraph<V, E> subGraph = loopColors.getLinearSubgraph(hol);
            /* update pmax (in topo order of the linear subgraph) */
            for (V node : MiscUtils.topologicalOrder(subGraph)) {
                if (node == hol) continue;
                updatePMax(subGraph, node, loopColors);
            }
            /* update loopProgress */
            long loopProgressMax = 0;
            for (E backEdge : loopColors.getBackEdgesByHOL().get(hol)) {
                loopProgressMax = Math.max(loopProgressMax, getMaxProgressVia(cfg, backEdge));
            }
            this.loopProgress.put(hol, loopProgressMax);
        }
        /* update pmax (linear subgraph of the method) */
        DirectedGraph<V, E> subGraph = loopColors.getLinearSubgraph(null);
        pMaxMap.put(cfg.getEntry(), 0L);
        for (V node : MiscUtils.topologicalOrder(subGraph)) {
            if (node == cfg.getEntry()) continue;
            updatePMax(subGraph, node, loopColors);
        }
        return pMaxMap;
    }

    public Map<E, RelativeProgress<V>> computeRelativeProgress() {
        Map<V, Long> maxProgress = getMaxProgress();
        Map<E, RelativeProgress<V>> relProgress = new HashMap<E, RelativeProgress<V>>();
        for (E e : cfg.edgeSet()) {
            V src = cfg.getEdgeSource(e);
            V target = cfg.getEdgeTarget(e);
            Long pmSrc = maxProgress.get(src);
            Long pmTarget = maxProgress.get(target);
            long staticDiff = pmTarget - pmSrc;
            if (this.loopColors.isBackEdge(e)) staticDiff += getLoopProgress(target);
            Map<V, Long> loopDiff = new HashMap<V, Long>();
            for (V exit : loopColors.getLoopExitSet(e)) {
                loopDiff.put(exit, getLoopProgress(exit));
            }
            relProgress.put(e, new RelativeProgress<V>(staticDiff, loopDiff));
        }
        return relProgress;
    }

    private void updatePMax(DirectedGraph<V,
            E> subgraph, V node,
                            LoopColoring<V, E> color) {
        long pMax = 0;
        for (E incoming : subgraph.incomingEdgesOf(node)) {
            pMax = Math.max(pMax, getMaxProgressVia(subgraph, incoming));
        }
        pMaxMap.put(node, pMax);
    }

    private long getMaxProgressVia(DirectedGraph<V, E> subgraph, E incoming) {
        V src = subgraph.getEdgeSource(incoming);
        long pMaxPred = pMaxMap.get(src);
        for (V exitLoop : loopColors.getLoopExitSet(incoming)) {
            pMaxPred += loopProgress.get(exitLoop) * loopBounds.get(exitLoop);
        }
        return pMaxPred + blockMeasure.get(src);
    }
    /* test */

    public static void main(String argv[]) {
        int nodes[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        long blockm[] = {1, 1, 22, 4, 4, 4, 2, 1, 1, 1, 1};
        int edges[][] = {{1, 2}, {2, 3}, {2, 4}, {4, 5},
                {5, 6}, {6, 7}, {6, 2}, {7, 5}, {7, 10},
                {5, 8}, {3, 9}, {8, 9}, {9, 2},
                {8, 10}, {9, 11}, {10, 11}};
        FlowGraph<Integer, DefaultEdge> testGraph =
                new DefaultFlowGraph<Integer, DefaultEdge>(DefaultEdge.class, 0, 11);
        for (int n : nodes) testGraph.addVertex(n);
        for (int[] e : edges) testGraph.addEdge(e[0], e[1]);

        Map<Integer, Long> testLoopBounds = new HashMap<Integer, Long>();
        testLoopBounds.put(2, 7L);
        testLoopBounds.put(5, 15L);

        TopOrder<Integer, DefaultEdge> topo = null;
        try {
            topo = new TopOrder<Integer, DefaultEdge>(testGraph, 1);
        } catch (BadGraphException e1) {
            e1.printStackTrace();
        }
        LoopColoring<Integer, DefaultEdge> testLoopColors
                = new LoopColoring<Integer, DefaultEdge>(testGraph, topo, 11);
        HashMap<Integer, Long> testBlockMeasure = new HashMap<Integer, Long>();
        for (int i = 0; i < nodes.length; i++) {
            testBlockMeasure.put(nodes[i], blockm[i]);
        }
        ProgressMeasure<Integer, DefaultEdge> pm =
                new ProgressMeasure<Integer, DefaultEdge>(testGraph, testLoopColors, testLoopBounds, testBlockMeasure);
        Map<Integer, Long> maxProgress = pm.getMaxProgress();
        MiscUtils.printMap(System.out, new TreeMap<Integer, Long>(maxProgress), 10, 0);

        /* compute relative updates */
        for (Entry<DefaultEdge, RelativeProgress<Integer>> x : pm.computeRelativeProgress().entrySet()) {
            int src = testGraph.getEdgeSource(x.getKey());
            int target = testGraph.getEdgeTarget(x.getKey());
            System.out.println(String.format("%2d --> %2d: +%s", src, target, x.getValue()));
        }
    }
}
