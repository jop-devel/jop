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
package com.jopdesign.wcet08.frontend;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.traverse.DepthFirstIterator;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter;
import com.jopdesign.wcet08.graphutils.AdvancedDOTExporter.DefaultNodeLabeller;

/**
 * TypeGraph based on JGraphT. Supports Interfaces.
 * 
 * @author Benedikt Huber (benedikt.huber@gmail.com)
 *
 */
public class TypeGraph extends DefaultDirectedGraph<ClassInfo,DefaultEdge> {
	private static final long serialVersionUID = 1L;
	private ClassInfo rootNode;
	public ClassInfo getRootNode() { return rootNode; }
	public TypeGraph(WcetAppInfo ai) {
		super(DefaultEdge.class);
		build(ai);
	}
	private void build(WcetAppInfo appInfo) {
		this.rootNode = appInfo.getCliMap().get("java.lang.Object");
		for(ClassInfo ci : appInfo.getCliMap().values()) {
			this.addVertex(ci);
			if(ci.superClass != null) {
				if(ci.clazz.isInterface() && ! ci.superClass.clazz.isInterface()) {
					// Interface -> java.lang.Object
				} else {
					this.addVertex(ci.superClass);
					this.addEdge(ci.superClass ,ci);
				}
			}
			/* interface edges */
			for(String ifaceClass : ci.clazz.getInterfaceNames()) {
				ClassInfo iface = appInfo.getClassInfo(ifaceClass);
				this.addVertex(iface);
				this.addEdge(iface ,ci);
			}
		}
		this.rootNode = appInfo.getCliMap().get("java.lang.Object");
	}
	
	/**
	 * Subtypes (including the type itself) of the type denoted by the given class info
	 * @param ci 
	 * @return
	 */
	public List<ClassInfo> getSubtypes(ClassInfo base) {
		List<ClassInfo> subTypes = getStrictSubtypes(base);
		subTypes.add(base);
		return subTypes;
	}
	/**
	 * Subtypes (not including the type itself) of the type denoted by the given class info
	 * @param ci 
	 * @return
	 */
	public List<ClassInfo> getStrictSubtypes(ClassInfo base) {
		DepthFirstIterator<ClassInfo, DefaultEdge> iter = new DepthFirstIterator<ClassInfo, DefaultEdge>(this,base);
		iter.setCrossComponentTraversal(false);
		List<ClassInfo> subTypes = new Vector<ClassInfo>();
		iter.next();
		while(iter.hasNext()) {
			subTypes.add(iter.next());
		}
		return subTypes;
	}

	private class TgNodeLabeller extends DefaultNodeLabeller<ClassInfo> {
		@Override public String getLabel(ClassInfo ci) {
			return ci.clazz.getClassName();
		}

		@Override public boolean setAttributes(ClassInfo ci, Map<String, String> ht) {
			boolean labelled = super.setAttributes(ci, ht);
			if(ci.clazz.isInterface()) {
				ht.put("shape","ellipse");
			}
			return labelled;
		}
		
	}
	/**
	 * Write the type graph in DOT format.
	 * @param w 
	 * @param classFilter If non-null, only classes matching this prefix will be exported
	 * @throws IOException
	 */
	public void exportDOT(Writer w, String classFilter) throws IOException {
		Set<ClassInfo> subset = null;
		if(classFilter != null) {
			subset = new HashSet<ClassInfo>();
			subset.add(this.rootNode);
			for(ClassInfo ci : this.vertexSet()) {
				if(ci.clazz.getClassName().startsWith(classFilter)) {
					while(ci.superClass != null) {
						subset.add(ci);
						ci = ci.superClass;
					}
				}
			}
		}
		Subgraph<ClassInfo, DefaultEdge, TypeGraph> subgraph = 
			new Subgraph<ClassInfo, DefaultEdge, TypeGraph>(this,subset);
		AdvancedDOTExporter<ClassInfo, DefaultEdge> exporter = new AdvancedDOTExporter<ClassInfo, DefaultEdge>(
				new TgNodeLabeller(),
				null				
		);
		exporter.exportDOTDiGraph(w, subgraph);	
	}
}
