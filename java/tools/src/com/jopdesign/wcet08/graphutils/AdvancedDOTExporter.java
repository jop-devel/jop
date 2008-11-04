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
package com.jopdesign.wcet08.graphutils;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

public class AdvancedDOTExporter<V,E> {
	public static String escapedToString(String s) {
		StringBuffer sb = new StringBuffer();
		boolean quote=false;
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch(c) {
			case '\n' : sb.append("\\n"); break;
			case '\r' : break;
			case '\t' : sb.append("\\t"); break;
			case '"'  : sb.append("\\\""); break;
			default: sb.append(c);break;
			}
			if(! Character.isLetterOrDigit(c)) quote=true;
		}
		if(quote || sb.length() == 0) { sb.insert(0, '"');sb.append('"'); }
		return sb.toString();
	}
	public interface DOTLabeller<T> {
		public String getLabel(T object);
		public boolean setAttributes(T object, Map<String,String> ht);
	}
	public interface DOTNodeLabeller<T> extends DOTLabeller<T> {
		public int getID(T node);
	}
	public static class NullLabeller<T> implements DOTLabeller<T> {
		public String getLabel(T obj) { return null; }
		public boolean setAttributes(T object, Map<String, String> ht) {
			return false;
		}		
	}
	public static class DefaultDOTLabeller<T> implements DOTLabeller<T> {
		public String getLabel(T node) {
			return node.toString();
		}
		public boolean setAttributes(T node, Map<String, String> ht) {
			String label = getLabel(node);
			if(label != null) ht.put("label", label);
			return (node != null);
		}				
	}
	public static class MapLabeller<T> extends DefaultDOTLabeller<T> {
		protected Map<T, ?> annots;
		public MapLabeller(Map<T, ?> annots) {
			this.annots = annots;
		}
		public String getLabel(T obj) {
			if(annots.containsKey(obj)) {
				return annots.get(obj).toString();
			} else {
				return obj.toString();
			}
		}
	}
	public static class DefaultNodeLabeller<T> 
			extends    DefaultDOTLabeller<T>
			implements DOTNodeLabeller<T> {		
		private Hashtable<T,Integer> nodeId = new Hashtable<T, Integer>();
		public int getID(T node) {
			Integer id= nodeId.get(node);
			if(id == null) {
				id = nodeId.size();
				nodeId.put(node, id);
			}
			return id;
		}
	}

	public static Map<String,String> defaultNodeAttributes() {
		Hashtable<String, String> attrs = new Hashtable<String,String>();
		attrs.put("shape", "box");
		attrs.put("fontname", "Courier");
		attrs.put("fontsize", "10");
		return attrs;
	}
	public static Map<String,String> defaultEdgeAttributes() {
		Hashtable<String, String> attrs = new Hashtable<String,String>();
		attrs.put("fontname", "Courier");
		attrs.put("fontsize", "10");
		return attrs;		
	}

	private DOTNodeLabeller<V> nodeLabeller;
	private DOTLabeller<E> edgeLabeller;
	private Map<String, String> nodeAttributes;
	private Map<String, String> edgeAttributes;
	public AdvancedDOTExporter() {
		this.nodeLabeller = new DefaultNodeLabeller<V>();
		this.edgeLabeller = new NullLabeller<E>();
		this.nodeAttributes = defaultNodeAttributes();
		this.edgeAttributes = defaultEdgeAttributes();
	}
	public AdvancedDOTExporter(DOTNodeLabeller<V> nodeLabeller,
							   DOTLabeller<E> edgeLabeller) {
		this.nodeLabeller = nodeLabeller == null ? new DefaultNodeLabeller<V>() : nodeLabeller;
		this.edgeLabeller = edgeLabeller == null ? new NullLabeller<E>() : edgeLabeller;
		this.nodeAttributes = defaultNodeAttributes();
		this.edgeAttributes = defaultEdgeAttributes();
	}
	public void exportDOT(Writer writer, DirectedGraph<V,E> g) throws IOException {
		exportDOTDiGraph(writer,g);
	}
	public void exportDOTDiGraph(Writer w, Graph<V,E> graph) throws IOException {
		w.append("digraph cfg\n{\n");
		for(V n : graph.vertexSet()) {
			int id = nodeLabeller.getID(n);
			Hashtable<String,String> attrs = new Hashtable<String,String>(this.nodeAttributes);
			nodeLabeller.setAttributes(n, attrs);

			w.append(""+id+" ");
			appendAttributes(w,attrs);
			w.append(";\n");
		}
		for(E e : graph.edgeSet()) {
			int idSrc = nodeLabeller.getID(graph.getEdgeSource(e));
			int idTarget = nodeLabeller.getID(graph.getEdgeTarget(e));
			Hashtable<String,String> attrs = new Hashtable<String,String>(this.edgeAttributes);
			edgeLabeller.setAttributes(e, attrs);

			w.append(""+idSrc+" -> "+idTarget);
			appendAttributes(w,attrs);
			w.append(";\n");
		}
		w.append("}\n");
	}
	private void appendAttributes(Writer w, Hashtable<String, String> attrs) throws IOException {
		if(attrs.isEmpty()) return;
		w.append('[');
		boolean first = true;
		for(Entry<String,String> e  : attrs.entrySet()) {
			if(first) first = false;
			else      w.append(',');
			w.append(e.getKey());
			w.append('=');
			w.append(escapedToString(e.getValue()));
		}
		w.append(']');
	}
}
