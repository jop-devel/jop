/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
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
package com.jopdesign.common.graphutils;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Export graphs to .DOT, with custom attributes. Supports JGraphT graphs via {@code JGraphTAdapter}
 *
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @param <V> node type
 * @param <E> edge type
 */
public class AdvancedDOTExporter<V, E> {

	public interface GraphAdapter<V,E> {
		public V getEdgeSource(E e);
		public V getEdgeTarget(E e);
	}
	
	public static class JGraphTAdapter<V,E> implements GraphAdapter<V,E> {
		private Graph<V, E> backing;

		public JGraphTAdapter(Graph<V,E> g) {
			this.backing = g;			
		}

		@Override
		public V getEdgeSource(E e) {
			return backing.getEdgeSource(e);
		}

		@Override
		public V getEdgeTarget(E e) {
			return backing.getEdgeTarget(e);
		}
	}
	
    public enum MultiLineAlignment {
        ML_ALIGN_LEFT, ML_ALIGN_CENTER, ML_ALIGN_RIGHT
    }

    /**
     * Escape a string, s.t. it can be used as a DOT attribute value.
     *
     * @param s   input string
     * @param mla alignment type
     * @return the string, with special characters replaced, and quoted if neccessary
     */
    public static String escapedToString(String s, MultiLineAlignment mla) {
        StringBuffer sb = new StringBuffer();
        boolean quote = false;
        boolean hasBreak = false;
        String lineBreak = null;
        switch (mla) {
            case ML_ALIGN_LEFT:
                lineBreak = "\\l";
                break;
            case ML_ALIGN_CENTER:
                lineBreak = "\\n";
                break;
            case ML_ALIGN_RIGHT:
                lineBreak = "\\r";
                break;
        }

        char c = 0;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case '\n':
                    sb.append(lineBreak);
                    hasBreak = true;
                    break;
                case '\r':
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                default:
                    sb.append(c);
                    break;
            }
            if (!Character.isLetterOrDigit(c)) quote = true;
        }
        // needed to justify the last line
        if (hasBreak && c != '\n') sb.append(lineBreak);
        if (quote || sb.length() == 0) {
            sb.insert(0, '"');
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Sets label and other attributes of a <code>DOT</code> node or edge based on the provided
     * object.
     *
     * @param <T> The type of the node/edge.
     */
    public interface DOTLabeller<T> {

        /**
         * get the label for the object
         *
         * @param object tje object to the the label from
         * @return the label, or null if no label should be used
         */
        String getLabel(T object);

        /**
         * Set <code>DOT</code> attributes depending on the given object
         *
         * @param object the object to set the label to
         * @param ht     the map to write attributes into
         * @return true if <code>ht</code> has been changed
         */
        boolean setAttributes(T object, Map<String, String> ht);
    }

    /**
     * Provides the ID and sets label and other attributes of a <code>DOT</code> node based
     * on the provided node.
     *
     * @param <T> node type
     */
    public interface DOTNodeLabeller<T> extends DOTLabeller<T> {
        /**
         * get the ID of a graph's node
         *
         * @param node the node to get the ID from
         * @return the (unique) integer ID
         */
        int getID(T node);
    }

    /**
     * Doesn't set a label.
     *
     * @param <T> the type of the objects getting labelled
     */
    public static class NullLabeller<T> implements DOTLabeller<T> {

        public String getLabel(T obj) {
            return null;
        }

        public boolean setAttributes(T object, Map<String, String> ht) {
            return false;
        }
    }

    /**
     * Uses {@link T#toString()} to provide a label
     *
     * @param <T> the type of the objects getting labelled
     */
    public static class DefaultDOTLabeller<T> implements DOTLabeller<T> {

        public String getLabel(T node) {
            return node.toString();
        }

        public boolean setAttributes(T node, Map<String, String> ht) {
            String label = getLabel(node);
            if (label != null) ht.put("label", label);
            return (node != null);
        }
    }

    /**
     * Uses a <code>Map</code>(dictionary) to provide labels
     *
     * @param <T> the type of the objects getting labelled
     */
    public static class MapLabeller<T> extends DefaultDOTLabeller<T> {

        protected Map<T, ?> annots;

        public MapLabeller(Map<T, ?> annots) {
            this.annots = annots;
        }

        public String getLabel(T obj) {
            if (annots.containsKey(obj)) {
                return annots.get(obj).toString();
            } else {
                return obj.toString();
            }
        }
    }

    /**
     * Additionally provides node IDs using a hash-based <code>Map</code>
     *
     * @param <T> The node type. Needs to provide correct <code>hashCode()</code>
     */
    public static class DefaultNodeLabeller<T>
            extends DefaultDOTLabeller<T>
            implements DOTNodeLabeller<T> {
        private Map<T, Integer> nodeId = new HashMap<T, Integer>();

        public int getID(T node) {
            Integer id = nodeId.get(node);
            if (id == null) {
                id = nodeId.size();
                nodeId.put(node, id);
            }
            return id;
        }
    }

    /**
     * Default DOT attributes used for nodes, using box shape and
     * a 10pt font.
     *
     * @return the attribute dictionary
     */
    public static Map<String, String> defaultNodeAttributes() {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("shape", "box");
        attrs.put("fontname", "Courier");
        attrs.put("fontsize", "10");
        return attrs;
    }

    /**
     * Default DOT attributes used for edges, using a 10pt font.
     *
     * @return the attribute dictionary
     */
    public static Map<String, String> defaultEdgeAttributes() {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("fontname", "Courier");
        attrs.put("fontsize", "10");
        return attrs;
    }

    private DOTNodeLabeller<V> nodeLabeller;
    private DOTLabeller<E> edgeLabeller;
    private Map<String, String> nodeAttributes;
    private Map<String, String> edgeAttributes;
    private Map<String, String> graphAttributes;
    private MultiLineAlignment multiLineAlignment;

    /**
     * Create a DOT exporter with {@link DefaultNodeLabeller} for nodes,
     * and {@link NullLabeller} for edges.
     */
    public AdvancedDOTExporter() {
        this(null, null);
    }

    /**
     * Create a DOT exporter using the provided labellers
     *
     * @param nodeLabeller if null, use {@link DefaultNodeLabeller}
     * @param edgeLabeller if null, use {@link NullLabeller}
     */
    public AdvancedDOTExporter(DOTNodeLabeller<V> nodeLabeller,
                               DOTLabeller<E> edgeLabeller) {
        this.nodeLabeller = nodeLabeller == null ? new DefaultNodeLabeller<V>() : nodeLabeller;
        this.edgeLabeller = edgeLabeller == null ? new NullLabeller<E>() : edgeLabeller;
        this.nodeAttributes = defaultNodeAttributes();
        this.edgeAttributes = defaultEdgeAttributes();
        this.graphAttributes = new HashMap<String, String>();
        this.multiLineAlignment = MultiLineAlignment.ML_ALIGN_LEFT;
    }

    /**
     * Create a DOT file for the given directed graph
     *
     * @param writer target for the export
     * @param graph  the directed graph to export
     * @throws IOException if {@code writer} raises an IO exception
     */
    public void exportDOT(Writer writer, DirectedGraph<V, E> graph) throws IOException {
        exportDOTDiGraph(writer, graph);
        writer.flush();
    }

    /**
     * Create a DOT digraph for the given graph
     *
     * @param writer target for the export
     * @param graph  the graph to export
     * @throws IOException if {@code writer} raises an IO exception
     */
    public void exportDOTDiGraph(Writer writer, Graph<V, E> graph) throws IOException {
    	exportDOTDiGraph(writer, graph.vertexSet(), graph.edgeSet(), new JGraphTAdapter<V,E>(graph));
    }
    
    /**
     * Create a DOT digraph for the given graph
     *
     * @param writer target for the export
     * @param graph  the graph to export
     * @throws IOException if {@code writer} raises an IO exception
     */
    public void exportDOTDiGraph(Writer writer, Iterable<V> vertexSet, Iterable<E> edgeSet, GraphAdapter<V,E> topo) throws IOException {
        writer.append("digraph cfg\n{\n");
        if (!this.graphAttributes.isEmpty()) {
            writer.append("graph ");
            appendAttributes(writer, this.graphAttributes);
            writer.append(";\n");
        }
        for (V n : vertexSet) {
            int id = nodeLabeller.getID(n);
            Map<String, String> attrs = new HashMap<String, String>(this.nodeAttributes);
            nodeLabeller.setAttributes(n, attrs);

            writer.append("" + id + " ");
            appendAttributes(writer, attrs);
            writer.append(";\n");
        }
        for (E e : edgeSet) {
            int idSrc = nodeLabeller.getID(topo.getEdgeSource(e));
            int idTarget = nodeLabeller.getID(topo.getEdgeTarget(e));
            Map<String, String> attrs = new HashMap<String, String>(this.edgeAttributes);
            edgeLabeller.setAttributes(e, attrs);

            writer.append("" + idSrc + " -> " + idTarget);
            appendAttributes(writer, attrs);
            writer.append(";\n");
        }
        writer.append("}\n");
    }

    private void appendAttributes(Writer w, Map<String, String> attrs) throws IOException {
        if (attrs.isEmpty()) return;
        w.append('[');
        boolean first = true;
        for (Entry<String, String> e : attrs.entrySet()) {
            if (first) first = false;
            else w.append(',');
            w.append(e.getKey());
            w.append('=');
            w.append(escapedToString(e.getValue(), this.multiLineAlignment));
        }
        w.append(']');
    }

    public void setGraphAttribute(String key, String val) {
        this.graphAttributes.put(key, val);
    }
}
