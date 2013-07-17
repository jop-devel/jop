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
package com.jopdesign.wcet.uppaal.translator;

import com.jopdesign.common.MethodInfo;
import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.common.processormodel.JOPConfig.CacheImplementation;
import com.jopdesign.wcet.WCETTool;
import com.jopdesign.wcet.jop.BlockCache;
import com.jopdesign.wcet.jop.MethodCache;
import com.jopdesign.wcet.jop.VarBlockCache;
import com.jopdesign.wcet.uppaal.UppAalConfig;
import com.jopdesign.wcet.uppaal.UppAalConfig.UppaalCacheApproximation;
import com.jopdesign.wcet.uppaal.model.DuplicateKeyException;
import com.jopdesign.wcet.uppaal.model.NTASystem;
import com.jopdesign.wcet.uppaal.model.Template;
import com.jopdesign.wcet.uppaal.model.XmlSerializationException;
import com.jopdesign.wcet.uppaal.translator.cache.CacheSimBuilder;
import com.jopdesign.wcet.uppaal.translator.cache.FIFOCacheBuilder;
import com.jopdesign.wcet.uppaal.translator.cache.FIFOVarBlockCacheBuilder;
import com.jopdesign.wcet.uppaal.translator.cache.LRUCacheBuilder;
import com.jopdesign.wcet.uppaal.translator.cache.LRUVarBlockCacheBuilder;
import com.jopdesign.wcet.uppaal.translator.cache.StaticCacheBuilder;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Builder for the system modeling the java program.
 * Support the following features / modeling elements
 * <ul> <li/> global, elapsed time
 *      <li/> per-process clocks
 *      <li/> method synchronization channels
 *      <li/> call stacks
 * </ul>
 */
public class SystemBuilder {
	
	public static final String CLOCK = "t";
	public static final String BB_CLOCK = "t_local";
	public static String bbClock(int process) {
		return BB_CLOCK+"_"+process;
	}

	public static final String INVOKE_CHAN = "invoke";
	public static final String RETURN_CHAN = "ret";
	
	public static final String MAX_CALL_STACK_DEPTH = "max_csd";
	public static final String NUM_METHODS = "num_methods";
	public static final String ACTIVE_METHOD = "active_method";
	public static final String CURRENT_CALL_STACK_DEPTH = "cur_csd";

	private NTASystem system;
	public NTASystem getNTASystem() { return system; }
	
	private HashMap<Template,Integer> templates = new HashMap<Template,Integer>();
	private HashMap<Template,Integer> priorities = new HashMap<Template,Integer>();
	private HashMap<MethodInfo,Integer> methodId = new HashMap<MethodInfo, Integer>();
	private CacheSimBuilder cacheSim;
	private WCETTool project;
	private UppAalConfig config;
	private int numMethods;
	private int maxCallStackDepth;
	private Vector<String> progressMeasures = new Vector<String>();

