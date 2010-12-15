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
package com.jopdesign.wcet.uppaal.model;

import java.awt.Point;
import java.util.LinkedList;
import org.w3c.dom.Element;

/**
 * <p>This class represents a transition in an UPPAAL template</p>
 *
 * <p>Transitions support the following labels:
 * <ul>
 * <li/> "select": Introduction of non-deterministic local variables (like a local random number generator)
 *  <pre>Example: i : int[0,3], j: int[0,1]</pre> 
 * <li/> "guard": A boolean expression, a conjunction of simple conditions on a clock, differences between clocks, and boolean 
 *          expressions not involving clocks. (So clock expressions are restricted to difference arithmetic)
 *          The transition can only be used if the guard is true in the current state. 
 *  <pre>Example: (x == 0 || y == 1) && clock > 3 && a[i] == a[j]</pre>
 * <li/>"sync": Synchronization (see UPPAAL help)
 *  <pre>Exammple: i?, i!</pre>
 * <li/> "assignment": Comma seperated list of statements with side-effects. The updates on the m? side are evaluated before the
 * updates on the m! side, when using channel synchronization.
 *  <pre>Example: x:=0, y:=1, call_fun()</pre>
 *  </ul>
 *
 * @see toXML() for the corresponding DTD elements.
 */
public class Transition {
	private int id;
	private Location source;
	private Location target;
	private Template template;
	private TransitionAttributes attrs;
	private VisualAttribute visual = null;
	private LinkedList<Point> nails;

	/* package visibility. Add transitions via template */
	public Transition(Location src, Location target) {
		this.template = null;
		this.source = src;		
		this.target = target;
		src.addSuccessor(this);
		target.addPredecessor(this);
		this.attrs = new TransitionAttributes();
		this.visual = new VisualAttribute();
		this.nails = new LinkedList<Point>();
	}
	public TransitionAttributes getAttrs() {
		return this.attrs;
	}
	public int getId() {
		if(this.template == null) {
			return -1;
		} else {
			return id;			
		}
	}
	public String getStringId() {
		return "trans_"+this.id;
	}
	public Location getSource() {
		return source;
	}
	public Location getTarget() {
		return target;
	}
	public void setTemplate(Template template, int id) {
		this.id = id;
		this.template = template;
	}
	/**
	 * Create a XML representation of this transition.
	 *  
	 * @param b The Builder/DOM to use
	 * @return The DOM element representing this transition
	 *
	 * The corresponding DTD elements are:
	 * 
	 * <!ELEMENT transition (source, target, label*, nail*)>
	 * <!ATTLIST transition id  ID #IMPLIED > (+ coords, color)
	 * <!ELEMENT source EMPTY>
	 * <!ATTLIST source ref IDREF #REQUIRED>
	 * <!ELEMENT target EMPTY>
	 * <!ATTLIST target ref IDREF #REQUIRED>
	 *
	 * On the GUI side, transitions may have nails, i.e. control points for drawing a spline. 
	 * <!ELEMENT nail EMPTY> (+ coords)
	 * <!ELEMENT label (#PCDATA)>
	 * <!ATTLIST label	kind CDATA #REQUIRED> (+coords)
	 *
	 * 	The note (coords) means that the element has 2 attributes x and y, 
	 *  which are used in the GUI to display the template.
	 * 
	 */
	public Element toXml(XmlBuilder b) {
		Element tElem = b.createElement("transition");
		tElem.setAttribute("id", getStringId());
		if(this.visual != null) { visual.setXmlAttributes(tElem); }
		b.addElement(tElem, "source").setAttribute("ref", this.source.getStringId());		
		b.addElement(tElem, "target").setAttribute("ref", this.target.getStringId());
		/* labels */
		this.attrs.addLabels(b,tElem);
		for(Point p : this.nails) {
			Element nail = b.addElement(tElem, "nail");
			nail.setAttribute("x", ""+p.x);
			nail.setAttribute("y", ""+p.y);
		}
		return tElem;
	}
	
	/* Visual stuff: Crosscutting, but for simplicity we'll keep it here */
	public void addNail(int x, int y) {
		this.nails.addLast(new Point(x,y));
	}
	public LinkedList<Point> getNails() {
		return this.nails;
	}
	
}	
