package com.jopdesign.wcet08.uppaal.model;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;

/**
 * Attributes for transitions
 * 
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 *
 */
public class TransitionAttributes {
	private String select;
	private String sync;	
	private List<String> guard;
	private List<String> updates;
	public TransitionAttributes() {
		this.select = new String();
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
	public void setSelect(String s) {
		this.select = s;
	}
	public TransitionAttributes setSync(String s) {
		this.sync = s;
		return this;
	}
	public void addLabels(XmlBuilder b, Element tElem) {
		if(select.length() > 0) b.addLabel(tElem, "select", this.select);
		if(guard.size() > 0) b.addLabel(tElem, "guard", XmlBuilder.joinStrings(guard, " && "));
		if(sync.length() > 0) b.addLabel(tElem, "synchronisation", this.sync);
		if(updates.size() > 0) b.addLabel(tElem, "assignment", XmlBuilder.joinStrings(updates, ", "));		
	}
	public void addAttributes(TransitionAttributes other) {
		if(other.select.length() > 0) {
			if(select.length() > 0) throw new AssertionError("merging in attributes leads to duplicate select");
			else this.select = new String(other.select);
		}
		if(other.sync.length() > 0) {
			if(sync.length() > 0) throw new AssertionError("merging in attributes leads to duplicate sync");
			else this.sync= new String(other.sync);
		}
		this.guard.addAll(other.guard);
		this.updates.addAll(other.updates);
	}
}
