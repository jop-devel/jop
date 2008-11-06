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
package com.jopdesign.wcet08;

import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import org.apache.log4j.Logger;

import com.jopdesign.build.ClassInfo;
import com.jopdesign.build.MethodInfo;
import com.jopdesign.wcet08.frontend.CallGraph;
import com.jopdesign.wcet08.frontend.FlowGraph;
import com.jopdesign.wcet08.frontend.JOPAppInfo;
import com.jopdesign.wcet08.frontend.SourceAnnotations;
import com.jopdesign.wcet08.frontend.SourceAnnotations.BadAnnotationException;
import com.jopdesign.wcet08.graphutils.TopOrder.BadGraphException;
import com.jopdesign.wcet08.report.Report;

/** WCET 'project', information on which method in which class to analyse etc. */
public class Project {
	public static class UnsupportedFeatureException extends Exception {
		private static final long serialVersionUID = 1L;
		public UnsupportedFeatureException(String msg) {
			super(msg);
		}
	}

	public static final Logger logger = Logger.getLogger(Project.class);

	private Config config;


	private String className;
	private String methodName;

	private JOPAppInfo appInfo;
	private CallGraph callGraph;

	public CallGraph getCallGraph() {
		return callGraph;
	}
	public ClassInfo getRootClass() {
		return callGraph.getRootClass();
	}
	public MethodInfo getRootMethod() {
		return callGraph.getRootMethod();
	}
	private Report results;
		
	private Map<MethodInfo, FlowGraph> cfgs;

	private Map<ClassInfo, SortedMap<Integer,Integer>> annotationMap;

	public Report getReport() { return results; }
	
	public Project() {
		this.config = Config.instance();
		this.className = config.getRootClassName();
		this.methodName = config.getRootMethodName();
		this.results = new Report(this);
	}
	
	public void load() throws Exception  {
		appInfo = new JOPAppInfo();
		appInfo.loadClasses(config.getRootClassName());
		callGraph = CallGraph.buildCallGraph(appInfo,className,methodName);

		/* Load source code annotations */
		annotationMap = new Hashtable<ClassInfo, SortedMap<Integer,Integer>>();
		SourceAnnotations sourceAnnotations = new SourceAnnotations(config);
		for(ClassInfo ci : callGraph.getClassInfos()) {
			annotationMap.put(ci,sourceAnnotations.calculateWCA(ci));
		}
		/* Analyse control flow graphs */
		cfgs = new Hashtable<MethodInfo, FlowGraph>();
		for(MethodInfo method : this.callGraph.getImplementedMethods()) {
			SortedMap<Integer,Integer> wcaMap = annotationMap.get(method.getCli());
			assert(wcaMap != null);
			FlowGraph fg;
			try {
				fg = new FlowGraph(method);
				fg.loadAnnotations(wcaMap);
			} catch (BadAnnotationException e) {
				logger.error("Bad annotation: "+e);
				e.printStackTrace();
				throw new Exception("Bad annotation: "+e,e);
			}  catch(BadGraphException e) {
				logger.error("Bad flow graph: "+e);
				throw new Exception("Bad flowgraph: "+e.getMessage(),e);				
			}
			cfgs.put(method,fg);
		}		
	}	
	public JOPAppInfo getAppInfo() {
		return this.appInfo;
	}
	public FlowGraph getFlowGraph(MethodInfo m) {
		if(cfgs.get(m) == null) 
			throw new AssertionError("No FlowGraph for "+m.getFQMethodName()+ ". Avail: "+this.cfgs.keySet());
		return cfgs.get(m);
	}

}
