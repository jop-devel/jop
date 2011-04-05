/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)

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
package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.common.code.ControlFlowGraph.CFGNode;
import com.jopdesign.common.code.ExecutionContext;
import com.jopdesign.common.code.LoopBound;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.model.LayoutCFG;
import com.jopdesign.wcet.uppaal.model.Location;
import com.jopdesign.wcet.uppaal.model.Location.LocationAttribute;
import com.jopdesign.wcet.uppaal.model.Template;
import com.jopdesign.wcet.uppaal.model.Transition;
import com.jopdesign.wcet.uppaal.model.TransitionAttributes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Builder for program templates.
 * Built-in support for
 * <ul>
 *   <li/> Initial / end location
 *   <li/> Wait nodes (wait for the specified amount of time on the given clock)
 *   <li/> Loop counters (set to _|_, T, increment, guard)
 * </ul>
 */
public class TemplateBuilder {

	public static String loopVarName(int loopId) {
		return("loop_cnt_"+loopId);
	}
	public static String loopBoundConst (int loopId) {
		return("LOOP_BOUND_"+loopId);
	}
	public static String loopLowerBoundConst(int loopId) {
		return("LOOP_LOWERBOUND_"+loopId);
	}

	//private Vector<Integer> loopVariables;
	private UppAalConfig config;

	private Template template;
	private Template getTemplate() { return this.template; }
	
	private Map<Location, TransitionAttributes> outgoingAttrs;
	private Map<Location, TransitionAttributes> incomingAttrs;
	private Location initLoc;
	private Location endLoc;
	/* which variable to use for the given HOL */
	private HashMap<CFGNode, Integer> loopVars;
	/* upper bound for loop variables */
	private Vector<Long> loopVarBounds = new Vector<Long>();
	/* which bound constants to use for the given HOL */
	private HashMap<CFGNode,Integer> loopBounds;
	private String clockBB;

	private Location postEnd;

	private int pid;
	public Vector<Long> getLoopVarBounds() {
		return this.loopVarBounds;
	}


	/**
	 * Build a fresh template representing a CFG or a supergraph, with two
	 * dedicated locations {@code initial} and {@code end}.
	 * A number of loop variables, depending on the maximum nesting depth is created.
	 * @param name the template's name
	 * @param bbClock the basic block clock
	 */
	public TemplateBuilder(UppAalConfig config,
						   String name, int processId,
						   String bbClock) {
		this.config = config;
		this.pid = processId;
		this.template = new Template(name,new Vector<String>());
		//this.loopVariables = loopVariables;
		this.incomingAttrs = new HashMap<Location, TransitionAttributes>();
		this.outgoingAttrs = new HashMap<Location, TransitionAttributes>();
		this.clockBB = bbClock;
		this.initLoc = new Location("I");
		template.setInitialLocation(initLoc);
		this.endLoc = new Location("E");
		endLoc.setCommited();
		template.addLocation(endLoc);
		loopVars   = new HashMap<CFGNode, Integer>();
		loopBounds = new HashMap<CFGNode, Integer>();
	}
	public void addClock(String clock) {
		getTemplate().appendDeclaration(
			String.format("clock %s; ", clock));
	}
	public int addLoop(CFGNode hol, int nestingDepth, LoopBound lb) {
		
		ExecutionContext eCtx = new ExecutionContext(hol.getControlFlowGraph().getMethodInfo());
		nestingDepth = nestingDepth - 1;
		int varKey = loopBounds.size();
		getTemplate().appendDeclaration(
				String.format("const int %s = %d;", 
							  loopBoundConst(varKey), lb.getUpperBound(eCtx)));
		getTemplate().appendDeclaration(
				String.format("const int %s = %d;", 
							  loopLowerBoundConst(varKey), lb.getLowerBound(eCtx)));
		while(loopVarBounds.size() < nestingDepth) {
			loopVarBounds.add(0L);
		}
		if(loopVarBounds.size() <= nestingDepth) {
			loopVarBounds.add(lb.getUpperBound(eCtx).longValue());
		} else {
			if(lb.getUpperBound(eCtx).longValue() > loopVarBounds.get(nestingDepth)) {
				loopVarBounds.set(nestingDepth,lb.getUpperBound(eCtx).longValue());
			}
		}
		this.loopBounds.put(hol, varKey);
		this.loopVars.put(hol,nestingDepth);
		return varKey;
	}
	public String getLoopVar(CFGNode hol) {
		String s = loopVarName(this.loopVars.get(hol));
		if(s == null) throw new AssertionError("Loop not registered: "+hol);
		return s;
	}
	public String getLoopBoundConst(CFGNode hol) {
		String s = loopBoundConst(this.loopBounds.get(hol));
		if(s == null) throw new AssertionError("Loop not registered: "+hol);
		return s;
	}
	public String getLoopLowerBoundConst(CFGNode hol) {
		String s = loopLowerBoundConst(this.loopBounds.get(hol));
		if(s == null) throw new AssertionError("Loop not registered: "+hol);
		return s;
	}
	
	public Location getInitial() {
		return this.template.getInitial();
	}
	public Location getEnd() {
		return this.endLoc;
	}
	
