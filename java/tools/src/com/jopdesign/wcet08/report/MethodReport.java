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
package com.jopdesign.wcet08.report;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.Project;
import com.jopdesign.wcet08.frontend.CallGraph.CallGraphNode;
import com.jopdesign.wcet08.frontend.SourceAnnotations.LoopBound;
import com.jopdesign.wcet08.graphutils.Pair;

public class MethodReport {
	private MethodInfo info;
	private Collection<LoopBound> loopBounds;
	private Set<String> referenced;
	String page;
	public MethodReport(Project p, MethodInfo m, String page) {
		this.info = m;
		this.loopBounds = p.getWcetAppInfo().getFlowGraph(info).getLoopBounds().values();
		this.referenced = new TreeSet<String>();
		Iterator<CallGraphNode> i = p.getCallGraph().getReferencedMethods(m);
		while(i.hasNext()) {
			Pair<ClassInfo, String> ref = i.next().getReferencedMethod();
			this.referenced.add(ref.fst().clazz.getClassName()+"."+ref.snd());
		}
		this.page = page;
	}
	public MethodInfo getInfo() {
		return info;
	}
	public Collection<LoopBound> getLoopBounds() {
		return loopBounds;
	}
	public Set<String> getReferenced() {
		return referenced;
	}
	public String getPage() {
		return page;
	}
}
