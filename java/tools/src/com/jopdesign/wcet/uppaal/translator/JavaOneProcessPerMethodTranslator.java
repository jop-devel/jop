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
package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.code.ControlFlowGraph;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.analysis.AnalysisContextLocal;
import com.jopdesign.wcet.analysis.LocalAnalysis;
import com.jopdesign.wcet.analysis.RecursiveWcetAnalysis;
import com.jopdesign.wcet.analysis.WcetCost;
import com.jopdesign.wcet.ipet.IPETConfig.CacheCostCalculationMethod;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * One-Template-Per-Method process translator
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 */
public class JavaOneProcessPerMethodTranslator extends JavaTranslator {
    public class InvokeViaSyncBuilder extends InvokeBuilder {

        public InvokeViaSyncBuilder(JavaTranslator mt,
                                    TemplateBuilder tBuilder) {
            super(mt, tBuilder, mt.cacheSim);
        }

        /* we need a few nodes for translating an invoke node
         * - A  bbNode (wait, before invoking)
         * - An invokeNode (for waiting while executing the invoked method)
         * - Additionally when using a cache:
         *   - A invokeMissNode (for waiting the miss of the invoked method)
         *     with (bb -> invokeMissNode, invokeMissNode -> wait)
         *     and  (access (-> bb), guard (bb -> invokeMissNode), guard (bb -> invokeNode))
         *   - A returnAccessNode, a returnMissNode and a exitInvokeNode
         *     (for waiting the miss of the returned method)
         * (non-Javadoc)
         * @see com.jopdesign.wcet.uppaal.translator.InvokeBuilder#translateInvoke(com.jopdesign.wcet.frontend.ControlFlowGraph.InvokeNode)
         */

        @Override
        public SubAutomaton translateInvoke(MethodBuilder mBuilder, ControlFlowGraph.InvokeNode n, long staticWCET) {
            /* location for executing the code */
            SubAutomaton basicBlock = mBuilder.createBasicBlock(n.getId(), staticWCET);
            Location startInvoke = basicBlock.getEntry(), finishInvoke;
            Location basicBlockNode = basicBlock.getExit();
            /* location for waiting */
            Location waitInvokeNode = tBuilder.createLocation("INVOKE_WAIT_" + n.getId());
            simulateMethodInvocation(waitInvokeNode, n);
            /* If dynamic cache sim */
            if (javaTranslator.getCacheSim().isDynamic()) {
                Location invokeMissNode = tBuilder.createLocation("INVOKE_MISS_" + n.getId());
                Transition toInvokeHit = tBuilder.createTransition(basicBlockNode, waitInvokeNode);
                Transition toInvokeMiss = tBuilder.createTransition(basicBlockNode, invokeMissNode);
                tBuilder.createTransition(invokeMissNode, waitInvokeNode);
                simulateCacheAccess(
                        n.receiverFlowGraph(), true,
                        basicBlockNode,  /* access cache on ingoing transitions */
                        toInvokeHit,     /* if hit transition */
                        toInvokeMiss,    /* if miss transition */
                        invokeMissNode); /* miss node */
                Location returnAccessNode = tBuilder.createCommitedLocation("RETURN_ACCESS_" + n.getId());
                Location returnMissNode = tBuilder.createLocation("RETURN_MISS_" + n.getId());
                Location exitInvokeNode = tBuilder.createCommitedLocation("EXIT_INVOKE_" + n.getId());
                tBuilder.createTransition(waitInvokeNode, returnAccessNode);
                Transition toReturnHit = tBuilder.createTransition(returnAccessNode, exitInvokeNode);
                Transition toReturnMiss = tBuilder.createTransition(returnAccessNode, returnMissNode);
                tBuilder.createTransition(returnMissNode, exitInvokeNode);
                simulateCacheAccess(
                        n.invokerFlowGraph(), false,
                        returnAccessNode, /* access cache on ingoing transitions */
                        toReturnHit,      /* if hit transition */
                        toReturnMiss,     /* if miss transition */
                        returnMissNode); /* miss node */
                finishInvoke = exitInvokeNode;
            } else {
                tBuilder.createTransition(basicBlockNode, waitInvokeNode);
                finishInvoke = waitInvokeNode;
            }
            return new SubAutomaton(startInvoke, finishInvoke);
        }

        public void simulateMethodInvocation(Location waitInvokeLoc, ControlFlowGraph.InvokeNode n) {
            if (n.receiverFlowGraph().isLeafMethod() && config.collapseLeaves) {
                RecursiveWcetAnalysis<AnalysisContextLocal> ilpAn =
                        new RecursiveWcetAnalysis<AnalysisContextLocal>(project, new LocalAnalysis());
                WcetCost wcet = ilpAn.computeCost(n.getImplementingMethod(),
                        new AnalysisContextLocal(CacheCostCalculationMethod.ALWAYS_HIT));
                tBuilder.waitAtLocation(waitInvokeLoc, wcet.getCost());
            } else {
                int mid = javaTranslator.getMethodID(n.getImplementingMethod());
                tBuilder.getIncomingAttrs(waitInvokeLoc)
                        .setSync(SystemBuilder.methodChannel(mid) + "!");
                tBuilder.getOutgoingAttrs(waitInvokeLoc)
                        .setSync(SystemBuilder.methodChannel(mid) + "?");
            }
        }

    }

    private Map<MethodInfo, TemplateBuilder> processes = new HashMap<MethodInfo, TemplateBuilder>();

    public JavaOneProcessPerMethodTranslator(UppAalConfig c, WCETTool p, MethodInfo root) {
        super(c, p, root);
    }

    @Override
    protected void translate() {
        systemBuilder.addMethodSynchChannels(methodInfos, this.methodIDs);
        String bbClock = systemBuilder.addProcessClock(0);
        /* For each method, create a process */
        for (MethodInfo mi : this.methodInfos) {
            if (project.getCallGraph().isLeafMethod(mi) && config.collapseLeaves) continue;
            int pid = getMethodID(mi);
            TemplateBuilder tBuilder =
                    new TemplateBuilder(config,
                            MiscUtils.qEncode(mi.getFQMethodName()), pid,
                            bbClock);
            recordLoops(mi, tBuilder);
            processes.put(mi, tBuilder);
            translateMethod(tBuilder, tBuilder.getTemplateAutomaton(), pid, mi,
                    new InvokeViaSyncBuilder(this, tBuilder));
            if (mi.equals(root)) {
                tBuilder.getInitial().setCommited();
                tBuilder.addPostEnd();
            } else {
                tBuilder.addSyncLoop();
            }
            try {
                systemBuilder.addTemplate(pid, 0, tBuilder.getFinalTemplate());
            } catch (DuplicateKeyException e) {
                throw new AssertionError("Unexpected exception when adding template: " + e.getMessage());
            }
        }
    }

    private void recordLoops(MethodInfo mi, TemplateBuilder pb) {
        ControlFlowGraph cfg = project.getFlowGraph(mi);
        for (CFGNode hol : cfg.getLoopColoring().getHeadOfLoops()) {
            LoopBound bound = hol.getLoopBound();
            int nesting = cfg.getLoopColoring().getLoopColor(hol).size();
            pb.addLoop(hol, nesting, bound);
        }
    }

}
