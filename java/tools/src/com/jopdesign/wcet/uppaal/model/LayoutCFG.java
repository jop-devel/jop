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
package com.jopdesign.wcet.uppaal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/* BROKEN */
public class LayoutCFG {
	public int x_step;
	public int y_step;
	
	private Map<Integer,LinkedList<Location>> nodesByYCoord;
	private Map<Location,Location> maxPred;
	private Template template;
	private Map<Location,Integer> orderNumber;
	public LayoutCFG(int x_step, int y_step) {
		this.x_step = x_step;
		this.y_step = y_step;
	}
	
	/**
	 * Compute a (heuristic) partial order on the locations of the automaton.
	 * Conceptually, we reduce the graph to a DAG, removing cycles depth first.
     *
	 * Then we assign order numbers to locations, s.t. ORD(l) = 1 + max ORD predecessors(l).
	 * 
	 * NOTE: This a heuristic for layouting only, as removing cycles isn't predictable.
	 *  Consider the complete graph K3, with initial node A:
     *
	 *    A <-> B <-> C
	 *    ^-----------^
	 *   
	 *  Removing cycles either gives:
	 *    A -> B -> C
	 *    |    |----^
	 *    ^---------^
	 *  or
	 *    A -> C -> B
	 *    |    |----^
	 *    ^---------^
     *
	 *  Now we either get: A < B < C or A < C < B, depending on the implementation.
	 * 
	 */
	public Map<Location,Integer> computeLocOrdering(Template t) {
		this.template = t;
		this.orderNumber = new TreeMap<Location,Integer>();
		computeOrdering();
		return this.orderNumber;
	}
	
	/* 'struct' for DFS */
	private class PathEntry {
		public PathEntry(Location l, Iterator<Transition> succIterator) {
			loc = l;
			cont = succIterator;
		}
		Location loc;
		Iterator<Transition> cont;
		public String toString() { return "PE_"+loc.toString(); }
	}
	/* compute partial order on locations */
	private void computeOrdering() {
		Set<Transition> cycleTransitions = new TreeSet<Transition>();

		/* Mark cycles using path-aware DFS */
		cycleTransitions = markCycles();		
		
		/* assign order numbers */
		Stack<Location> locStack = new Stack<Location>();

		locStack.push(template.getInitial());
		while(! locStack.empty()) {
			Location loc = locStack.pop();
			/* check if all predecessors have been processed, ignoring cycle transitions */
			List<Location> todoPreds = new Stack<Location>();
			int maxPred = -1;
			for(Transition tPred : loc.getPredecessors()) {
				if(cycleTransitions.contains(tPred)) continue;				
				if(orderNumber.containsKey(tPred.getSource())) {
					maxPred = Math.max(maxPred, orderNumber.get(tPred.getSource()));
				} else {
					todoPreds.add(tPred.getSource());
				}
			}
			/* If we're done, compute the order number, and push successors which do not yet
			 * have an order number */
			if(todoPreds.isEmpty()) {
				orderNumber.put(loc, maxPred + 1);
				for(Transition tSucc : loc.getSuccessors()) {
					if(cycleTransitions.contains(tSucc)) continue;				
					if(! orderNumber.containsKey(tSucc.getTarget())) {
						locStack.push(tSucc.getTarget());
					}
				}
			} 
			/* Otherwise, process the predecessors */
			else {
				locStack.addAll(todoPreds);
			}
		}
	}
	/* mark cycle transitions */
	private Set<Transition> markCycles() {
		Set<Transition> cycleTransitions = new HashSet<Transition>();
		/* A stack of locations */
		Stack<Location> locStack = new Stack<Location>();
		Stack<PathEntry> locPath = new Stack<PathEntry>();
		Set<Location> visitedLocs = new TreeSet<Location>();

		/* Initialize */
		locStack.add(template.getInitial());		

		while(! locStack.empty()) {
			Location l = locStack.pop();
			/* find an unvisited successor, remembering the iterator */
			visitedLocs.add(l);
			Location succLocation = null;			
			Iterator<Transition> succIterator = l.getSuccessors().iterator();
			while(succIterator.hasNext()) {
				Transition succTransition = succIterator.next(); 
				Location tmpLoc = succTransition.getTarget();
				if(containsLoc(tmpLoc,locPath)) {
					/* Cycle transition */
					cycleTransitions.add(succTransition);
				} else if(visitedLocs.contains(tmpLoc)) {
				} else {
					succLocation = tmpLoc;
					break;
				}
			}
			/* Check if there is a successor */
			if(succLocation != null) {
				locStack.push(succLocation);
				locPath.push(new PathEntry(l,succIterator));				
			} else {
				/* move back */
				while(! locPath.isEmpty() &&
					  ! locPath.peek().cont.hasNext()) {
					locPath.pop(); /* remove processed entry from locPath */
				}
				if(! locPath.isEmpty() &&
				     locPath.peek().cont.hasNext())  {
					locStack.push(locPath.peek().cont.next().getTarget());
				}
			}
		}
		return cycleTransitions;
	}
	/* check if a locations occurs in a path */
	private static boolean containsLoc(Location l, Collection<PathEntry> path) {
		for(PathEntry e : path) {
			if(l.equals(e.loc)) return true;
		}
		return false;
	}
	/* Comparator for order numbers */
	private final Comparator<Transition> compareSourceByOrder = 
		new Comparator<Transition>() {
			public int compare(Transition l1, Transition l2) {
				Integer n1 = orderNumber.get(l1.getSource());
				Integer n2 = orderNumber.get(l2.getSource());
				return n1.compareTo(n2);
			}		
	};
	/**
	 * Simple layout for hierarchical templates with cycles.
	 * Writing a good layout algorithm is very hard, so we chose to take
	 * a simple, yet usable variant.
	 * 
	 * The strategy is a three-pass layout algorithm on a graph G
	 * 1st pass: Y-Layout
	 *   - Locations are partially ordered (see computeLocOrdering)
	 *   - The y-coordinate corresponds to the order number
	 * 2nd pass: X-Layout
	 *   - The node with the smallest y-coordinate is layouted first.
	 *   - If more than one node is on some y-coordinate, we sort the nodes
	 *     by the x-coordinate of their predecessors, and then stack them horizontally
	 *   - TODO (low priority) : compact X-Layout
	 * Before the 3rd pass, convert the pseudo into screen coordinates
	 * 3rd pass: Transitions
	 *   - If a transition is a forward ref or a backlink, add two nails and slightly displace
	 *     the transition.
	 * @param template The automaton template to layout
	 */
	public void layoutCfgModel(Template template) {
		this.template = template;
		this.nodesByYCoord = new TreeMap<Integer, LinkedList<Location>>();
		this.maxPred = new TreeMap<Location, Location>();

		/* Firt pass */
		this.orderNumber = new TreeMap<Location,Integer>();
		this.computeOrdering();
		
		/* y - coord ~ order number */
		for(Location l : template.getLocations()) {
			setYCoord(l, orderNumber.get(l));
		}		

		/* compute lowest predecessors */
		for(Location l : template.getLocations()) {
			if(! l.getPredecessors().isEmpty()) {
				maxPred.put(l,Collections.max(l.getPredecessors(),compareSourceByOrder).getSource());
			}
		}

		/* 2nd pass */
		displaceLocations();
		
		/* Screen Coordinates  */
		for(Location l: template.getLocations()) {
			l.setPos(l.getCoords().x * x_step, l.getCoords().y * y_step);
		}
		/* 3rd pass */		
		transitionLayout();		
//		for(Location l: template.getLocations()) {
//			System.err.println("Loc: "+l);			
//			System.err.println("Pos: "+l.getCoords());
//		}
	}
	/* set and record the y-coordinate of a location. */
	private void setYCoord(Location loc, int y) {
		loc.setPos(0, y);
		if(! this.nodesByYCoord.containsKey(y)) {
			LinkedList<Location> entries = new LinkedList<Location>();
			entries.add(loc);
			this.nodesByYCoord.put(y, entries);
		} else {
			this.nodesByYCoord.get(y).add(loc);
		}
	}

