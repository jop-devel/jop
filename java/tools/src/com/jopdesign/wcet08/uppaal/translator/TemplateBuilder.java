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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.jopdesign.wcet08.Config;
import com.jopdesign.wcet08.frontend.ControlFlowGraph.CFGNode;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.uppaal.model.Location;
import com.jopdesign.wcet08.uppaal.model.Template;
import com.jopdesign.wcet08.uppaal.model.Transition;

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
	public Template getTemplate() { return this.template; }
	
	private Map<CFGNode, Integer>   loopIds;
	private Vector<LoopBound> loopBounds;
	private int methodNum;

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
	public TemplateBuilder(String name, int methodNumber, Map<CFGNode, LoopBound> map, boolean isRootMethod) {
		this.methodNum = methodNumber;
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

		initializeTemplate();
	}
	
	/**
	 * Declarations: 
	 *   - Create a local clock {@code t_local}. 
	 *   - Create variables for loop counters,and constants for loop bounds
	 */
	private void initializeTemplate() {
		getTemplate().appendDeclaration(
				MessageFormat.format("clock {0}; ", LOCAL_CLOCK));
		getTemplate().appendDeclaration(
				MessageFormat.format("int[0,{0}] {1};", 
						SystemBuilder.MAX_CALL_STACK_DEPTH, 
						LOCAL_CALL_STACK_DEPTH));
		for(int i = 0; i < loopBounds.size(); i++ ) {
			getTemplate().appendDeclaration(
					MessageFormat.format("const int {0} = {1};", 
										  loopBoundConst(i), loopBounds.get(i).getUpperBound()));
			getTemplate().appendDeclaration(
					MessageFormat.format("const int {0} = {1};", 
										  loopLowerBoundConst(i), loopBounds.get(i).getLowerBound()));
			getTemplate().appendDeclaration(
					MessageFormat.format("int[0,{0}] {1};", 
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
	public String contLoopGuard(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return MessageFormat.format("{0} < {1}",
									loopVar(id), loopBoundConst(id));
	}
	public String exitLoopGuard(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return MessageFormat.format("{0} >= {1}",
									loopVar(id), 
									(new UppAalConfig(Config.instance()).assumeTightBounds())
									? loopBoundConst(id)
									: loopLowerBoundConst(id));
	}
	public String resetLoopCounter(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return MessageFormat.format("{0} := 0",
									loopVar(id), loopBoundConst(id));
	}		
	public String incrLoopCounter(CFGNode loop) {
		int id = this.loopIds.get(loop);
		return MessageFormat.format("{0} := {0} + 1",
									loopVar(id), loopBoundConst(id));
	}
	public String initLocalCallStackDepth() {
		return MessageFormat.format("{0} := {1}", 
									LOCAL_CALL_STACK_DEPTH, 
									SystemBuilder.CURRENT_CALL_STACK_DEPTH);
	}
}
