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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a location of an UPPAAL template.
 * Supported labels:
 *
 * kind="invariant"
 * Locations may be labelled with an invariant, which forces the system 
 * to leave the state before the invariant becomes false.
 */
public class Location implements Comparable<Location> {
	public enum LocationAttribute { none, urgent, committed };
	
	private int id;
	private String name;
	private String invariant;
	private LocationAttribute attr;
	private VisualAttribute visual = null;

	// Dependent attributes
	private List<Transition> successors;
	protected List<Transition> predecessors;
	private Template template;
	
	public Location(String name, LocationAttribute attr) {
		this.template = null;
		this.name = name;
		this.attr= attr;
		this.invariant = "";
		this.successors = new LinkedList<Transition>();
		this.predecessors = new LinkedList<Transition>();
		this.visual = new VisualAttribute();
	}
	
	public Location(String name) {
		this(name, LocationAttribute.none);
	}

	public boolean hasInvariant() {
		return this.invariant.length() > 0;
	}
	
	public int getId() {
		if(this.template == null) {
			return (-1);
		} else {
			return id;
		}
	}
	public String getStringId() {
		return "loc_"+this.getId();
	}
	
	public String getName() {
		return name;
	}
	public boolean isUrgent() {
		return this.attr == LocationAttribute.urgent;
	}
	public boolean isCommitted() {
		return this.attr == LocationAttribute.committed;
	}

	/**
	 * Get the transitions from predecessors to this node.
	 * Note that Java won't allow to refine the type in a subclass of Location,
	 * so casting is unavoidable here.
	 * @return
	 */
	public List<Transition> getPredecessors() {
		return this.predecessors;
	}
	public List<Transition> getSuccessors() {
		return this.successors;
	}
	
	public void setInvariant(String inv) {
		this.invariant = inv;
	}
	public void setTemplate(Template template, int id) {
		this.template = template;
		this.id = id;
	}
	public void setUrgent() {
		this.attr = LocationAttribute.urgent;
	}	
	public void setCommited() {
		this.attr = LocationAttribute.committed;
	}
	
	/**
	 * The corresponding DTD elements are
	 * <!ELEMENT location (name?, label*, urgent?, committed?)>
	 * <!ATTLIST location id ID #REQUIRED > (+coords,color)
	 * <!ELEMENT urgent EMPTY>
	 * <!ELEMENT committed EMPTY>
	 * <!ELEMENT label (#PCDATA)>
	 * <!ATTLIST label	kind CDATA #REQUIRED> (+coords)
	 * 	The note (coords) means that the element has 2 attributes x and y, 
	 *  which are used in the GUI to display the template.
	 *
	 * @param b
	 * @return
	 */
	public Node toXml(XmlBuilder b) {
		Element lElem = b.createElement("location");
		lElem.setAttribute("id", this.getStringId());
		if(this.visual != null) visual.setXmlAttributes(lElem);
		b.addElement(lElem, "name").setTextContent(this.getName());
		switch(this.attr) {
			case none: break;
			case urgent: b.addElement(lElem,"urgent");break;
			case committed: b.addElement(lElem,"committed");break;
		}
		if(this.hasInvariant()) b.addLabel(lElem,"invariant",this.invariant);
		return lElem;
	}
	
	public int compareTo(Location o) {
		if(this == o) return 0;
		else if(this.id < o.id) return -1;
		else if(this.id > o.id) return 1;
		else return 0;
	}
	
	
	/* Visual stuff */
	public void setColor(Color c) {
		this.visual.setColor(c);
	}
	public void setPos(int x, int y) {
		this.visual.setPos(x,y);
	}
	public Point getCoords() {
		return this.visual.getCoords();
	}
	public boolean hasCoords() {
		return this.visual.hasCoords();
	}
	
	/* Add succ / pred */
	void addSuccessor(Transition t) {
		this.successors.add(t);
	}
	void addPredecessor(Transition t) {
		this.predecessors.add(t);
	}	
	
	public String toString() {
		return "Loc@"+this.getStringId();
	}

}
