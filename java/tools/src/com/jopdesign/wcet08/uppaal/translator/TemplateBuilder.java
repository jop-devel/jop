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
package com.jopdesign.wcet08.uppaal.translator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.uppaal.UppAalConfig;
import com.jopdesign.wcet08.uppaal.model.Location;
import com.jopdesign.wcet08.uppaal.model.Template;
import com.jopdesign.wcet08.uppaal.model.Transition;
import com.jopdesign.wcet08.uppaal.model.TransitionAttributes;

/**
 * Builder for templates
 */
public class TemplateBuilder {
	public static final String LOCAL_CLOCK = "t_local";
	public static final String LOCAL_CALL_STACK_DEPTH = "csd_local";
	public static String loopVar(int loopId) {
		return("loop_cnt_"+loopId);
	}
	public static String loopBoundConst (int loopId) {
		return("LOOP_BOUND_"+loopId);
	}
	public static String loopLowerBoundConst(int loopId) {
		return("LOOP_LOWERBOUND_"+loopId);
	}

	private Template template;
	private Template getTemplate() { return this.template; }
	
	private Map<CFGNode, Integer>   loopIds;
	private Vector<LoopBound> loopBounds;
	private Config config;
	private Map<Location, TransitionAttributes> outgoingAttrs;
	private Map<Location, TransitionAttributes> incomingAttrs;

	/**
	 * Build a fresh template representing a CFG.
	 * The local clock 'local' is declared, and a commited initial state is created.
	 * For each loop, variables loop_i and bound_i are created.
	 * Two locations end and post-end are created.
	 * @param name the template's name
	 * @param methodNumber the method's unique number
	 * @param map the bound of each loop in the template
	 * @param isRootMethod whether this is the `measure' entry point
	 */
	public TemplateBuilder(String name, int methodNumber,
						   Map<CFGNode, LoopBound> map) {
		this.config = Config.instance();
		Vector<String> parameters = new Vector<String>();
		this.template = new Template(name,parameters);	

		this.loopIds = new HashMap<CFGNode, Integer>();
		this.loopBounds = new Vector<LoopBound>();
		int id = 0;
		for(Entry<CFGNode,LoopBound> entry : map.entrySet()) {
			this.loopIds.put(entry.getKey(), id);
			this.loopBounds.add(entry.getValue());
			id = id + 1;
		}
		this.incomingAttrs = new HashMap<Location, TransitionAttributes>();
		this.outgoingAttrs = new HashMap<Location, TransitionAttributes>();
		initializeTemplate();
	}
	
	/**
	 * Declarations: 
	 *   - Create a local clock {@code t_local}. 
	 *   - Create variables for loop counters,and constants for loop bounds
	 */
	private void initializeTemplate() {
		getTemplate().appendDeclaration(
				String.format("clock %s; ", LOCAL_CLOCK));
		if(! config.getOption(UppAalConfig.UPPAAL_ONE_CHANNEL_PER_METHOD)) {
			getTemplate().appendDeclaration(
					String.format("int[0,%s] %s;", 
							SystemBuilder.MAX_CALL_STACK_DEPTH, 
							LOCAL_CALL_STACK_DEPTH));
		}
		for(int i = 0; i < loopBounds.size(); i++ ) {
			getTemplate().appendDeclaration(
					String.format("const int %s = %d;", 
								  loopBoundConst(i), loopBounds.get(i).getUpperBound()));
			getTemplate().appendDeclaration(
					String.format("const int %s = %d;", 
								  loopLowerBoundConst(i), loopBounds.get(i).getLowerBound()));
			getTemplate().appendDeclaration(
					String.format("int[0,%s] %s;", 
								  loopBoundConst(i), loopVar(i)));
		}		
		Location initLoc = new Location("I");
		template.setInitialLocation(initLoc);
	}
	public Integer getLoopId(CFGNode hol) {
		return this.loopIds.get(hol);
	}
	public Location getInitial() {
		return this.template.getInitial();
	}
	public Location createLocation(String name) {
		Location l = new Location(name);
		this.template.addLocation(l);
		return l;
	}
	public Transition createTransition(Location src, Location target) {
		Transition t = new Transition(src,target);
		this.template.addTransition(t);
		return t;
	}
	public Template getFinalTemplate() {
		if(outgoingAttrs == null) return this.template;
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
		return template;
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
		location.setInvariant(String.format("%s <= %d", 
				TemplateBuilder.LOCAL_CLOCK, 
				waitTime));
		getIncomingAttrs(location).appendUpdate(TemplateBuilder.LOCAL_CLOCK + " := 0");
		getOutgoingAttrs(location).appendGuard(
			String.format("%s >= %d", 
			TemplateBuilder.LOCAL_CLOCK, 
			waitTime));
	}
	public String contLoopGuard(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return String.format("%s < %s",
									loopVar(id), loopBoundConst(id));
	}
	public String exitLoopGuard(CFGNode loop) {
		int id = this.loopIds.get(loop);
		if(config.getOption(UppAalConfig.UPPAAL_TIGHT_BOUNDS)) {
			return String.format("%s == %s",loopVar(id),loopBoundConst(id));
		}
		else {
			return String.format("%s >= %s",loopVar(id),loopLowerBoundConst(id)); 
		}
	}
	public String resetLoopCounter(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return String.format("%s := 0",loopVar(id));
	}		
	public String incrLoopCounter(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return String.format("%1$s := %1$s + 1",loopVar(id));
	}
	public void addDescription(String string) {
		this.template.addComment(string);
	}
	public void onHit(Transition trans) {
		trans.getAttrs().appendGuard("lastHit");
	}
	public void onMiss(Transition trans) {
		trans.getAttrs().appendGuard("! lastHit");
	}
}
