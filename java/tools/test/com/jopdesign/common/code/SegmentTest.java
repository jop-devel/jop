/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2011, Benedikt Huber (benedikt@vmars.tuwien.ac.at)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.common.code;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jopdesign.common.AppInfo;
import com.jopdesign.common.AppSetup;
import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.TestFramework;
import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ControlFlowGraph.InvokeNode;
import com.jopdesign.common.code.SuperGraph.ContextCFG;
import com.jopdesign.common.code.SuperGraph.SuperEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphEdge;
import com.jopdesign.common.code.SuperGraph.SuperGraphNode;
import com.jopdesign.common.code.SuperGraph.SuperInvokeEdge;
import com.jopdesign.common.misc.BadGraphException;
import com.jopdesign.common.misc.Iterators;

/**
 * Purpose:
 * @author Benedikt Huber (benedikt@vmars.tuwien.ac.at)
 *
 */
public class SegmentTest implements CFGProvider {
	AppInfo appInfo;

	public static void check(String msg, boolean test) {
		System.out.println(msg + ": " + (test ? "OK" : "FAIL"));
	}

	public static<T> void checkEquals(String msg, T expect, T actual) {
		if(expect.equals(actual)) {
			System.out.println(msg + " OK both " + expect);			
		} else {
			System.out.println(msg + " FAILED because expected /= actual: " + expect + " /= " + actual);						
		}
	}
	public static<T extends Comparable<T>> void checkLessEqual(String msg, T o1, T o2) {
		if(o1.compareTo(o2) < 1) {
			System.out.println(msg + " OK with " + o1 + " <= " + o2);			
		} else {
			System.out.println(msg + " FAILED because " + o1 + " > " + o2);						
		}
	}

	public static void main(String[] args) {
		TestFramework testFramework = new TestFramework();
		AppSetup setup =  testFramework.setupAppSetup("java/tools/test/test/cg1.zip", null);
		AppInfo appInfo = testFramework.setupAppInfo("wcet.devel.CallGraph1.run", true);
		
		SegmentTest testInst = new SegmentTest();
		testInst.appInfo = appInfo;

		MethodInfo mainMethod = appInfo.getMainMethod();		

		/* count total number of CFG nodes */
		SuperGraph superGraph = new SuperGraph(testInst, testInst.getFlowGraph(mainMethod), 2);
		Segment segment = Segment.methodSegment(mainMethod, CallString.EMPTY, testInst, 2, superGraph.getInfeasibleEdgeProvider());
		
		int count = 0;
		for(ContextCFG cgNode : superGraph.getCallGraphNodes()) {
			try {
				cgNode.getCfg().exportDOT(new File("/tmp/cfg-"+cgNode.getCfg().getMethodInfo().getClassName()+"_"+cgNode.getCfg().getMethodInfo().getShortName()+".dot"));
			} catch (IOException e) {}
			count += cgNode.getCfg().vertexSet().size();
		}
		checkEquals("[Segment 1] Expected node count", (count-2), Iterators.size(segment.getNodes()));
		
		
		try {
			segment.exportDOT(new File("/tmp/cg1-segment.dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* root */
		ContextCFG root = superGraph.getRootNode();

		/* Build a segment cuts all invokes in those methods invoked by run() */
		Segment segment2;
		
		/* root entries */
		Set<SuperGraphEdge> entries = new HashSet<SuperGraphEdge>();
		Iterators.addAll(entries, superGraph.liftCFGEdges(root, root.getCfg().outgoingEdgesOf(root.getCfg().getEntry())));

		Set<SuperGraphEdge> exits = new HashSet<SuperGraphEdge>();
		int cfgNodeCandidateCount = root.getCfg().vertexSet().size();
		
		/* find callees */
		for(SuperEdge superEdge : superGraph.getCallGraph().outgoingEdgesOf(root)) {
			if(! (superEdge instanceof SuperInvokeEdge)) continue;
			ContextCFG callee1 = superGraph.getCallGraph().getEdgeTarget(superEdge);
			cfgNodeCandidateCount += callee1.getCfg().vertexSet().size();
			/* find all edges from invoke nodes */
			for(CFGNode cfgNode : callee1.getCfg().vertexSet()) {
				if(cfgNode instanceof InvokeNode) {					
					Iterators.addAll(exits, superGraph.outgoingEdgesOf(new SuperGraphNode(callee1, cfgNode)));
				}
			}			
		}
		segment2 = new Segment(superGraph, entries, exits);
		exits = segment2.getExitEdges(); /* reachable exits */
		try {
			segment2.exportDOT(new File("/tmp/cg1-segment2.dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		checkEquals("[Segment 2] Expected node count", 14, Iterators.size(segment2.getNodes())+2);
		checkLessEqual("[Segment 2] Expected node count <= |root + directly invoked|", 
				Iterators.size(segment2.getNodes()) + 2, cfgNodeCandidateCount);
		
		/* Another segment, with entries the exits of the last segment, and exits all invokes in methods the entries */
		Segment segment3;
		entries = segment2.getExitEdges();
		exits = new HashSet<SuperGraphEdge>();
		cfgNodeCandidateCount = 0;
		/* find callees */
		
		for(SuperGraphEdge superEdge : entries) {
			SuperGraphNode node1 = superEdge.getTarget();
			for(SuperEdge superEdge2 : superGraph.getCallGraph().outgoingEdgesOf(node1.getContextCFG())) {
				if(! (superEdge2 instanceof SuperInvokeEdge)) continue;
				ContextCFG callee2 = superGraph.getCallGraph().getEdgeTarget(superEdge2);
				/* find all edges from invoke nodes */
				for(CFGNode cfgNode : callee2.getCfg().vertexSet()) {
					if(cfgNode instanceof InvokeNode) {					
						Iterators.addAll(exits, superGraph.outgoingEdgesOf(new SuperGraphNode(callee2, cfgNode)));
					}
				}				
			}			
		}
		segment3 = new Segment(superGraph, entries, exits);
		try {
			segment3.exportDOT(new File("/tmp/cg1-segment3.dot"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		checkEquals("[Segment 2] 3 exits", 3, segment2.getExitEdges().size());
		checkEquals("[Segment 3] 3 entries", 3, segment3.getEntryEdges().size());
		checkEquals("[Segment 3] 4 exits", 4, segment3.getExitEdges().size());
	}

	@Override
	public ControlFlowGraph getFlowGraph(MethodInfo method) {
		ControlFlowGraph cfg = appInfo.getFlowGraph(method);
		try {
			cfg.resolveVirtualInvokes();
			cfg.insertReturnNodes();
		} catch (BadGraphException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return cfg;
	}
}
