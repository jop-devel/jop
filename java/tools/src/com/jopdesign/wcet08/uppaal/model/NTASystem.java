package com.jopdesign.wcet08.uppaal.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class represents an UPPAAL System.
 * 
 * Source Code is modelled using the Strings - this can be refined as needed.
 *
 * @see NTASystem.toXml() for the corresponding DTD elements. 
 */
public class NTASystem {
	private String name;
	private String imports;
	private List<String> declarations;
	private String instantitations;
	private String system;
	private Map<String,Template> templates;
	public NTASystem(String name) {
		this.name = name;
		this.imports = new String();
		this.declarations = new Vector<String>();
		this.instantitations = new String();		
		this.system = new String();
		this.templates = new HashMap<String,Template>();
	}
	public Collection<String> getDeclarations() {
		return declarations;
	}
	
	public String getImports() {
		return imports;
	}
	public String getName() {
		return name;
	}
	public void setSystem(String sys) {
		this.system = sys;
	}
	public void addTemplate(Template t) throws DuplicateKeyException {
		if(this.templates.containsKey(t.getId())) {
			throw new DuplicateKeyException("Duplicate template");
		}
		this.templates.put(t.getId(), t);
	}
	public void appendDeclaration(String decl) {
		this.declarations.add(decl);
	}
	/**
	 * Export the system to XML
	 * @throws XmlSerializationException 
	 */
	public Document toXML() throws XmlSerializationException {
		Document dom= XmlBuilder.createDom();
		Element rootEl = this.toXML(dom);
		dom.appendChild(rootEl);
		return dom;
	}
	/**
	 * Export the system to the given DOM object.
	 * 
	 * <!ELEMENT nta (imports?, declaration?, template+, instantiation?, system)>
	 * <!ELEMENT imports (#PCDATA)>
	 * <!ELEMENT declaration (#PCDATA)>
	 * <!ELEMENT instantiation (#PCDATA)>
	 * <!ELEMENT system (#PCDATA)>
	 * @param dom The XML document model
	 * @return the nta element of the DOM tree
	 */
	public Element toXML(Document dom) {
		XmlBuilder b = new XmlBuilder(dom);
		Element rootEl = b.createElement("nta");
		// imports,declarations, instantiations, system
		b.addOptSourceTextElement(rootEl,"imports",imports);
		b.addOptSourceTextElement(rootEl,"declaration", XmlBuilder.joinStrings(declarations,"\n"));
		for(Template t : this.templates.values()) {
			rootEl.appendChild(t.toXml(b));
		}
		
		b.addOptSourceTextElement(rootEl,"instantiation",this.instantitations);
		b.addOptSourceTextElement(rootEl,"system",this.system);

		return rootEl;
	}
}