	/**
	 * Create a new top-level UPPAAL System builder
	 * @param config             configuration
	 * @param p                  uplink to Project
	 * @param maxCallStackDepth  the maximal call stack depth
	 * @param methods            the methods of the program
	 */
	public SystemBuilder(UppAalConfig config,
			             WCETTool p,
			             int maxCallStackDepth, List<MethodInfo> methods) {
		this.config = config;
		this.project = p;
		this.system = new NTASystem(p.getProjectName());
		this.numMethods = methods.size();
		for(int i = 0; i < numMethods; i++) {
			methodId.put(methods.get(i), i);
		}
		this.cacheSim = getCacheSimulation();
		this.maxCallStackDepth = maxCallStackDepth;
		system.appendDeclaration("clock " + CLOCK +";");
		system.appendDeclaration("const int " + MAX_CALL_STACK_DEPTH + " = "+maxCallStackDepth+";"); 
		system.appendDeclaration("const int " + NUM_METHODS + " = "+numMethods+";"); 
		cacheSim.appendDeclarations(system,NUM_METHODS);
	}
	private CacheSimBuilder getCacheSimulation() {
		MethodCache cache = project.getWCETProcessorModel().getMethodCache();
		UppaalCacheApproximation cacheApprox = config.getCacheApproximation();
		CacheSimBuilder sim;
		if(cache.getName() == CacheImplementation.NO_METHOD_CACHE) {
			 sim = new StaticCacheBuilder(false);
		} else if(cacheApprox == UppaalCacheApproximation.ALWAYS_MISS) {
			sim = new StaticCacheBuilder(true);
		} else {
			switch(cache.getName()) {
			case LRU_CACHE: 
				sim = new LRUCacheBuilder((BlockCache)cache);
				break;
			case FIFO_CACHE:
				sim = new FIFOCacheBuilder((BlockCache)cache, config.emptyInitialCache);
				break;
			case LRU_VARBLOCK_CACHE:
				sim = new LRUVarBlockCacheBuilder(project, (VarBlockCache)cache, this.methodId.keySet());
				break;				
			case FIFO_VARBLOCK_CACHE: 
				sim = new FIFOVarBlockCacheBuilder(project,        (VarBlockCache)cache, 
						                       this.methodId.keySet(), config.emptyInitialCache);
				break;
			default: throw new AssertionError("Unsupport cache implementation: "+cache.getName());
			}
		}
		return sim;
	}
	public WCETTool getProject() {
		return project;
	}
	public CacheSimBuilder getCacheSim() {
		return this.cacheSim;
	}
	public int getMethodId(MethodInfo implementedMethod) {
		return this.methodId.get(implementedMethod);
	}
	public String accessCache(MethodInfo m) {
		return "access_cache("+getMethodId(m)+")";
	}
	public String addProcessClock(int proc) {
		String cl = bbClock(proc);
		system.appendDeclaration(String.format("clock %s; ", cl));
		return cl;
	}
	/* Method channel support
	 * ----------------------
	 */
	public void addMethodSynchChannels(List<MethodInfo> mis, Map<MethodInfo,Integer> mids) {
		for(MethodInfo mi : mis) {
			system.appendDeclaration("chan "+methodChannel(mids.get(mi))+";");				
		}		
	}
	public static String methodChannel(int i) { 
		return "invoke_"+i; 
	}
	/* call stack support 
	 * -----------------  
	 */
	public void addCallStack(MethodInfo rootMethod, int numCallSites) {
		int rootId = this.getMethodId(rootMethod);
		List<String> initArray = new LinkedList<String>();
		initArray.add(""+rootId);
		for(int i = 1; i < maxCallStackDepth; i++) { initArray.add(""+rootId); }
		system.appendDeclaration(String.format("int[0,%d] callStack[%s] = %s;",
				numCallSites, MAX_CALL_STACK_DEPTH, constArray(initArray).toString()));
		system.appendDeclaration(
				"void pushCallStack(int id) {\n" +
				"  int i;\n" +
				"  for(i = "+MAX_CALL_STACK_DEPTH+"-1;i > 0; --i) {\n"+
			    "    callStack[i] = callStack[i-1];\n"+
				"  }\n"+
				" callStack[0] = id;\n" +
				"}");
		system.appendDeclaration(
				"bool matchCallStack(int id) {\n" +
				"  return (callStack[0] == id);\n" +
				"}\n");
		system.appendDeclaration(
				"void popCallStack() {\n" +
				"  int i;\n" +
				"  for(i = 0; i < "+MAX_CALL_STACK_DEPTH+"-1;i++) {\n"+
			    "    callStack[i] = callStack[i+1];\n"+
				"  }\n"+
				"}");
		
	}
	public String pushCallStack(String id) {
		return String.format("pushCallStack(%s)",id);
	}
	public String matchCallStack(String id) {
		return String.format("callStack[0] == %s",id);		
	}
	public String popCallStack() {
		return "popCallStack()";
	}
	/** Build the final system */
	public void buildSystem() {
		StringBuilder sys = new StringBuilder();
		/* Create processes */
		for(Entry<Template,Integer> tmpl : templates.entrySet()) {
			sys.append(String.format("M%s = %s() ;\n", 
					   tmpl.getValue(), tmpl.getKey().getId()));
		}
		/* Instantiate processes */
		sys.append("system ");
		/* bucket sort templates by priority */
		TreeMap<Integer, List<Template>> templatesByPrio =
			MiscUtils.partialSort(templates.keySet(), new MiscUtils.F1<Template, Integer>() {
                public Integer apply(Template t) {
                    return priorities.get(t);
                }
            }
            );
		/* create system declaration strings */
		List<String> systemEntry = new LinkedList<String>();
		for(List<Template> entry : templatesByPrio.values()) {
			List<String> proclist = new LinkedList<String>();
			for(Template t : entry) {
				proclist.add("M"+templates.get(t));
			}
			systemEntry.add(MiscUtils.joinStrings(proclist, ", "));
		}
		sys.append(MiscUtils.joinStrings(systemEntry, " < "));
		sys.append(";\n");
		if(this.progressMeasures.size() > 0) {
			sys.append("progress { ");
			for(String pm : progressMeasures) {
				sys.append("  ");sys.append(pm);sys.append(";\n");				
			}
			sys.append("}");
		}
		this.system.setSystem(sys.toString());
	}
	
	/** add a template with the given id and priority */
	public void addTemplate(int procid, int priority, Template templ) throws DuplicateKeyException {
		this.templates.put(templ,procid);
		this.priorities.put(templ,priority);
		this.system.addTemplate(templ);
	}

	public Document toXML() throws XmlSerializationException {
		return this.system.toXML();
	}
	public static StringBuilder constArray(List<?> elems) {
		StringBuilder sb = new StringBuilder("{ ");
		boolean first = true;
		for(Object o : elems) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(o);
		}
		sb.append(" }");
		return sb;
	}
	public UppAalConfig getConfig() {
		return this.config;
	}
	public void addProgressMeasure(String progressMeasure) {
		this.progressMeasures .add(progressMeasure);
	}
	public void declareVariable(String ty, String var, String init) {
		this.system.appendDeclaration(ty+" "+var+" := "+init+";");		
	}
}
/* -- Using global-local clock
 * com.jopdesign.build.MethodInfo@cdbc83"wcet.StartLift.measure()V"
 * wcet: 9797
 * complex: 98
 * searchT: 4.58302
 * solverTmax: 0.265957
 * -- Using per method-local clock
 * com.jopdesign.build.MethodInfo@cdbc83"wcet.StartLift.measure()V"
 * wcet: 9797
 * complex: 98
 * searchT: 7.969566
 * solverTmax: 0.491255
 */