	/* Compare to locations by the x-coord of their lowest predecessor */
	private final Comparator<Location> compareByLowestPredsXCoord =
	 new Comparator<Location>() {
		public int compare(Location l1, Location l2) {
			Location p1 = maxPred.get(l1);
			Location p2 = maxPred.get(l2);
			return compareInt(p1.getCoords().x, p2.getCoords().x);
		}
	};
	

	/*
	 * Traverse the locations by y-coordinate.
	 * Sort locations sharing the same y-coord in order of the x-coord of their max y-coord
	 * predecessors.
	 * Stack the locations vertically in this order.
	 */
	private void displaceLocations() {
		for(List<Location> locs : this.nodesByYCoord.values()) {
			if(locs.get(0).getCoords().y == 0) {
				locs.get(0).setPos(0,locs.get(0).getCoords().y);
				continue;
			}
			Collections.sort(locs, compareByLowestPredsXCoord);
			int minX = 0;
			for(Location l : locs) {
				int predX = maxPred.get(l).getCoords().x;
				int xCoord = Math.max(minX, predX);
				l.setPos(xCoord,l.getCoords().y);
				minX = xCoord+1;
			}
		}
	}

	private void transitionLayout() {
		for(Transition t: template.getTransitions()) {
			Location src = t.getSource();
			Location target = t.getTarget();
			if(target.getCoords().y - src.getCoords().y != y_step) {
				int displace_y = y_step / 2;
				if(src.getCoords().y > target.getCoords().y) displace_y = -displace_y;
				int displace_x = x_step / 3;
				int displace_x_src, displace_x_target;
				if(src.getCoords().x == target.getCoords().x) {
					displace_x_src = -displace_x;
					displace_x_target = -displace_x;
				} else if(src.getCoords().x > target.getCoords().x) {
					displace_x_src = -displace_x;
					displace_x_target = displace_x;
				} else {					
					displace_x_src = displace_x;
					displace_x_target = -displace_x;
				}
				t.addNail(src.getCoords().x+displace_x_src,src.getCoords().y+displace_y);
				t.addNail(target.getCoords().x+displace_x_target,target.getCoords().y-displace_y);
			}
		}
	}

	public static int compareInt(int i1, int i2) {
		return new Integer(i1).compareTo(i2);
	}
}
