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

import com.jopdesign.common.graphutils.AdvancedDOTExporter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents an UPAAL template.
 * The parameters L and T are the attribute types of locations and transitions, respectively.
 * See toXML() for the corresponding DTD elements.
 */
public class Template {
	private String name;
	protected List<String> parameters;
	protected List<String> declarations;
	protected Map<Integer,Location> locations;
	protected Map<String,Location> locationsByName;
	protected Location initial;
	protected List<Transition> transitions;
	private int idPool;
	private int idPoolT;
	
	public Template(String name, List<String> parameters) {
		this.name = name;
		this.declarations = new Vector<String>();
		this.parameters = parameters;
		this.locations = new HashMap<Integer,Location>();
		this.locationsByName = new HashMap<String,Location>();
		this.transitions = new Vector<Transition>();
		this.idPool = 1; this.idPoolT = 1;
	}
	
	public Collection<String> getDeclarations() {
		return declarations;
	}
	public Template appendDeclaration(String decl) {
		this.declarations.add(decl);
		return this;
	}

	public Location getInitial() {
		return initial;
	}

	public Collection<Location> getLocations() {
		return locations.values();
	}
	
	public void addLocation(Location l) {
		int id = generateLocationId();
		l.setTemplate(this, id);
		this.locations.put(id, l);
		this.locationsByName.put(l.getName(), l);
	}
	
	public void addTransition(Transition t) {
		int nextId = generateTransitionId();
		this.transitions.add(t);
		t.setTemplate(this,nextId);		
	}
	public String getId() {
		return this.name;
	}
	private int generateTransitionId() {
		int nextId = this.idPoolT;
		this.idPoolT+=1;
		return nextId;
	}
	private int generateLocationId() {
		int nextId = this.idPool;
		this.idPool+=1;
		return nextId;
	}
	/**
	 * Create a XML representation of the template.
	 * 
	 * <!ELEMENT template (name, parameter?, declaration?, location*, init?, transition*)>
	 * <!ELEMENT name (#PCDATA)> (coords)
	 * <!ELEMENT parameter (#PCDATA)> (coords)
	 * <!ELEMENT init EMPTY>
	 * <!ATTLIST init ref IDREF #IMPLIED>

	 * @param b the XmlBuilder (and its aggregated DOM model) to use
	 * @return the root element for the template
	 */
	public Element toXml(XmlBuilder b) {
		Element tElem = b.createElement("template");
		b.addElement(tElem,"name").setTextContent(this.name);
		b.addOptSourceTextElement(tElem, "parameter", 
			XmlBuilder.joinStrings(this.parameters, ", "));
		b.addOptSourceTextElement(tElem, "declaration", 
			XmlBuilder.joinStrings(this.declarations, "\n"));
		/* locations */
		for(Location l : this.locations.values()) {
			tElem.appendChild(l.toXml(b));
		}
		/* initial location */
		Element lInit = b.addElement(tElem,"init");
		lInit.setAttribute("ref", this.initial.getStringId());
		/* transitions */
		for(Transition t: this.transitions) {
			tElem.appendChild(t.toXml(b));
		}
		return tElem;
	}
	public List<Transition> getTransitions() {
		return transitions;
	}

	public void setInitialLocation(Location initLoc) {
		this.addLocation(initLoc);
		this.initial = initLoc;
	}
	
	@Override
	public String toString() {
		return "Template@" +this.name;
	}

	public void addComment(String comment) {
		this.declarations.add("/* "+comment+" */");		
	}

	public void exportDOT(File dbgFile) throws IOException {
		DirectedGraph<Location, DefaultEdge> locGraph =
			new DefaultDirectedGraph<Location, DefaultEdge>(DefaultEdge.class);
		for(Location l : this.locations.values()) locGraph.addVertex(l);
		Map<DefaultEdge,String> edgeMap = new HashMap<DefaultEdge,String>();
		for(Transition t: this.transitions) {
			DefaultEdge e = locGraph.addEdge(t.getSource(),t.getTarget());
			edgeMap.put(e, t.getAttrs().toString());
		}		
		FileWriter fw = new FileWriter(dbgFile);
		AdvancedDOTExporter.DOTNodeLabeller<Location> nodeLabeller = new AdvancedDOTExporter.DefaultNodeLabeller<Location>(){
			public int getID(Location node) { return node.getId(); }
			public String getLabel(Location node) { return node.getName(); }
		};
		AdvancedDOTExporter.DOTLabeller<DefaultEdge> edgeLabeller = new AdvancedDOTExporter.MapLabeller<DefaultEdge>(edgeMap);
		AdvancedDOTExporter<Location, DefaultEdge> dotExport =			
			new AdvancedDOTExporter<Location, DefaultEdge>(nodeLabeller,edgeLabeller );
		dotExport.exportDOT(fw, locGraph);
		fw.close();
	}

}