	private Location createLocation(String name, LocationAttribute lAttr) {
		Location l = new Location(name,lAttr);
		this.template.addLocation(l);
		return l;		
	}
	public Location createLocation(String name) {
		return createLocation(name,LocationAttribute.none);
	}
	public Location createCommitedLocation(String name) {
		return createLocation(name,LocationAttribute.committed);
	}
	public Transition createTransition(Location src, Location target) {
		Transition t = new Transition(src,target);
		this.template.addTransition(t);
		return t;
	}
	public TransitionAttributes getOutgoingAttrs(Location l) {
		TransitionAttributes attrs = outgoingAttrs.get(l);
		if(attrs == null) {
			attrs = new TransitionAttributes();
			outgoingAttrs.put(l,attrs);
		}
		return attrs;
	}
	public TransitionAttributes getIncomingAttrs(Location l) {
		TransitionAttributes attrs = incomingAttrs.get(l);
		if(attrs == null) {
			attrs = new TransitionAttributes();
			incomingAttrs.put(l,attrs);
		}
		return attrs;
	}
	public void waitAtLocation(Location location, long waitTime) {
		location.setInvariant(String.format("%s <= %d", this.clockBB, waitTime));
		getIncomingAttrs(location).appendUpdate(clockBB + " := 0");
		/* Not necessary for WCET; according to measurements not beneficial either */
		// getOutgoingAttrs(location).appendGuard(String.format("%s == %d",clockBB,waitTime)); 
	}
	public String contLoopGuard(CFGNode loop) {
		return String.format("%s < %s",getLoopVar(loop),getLoopBoundConst(loop));
	}
	public String exitLoopGuard(CFGNode loop) {
		if(config.assumeTightBounds) {
			return String.format("%s == %s",getLoopVar(loop),getLoopBoundConst(loop));
		} else {
			return String.format("%s >= %s",getLoopVar(loop),getLoopLowerBoundConst(loop)); 
		}
	}
	public String resetLoopCounter(CFGNode loop) {
		return String.format("%s := 0",getLoopVar(loop));
	}		
	public String incrLoopCounter(CFGNode loop) {
		return String.format("%1$s := %1$s + 1",getLoopVar(loop));
	}
	public void addDescription(String string) {
		this.template.addComment(string);
	}
	public SubAutomaton getTemplateAutomaton() {
		return new SubAutomaton(getInitial(), getEnd());
	}
	/** add a final node */
	public void addPostEnd() {
		if(postEnd != null) throw new AssertionError("Called addPostEnd twice");
		postEnd = new Location("EE");
		template.addLocation(postEnd);
		template.addTransition(new Transition(getEnd(), postEnd));
	}
	/** add a synchronization loop from end to start */
	public void addSyncLoop() {
		Transition cont = new Transition(getEnd(), getInitial());
		String channel = SystemBuilder.methodChannel(pid);
		cont.getAttrs().setSync(channel+"!");
		if(loopBounds.size() > 0) {
			cont.getAttrs().appendUpdate("rst()");
		}
		template.addTransition(cont);
		getOutgoingAttrs(getInitial()).setSync(channel+"?");
	}
	/** create a subautomaton in the template */
	public SubAutomaton createSubAutomaton(String name) {
		Location subI = new Location("I_"+name);
		subI.setCommited();
		Location subE = new Location("E_"+name);
		subE.setCommited();
		template.addLocation(subI); template.addLocation(subE);
		return new SubAutomaton(subI,subE);
	}
	public Template getFinalTemplate() {
		if(outgoingAttrs == null) return this.template;
		for(int i = 0; i < loopVarBounds.size(); i++) {
			template.appendDeclaration(
					String.format("int[0,%s] %s;",loopVarBounds.get(i), loopVarName(i)));			
		}
		if(loopVarBounds.size() > 0) {
			StringBuilder rst = new StringBuilder();
			rst.append("void rst() {\n");
			for(int i = 0; i < loopVarBounds.size(); i++) {
				rst.append(String.format("  %s = 0;\n",loopVarName(i)));
			}
			rst.append("} \n");
			template.appendDeclaration(rst.toString());
		}

		for(Location l : template.getLocations()) {
			TransitionAttributes in = incomingAttrs.get(l);
			if(in != null) {
				for(Transition t : l.getPredecessors()) {
					t.getAttrs().addAttributes(in);
				}
			}
			TransitionAttributes out = outgoingAttrs.get(l);
			if(out != null) {
				for(Transition t : l.getSuccessors()) {
					t.getAttrs().addAttributes(out);
				}
			}
		}
		outgoingAttrs = incomingAttrs = null;
		/* debug: create dot file */
		if(config.debug) {
			File dbgFile = config.getOutFile("template_"+template.getId()+".dot");
			try {
				template.exportDOT(dbgFile);				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		new LayoutCFG(100,120).layoutCfgModel(template);
		return template;
	}
	/** Debug: dump loops */
	public void dumpLoops() {
		System.out.println("Loop Bounds");
		for(int i = 0; i < this.loopVarBounds.size(); i++) {
			System.out.println(String.format(" %s <= %d ",loopVarName(i),loopVarBounds.get(i)));
		}
		for(CFGNode hol : this.loopVars.keySet()) {
			System.out.println(String.format(" %s maps to variable %s with bound constant %s",
					hol.toString(),
					this.loopVars.get(hol).toString(),
					this.loopBounds.get(hol).toString()));
		}
		
	}
}
