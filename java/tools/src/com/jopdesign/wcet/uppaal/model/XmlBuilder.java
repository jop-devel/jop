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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class XmlBuilder {
	private Document dom;

	/**
	 * Create a DOM object.
	 * (guideline: http://totheriver.com/learn/xml/xmltutorial.html)
	 * 
	 * @return An XML Document (DOM)
	 * @throws XmlSerializationException 
	 */
	public static Document createDom() throws XmlSerializationException {
		Document dom;

		//get an instance of factory

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
	
			//create an instance of DOM
			dom = db.newDocument();
	
		} catch(ParserConfigurationException pce) {
			throw new XmlSerializationException
				("Error while trying to instantiate DocumentBuilder",pce);
		}
		return dom;
	}
	
	public XmlBuilder(Document dom) {
		this.dom = dom;
	}
	
	public Document getDocument() {
		return this.dom;
	}
	/**
	 * Create a DOM element
	 * @param tagName
	 */
	public Element createElement(String tagName) {
		return this.dom.createElement(tagName);
	}

	public Element addElement(Element elem, String tagName) {
		Element newElem = createElement(tagName);
		elem.appendChild(newElem);
		return newElem;
	}
	/**
	 * Add a label (if non-empty) to the given location / transition
	 * @param elem   The element to attach the label to
	 * @param kind The kind (invariant,guard,etc.) of the label
	 * @param labelText  The text of the label
	 * 
	 */
	public void addLabel(Element elem, String kind, String labelText) {
		if(labelText == null || labelText.length() == 0) return;
		Element label = this.addElement(elem, "label");
		label.setAttribute("kind", kind);
		label.setTextContent(labelText);
	}
	
	/**
	 * 
	 * @param parent
	 * @param tag
	 * @param code
	 */
	public void addOptSourceTextElement(Element parent, String tag, String code) {
		String c = code.trim();
		if(c.length() == 0) return;
		Element cEl = dom.createElement(tag);
		cEl.setTextContent(code);
		parent.appendChild(cEl);
	}
	
	/**
	 * 
	 * @param dom
	 * @return
	 * @throws XmlSerializationException
	 */
	public static String domToString(Document dom) throws XmlSerializationException {
		StringWriter s = new StringWriter();
		writeDom(dom, s);
		return s.getBuffer().toString();
	}

	/**
	 * @param dom
	 * @param s
	 * @throws TransformerFactoryConfigurationError
	 * @throws XmlSerializationException
	 */
	public static void writeDom(Document dom, Writer s) 
		throws TransformerFactoryConfigurationError, XmlSerializationException {
		
		DOMSource domSource = new DOMSource(dom);
		StreamResult streamResult = new StreamResult(s);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
					"http://www.it.uu.se/research/group/darts/uppaal/flat-1_1.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.transform(domSource, streamResult); 
		} catch (Exception e) {
			throw new XmlSerializationException("Error in domToString()",e);
		}
	}
	/**
	 * 
	 * @param strs A collection of strings
	 * @param sep A seperator string
	 * @return The concatenated sequence of the given strings, interspersed with the seperator.
	 */
	public static String joinStrings(Iterable<String> strs, String sep) {
		StringBuilder b = new StringBuilder("");

		Iterator<String> i = strs.iterator();
		if(! i.hasNext()) return "";
		b.append(i.next());
		while(i.hasNext()) { b.append(sep);b.append(i.next()); }
		return b.toString();
	}

}
