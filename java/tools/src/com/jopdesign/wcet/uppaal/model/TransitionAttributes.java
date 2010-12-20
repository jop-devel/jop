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

import java.util.List;
import java.util.Vector;

/**
 * Attributes for transitions
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class TransitionAttributes {
	private String sync;	
	private List<String> guard;
	private List<String> select;
	private List<String> updates;
	public TransitionAttributes() {
		this.select = new Vector<String>();
		this.sync = new String();
		this.guard = new Vector<String>();
		this.updates = new Vector<String>();		
	}
	public TransitionAttributes appendGuard(String g) {
		this.guard.add(g);
		return this;
	}
	public TransitionAttributes appendUpdate(String src) {
		updates.add(src);
		return this;
	}
	public void addSelect(String s) {
		this.select.add(s);
	}
	public TransitionAttributes setSync(String s) {
		this.sync = s;
		return this;
	}
	public void addLabels(XmlBuilder b, Element tElem) {
		if(select.size() > 0) b.addLabel(tElem, "select", XmlBuilder.joinStrings(select, ", "));
		if(guard.size() > 0) b.addLabel(tElem, "guard", XmlBuilder.joinStrings(guard, " && "));
		if(sync.length() > 0) b.addLabel(tElem, "synchronisation", this.sync);
		if(updates.size() > 0) b.addLabel(tElem, "assignment", XmlBuilder.joinStrings(updates, ", "));		
	}
	public void addAttributes(TransitionAttributes other) {
		if(other.sync.length() > 0) {
			if(sync.length() > 0) throw new AssertionError("merging in attributes leads to duplicate sync");
			else this.sync= new String(other.sync);
		}
		this.select.addAll(other.select);
		this.guard.addAll(other.guard);
		this.updates.addAll(other.updates);
	}
}
